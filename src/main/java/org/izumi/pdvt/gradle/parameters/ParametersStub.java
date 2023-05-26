package org.izumi.pdvt.gradle.parameters;

import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
@RequiredArgsConstructor
public class ParametersStub implements Parameters {
    private final boolean withVersions;
    private final boolean deep;
    private final String groupFilter;

    public ParametersStub() {
        this(false, true, null);
    }

    @Override
    public boolean withVersions() {
        return withVersions;
    }

    @Override
    public boolean isDeep() {
        return deep;
    }

    @Override
    public String getGroupFilter() {
        return groupFilter;
    }

    @Override
    public boolean doesFitToGroupFilter(RenderableDependency dependency) {
        if (Objects.isNull(groupFilter)) {
            return true;
        }

        //TODO: separate class with politics (contains, start with, regex, etc)

        return dependency.getName().contains(groupFilter);
    }

    @Override
    public boolean isDebug() {
        return true;
    }

    @Override
    public Optional<String> getClientCode() {
        return Optional.empty();
    }
}
