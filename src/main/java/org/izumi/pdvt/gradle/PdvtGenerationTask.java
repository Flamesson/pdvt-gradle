package org.izumi.pdvt.gradle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.diagnostics.ProjectBasedReportTask;
import org.gradle.internal.deprecation.DeprecatableConfiguration;
import org.izumi.pdvt.gradle.parameters.ExtensionParameters;
import org.izumi.pdvt.gradle.parameters.Parameters;
import org.izumi.pdvt.gradle.parameters.ParametersStub;

@NonNullApi
public class PdvtGenerationTask extends ProjectBasedReportTask implements DependenciesReportTask {
    private static final String BUILD_DIRECTORY_PATH = "build/";
    private final PdvtReportRenderer defaultRenderer = new PdvtReportRenderer(this::getParameters);
    private Parameters parameters = new ParametersStub();

    @Override
    public void generate(Project project) {
        final PdvtPluginExtension extension = project.getExtensions().getByType(PdvtPluginExtension.class);
        this.parameters = new ExtensionParameters(extension);

        final String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        final Dictionary dictionary = new Dictionary(
                BUILD_DIRECTORY_PATH + String.format("%s_%s_dictionary.pdvt", project.getName(), date)
        );
        final Mappings mappings = new Mappings(
                BUILD_DIRECTORY_PATH + String.format("%s_%s_mappings.pdvt", project.getName(), date)
        );
        final File logger = new File(
                BUILD_DIRECTORY_PATH + String.format("%s_%s_logs.pdvt", project.getName(), date)
        );
        dictionary.recreate();
        mappings.recreate();
        if (parameters.isDebug()) {
            logger.recreate();
        }

        final PdvtReportRenderer renderer = getRenderer();
        renderer.setDictionary(dictionary);
        renderer.setMappings(mappings);
        renderer.setLog(logger);

        final Set<Configuration> configurations = getReportConfigurations();
        for (Configuration configuration : configurations) {
            renderer.render(configuration);
        }

        renderer.complete();

        final File result = new File(
                BUILD_DIRECTORY_PATH + String.format("%s_%s.pdvt", project.getName(), date)
        );
        result.recreate();
        compose(result, dictionary, mappings);
        dictionary.delete();
        mappings.delete();
    }

    @Override
    public PdvtReportRenderer getRenderer() {
        return defaultRenderer;
    }

    private Parameters getParameters() {
        return parameters;
    }

    private void compose(File result, Dictionary dictionary, Mappings mappings) {
        result.appendln(getSignature());
        result.appendln(getDictionarySectionName());
        result.appendln(dictionary);
        result.appendln(getMappingsSectionName());
        result.appendln(mappings);
    }

    private Set<Configuration> getReportConfigurations() {
        return getNonDeprecatedTaskConfigurations();
    }

    private Set<Configuration> getNonDeprecatedTaskConfigurations() {
        Set<Configuration> filteredConfigurations = new HashSet<>();
        for (Configuration configuration : getTaskConfigurations()) {
            if (!((DeprecatableConfiguration) configuration).isFullyDeprecated()) {
                filteredConfigurations.add(configuration);
            }
        }

        return filteredConfigurations;
    }

    @Internal
    public ConfigurationContainer getTaskConfigurations() {
        return getProject().getConfigurations();
    }

    private String getTag() {
        if (parameters.withVersions()) {
            return "[versioned]";
        } else {
            return "[unversioned]";
        }
    }

    private String getVersion() {
        return "[" + 1 + "]";
    }

    private String getSignature() {
        return getTag() + getVersion();
    }

    private String getDictionarySectionName() {
        return "[dictionary]";
    }

    private String getMappingsSectionName() {
        return "[mappings]";
    }
}
