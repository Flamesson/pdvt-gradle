package org.izumi.pdvt.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public interface DependenciesReportTask extends Task {
    void generate(Project project);

    FileReportRenderer getRenderer();
}
