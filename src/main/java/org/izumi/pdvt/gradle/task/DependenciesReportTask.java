package org.izumi.pdvt.gradle.task;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.izumi.pdvt.gradle.FileReportRenderer;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public interface DependenciesReportTask extends Task {
    void generate(Project project);

    FileReportRenderer getRenderer();
}
