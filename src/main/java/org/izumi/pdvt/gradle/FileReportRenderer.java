package org.izumi.pdvt.gradle;

import java.io.File;
import java.nio.file.Path;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.diagnostics.internal.ReportRenderer;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public interface FileReportRenderer extends ReportRenderer {
    void render(Configuration configuration);

    void setOutput(String path);
    void setOutput(File file);
    void setOutput(Path path);
}
