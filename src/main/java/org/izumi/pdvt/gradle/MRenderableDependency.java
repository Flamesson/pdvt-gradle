package org.izumi.pdvt.gradle;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedVariantResult;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.AbstractRenderableDependencyResult;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.AbstractRenderableModuleResult;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.Section;

@RequiredArgsConstructor
public class MRenderableDependency implements RenderableDependency {
    private final RenderableDependency delegate;

    public ComponentIdentifier getId() {
        return (ComponentIdentifier) delegate.getId();
    }

    public boolean isDependency() {
        return delegate instanceof AbstractRenderableDependencyResult;
    }

    public boolean isModule() {
        return delegate instanceof AbstractRenderableModuleResult;
    }

    public boolean hasChildren() {
        return !delegate.getChildren().isEmpty();
    }

    public Set<MRenderableDependency> getChildren() {
        return delegate.getChildren().stream()
                .map(MRenderableDependency::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Nullable
    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public List<ResolvedVariantResult> getResolvedVariants() {
        return delegate.getResolvedVariants();
    }

    @Override
    public ResolutionState getResolutionState() {
        return delegate.getResolutionState();
    }

    @Override
    public List<Section> getExtraDetails() {
        return delegate.getExtraDetails();
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MRenderableDependency that = (MRenderableDependency) o;
        return Objects.equals(delegate, that.delegate);
    }
}
