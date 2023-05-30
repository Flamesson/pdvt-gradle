package org.izumi.pdvt.gradle.task;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.ResolvedModuleVersion;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.internal.artifacts.DefaultProjectComponentIdentifier;
import org.gradle.api.tasks.diagnostics.internal.ProjectDetails;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableModuleResult;
import org.gradle.initialization.BuildClientMetaData;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.izumi.pdvt.gradle.AbstractFileReportRenderer;
import org.izumi.pdvt.gradle.Alias;
import org.izumi.pdvt.gradle.Dictionary;
import org.izumi.pdvt.gradle.File;
import org.izumi.pdvt.gradle.MRenderableDependency;
import org.izumi.pdvt.gradle.Mapping;
import org.izumi.pdvt.gradle.Mappings;
import org.izumi.pdvt.gradle.collections.DequeWrapper;
import org.izumi.pdvt.gradle.generator.AliasGenerator;
import org.izumi.pdvt.gradle.parameters.Parameters;

@NonNullApi
@RequiredArgsConstructor
public class PdvtReportRenderer extends AbstractFileReportRenderer {
    private final AliasGenerator generator = new AliasGenerator();
    private final Supplier<Parameters> parametersSupplier;
    private Set<Object> visitedIds;
    private Dictionary dictionary;
    private Mappings mappings;
    private File log;

    @Override
    public void render(Configuration configuration) {
        final Parameters parameters = parametersSupplier.get();
        if (!canBeResolved(configuration)) {
            log(parameters, configuration.getName() + " can't be resolved");
            return;
        }

        visitedIds = new HashSet<>();

        final ResolutionResult result = configuration.getIncoming().getResolutionResult();
        final Deque<MRenderableDependency> toHandle = new LinkedList<>();
        toHandle.add(new MRenderableDependency(new RenderableModuleResult(result.getRoot())));
        while (!toHandle.isEmpty()) {
            final MRenderableDependency handled = toHandle.poll();
            if (handled.isDependency()) {
                log(parameters, handled.getId() + " in handling");
                handleDependency(handled, children -> new DequeWrapper<>(toHandle).addFirst(children));
            } else if (handled.isModule()) {
                if (handled.hasChildren()) {
                    new DequeWrapper<>(toHandle).addFirst(handled.getChildren());
                }
            } else {
                throw new IllegalStateException("Unknown type of dependency met. The dependency: " + handled);
            }
        }

        log(parameters, configuration.getName() + " was resolved");
    }

    /**
     * <p>Renders own dependencies of project.</p>
     *
     * @param project The project.
     */
    public void renderItself(Project project) {
        final Deque<ResolvedDependency> toHandle = new ArrayDeque<>();
        project.getConfigurations().stream()
                .filter(Configuration::isCanBeResolved)
                .map(configuration -> configuration.getResolvedConfiguration().getFirstLevelModuleDependencies())
                .forEach(toHandle::addAll);

        final Alias source = dictionary.getAlias(toArtifact(project), generator::generate);

        while (!toHandle.isEmpty()) {
            final ResolvedDependency dependency = toHandle.pop();
            final ResolvedModuleVersion module = dependency.getModule();
            final ModuleVersionIdentifier identifier = module.getId();

            final Alias target = dictionary.getAlias(toString(identifier), generator::generate);
            mappings.addIfAbsent(new Mapping(source.getNewName(), target.getNewName()));
        }
    }

    @Override
    public void setClientMetaData(BuildClientMetaData buildClientMetaData) {
    }

    @Override
    public void setOutput(StyledTextOutput styledTextOutput) {
    }

    @Override
    public void startProject(ProjectDetails projectDetails) {
        //nothing to initialize
    }

    @Override
    public void completeProject(ProjectDetails projectDetails) {
        //nothing to clear
    }

    @Override
    public void complete() {
        //nothing to clear
    }

    void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    void setLog(File log) {
        this.log = log;
    }

    private void handleDependency(MRenderableDependency handled,
                                  Consumer<Collection<? extends MRenderableDependency>> childrenConsumer) {
        final Parameters parameters = parametersSupplier.get();
        if (!parameters.doesFitToGroupFilter(handled)) {
            return;
        }

        if (handled.hasChildren() && parameters.isDeep()) {
            final Collection<MRenderableDependency> children = handled.getChildren().stream()
                    .filter(child -> !visitedIds.contains(child.getId()))
                    .collect(Collectors.toList());
            childrenConsumer.accept(children);
        }

        final Alias source = dictionary.getAlias(toString(handled), generator::generate);
        final Set<? extends MRenderableDependency> dependsOn = handled.getChildren();
        dependsOn.stream().filter(parameters::doesFitToGroupFilter).forEach(dependsOnItem -> {
            final Alias target = dictionary.getAlias(toString(dependsOnItem), generator::generate);
            mappings.addIfAbsent(new Mapping(source.getNewName(), target.getNewName()));
            visitedIds.add(dependsOnItem.getId());
        });
        visitedIds.add(handled.getId());
    }

    private String toString(RenderableDependency dependency) {
        return toString(new MRenderableDependency(dependency));
    }

    private String toString(MRenderableDependency dependency) {
        if (dependency.getId() instanceof ModuleComponentIdentifier) {
            return toString((ModuleComponentIdentifier) dependency.getId());
        } else if (dependency.getId() instanceof DefaultProjectComponentIdentifier) {
            final DefaultProjectComponentIdentifier identifier = (DefaultProjectComponentIdentifier) dependency.getId();
            return identifier.getDisplayName();
        } else {
            throw new IllegalStateException("Unknown implementation of id. " +
                    "The implementation: " + dependency.getId().getClass().getSimpleName());
        }
    }

    private String toString(ModuleComponentIdentifier identifier) {
        final Parameters parameters = parametersSupplier.get();
        if (!parameters.withVersions()) {
            return identifier.getGroup() + ":" + identifier.getModule();
        } else {
            return identifier.getGroup() + ":" + identifier.getModule() + ":" + identifier.getVersion();
        }
    }

    private String toString(ModuleVersionIdentifier identifier) {
        final Parameters parameters = parametersSupplier.get();
        if (!parameters.withVersions()) {
            return identifier.getModule().toString();
        } else {
            return identifier.getModule() + ":" + identifier.getVersion();
        }
    }

    private void log(Parameters parameters, String message) {
        if (parameters.isDebug()) {
            log.appendln(message);
        }
    }

    private String toArtifact(Project project) {
        final String group = project.getGroup().toString();
        final String name = project.getName();
        final String version = project.getVersion().toString();

        return group + ":" + name + ":" + version;
    }
}
