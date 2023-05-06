package org.izumi.pdvt.gradle.parameters;

import org.gradle.api.tasks.diagnostics.internal.graph.nodes.RenderableDependency;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public interface Parameters {
    boolean withVersions();
    boolean isDeep();
    String getGroupFilter();

    boolean isDebug();

    boolean doesFitToGroupFilter(RenderableDependency dependency);
    default boolean doesNotFitToGroupFilter(RenderableDependency dependency) {
        return !doesFitToGroupFilter(dependency);
    }
}
