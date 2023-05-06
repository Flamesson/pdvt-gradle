package org.izumi.pdvt.gradle.parameters;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;
import org.izumi.pdvt.gradle.PdvtPluginExtension;

@RequiredArgsConstructor
public class ExtensionParameters implements Parameters {
    private static final boolean WITH_VERSIONS_DEFAULT = false;
    private static final boolean IS_DEEP_DEFAULT = true;
    private static final boolean IS_DEBUG_DEFAULT = false;
    private final PdvtPluginExtension extension;

    @Override
    public boolean withVersions() {
        return extension.getWithVersions().getOrElse(WITH_VERSIONS_DEFAULT);
    }

    @Override
    public boolean isDeep() {
        return extension.getIsDeep().getOrElse(IS_DEEP_DEFAULT);
    }

    @Override
    public String getGroupFilter() {
        return extension.getGroupFilter().getOrNull();
    }

    @Override
    public boolean isDebug() {
        return extension.getIsDebug().getOrElse(IS_DEBUG_DEFAULT);
    }

    @Override
    public boolean doesFitToGroupFilter(RenderableDependency dependency) {
        final String groupFilter = getGroupFilter();
        if (Objects.isNull(groupFilter)) {
            return true;
        }

        //TODO: separate class with politics (contains, start with, regex, etc)

        return dependency.getName().contains(groupFilter);
    }
}
