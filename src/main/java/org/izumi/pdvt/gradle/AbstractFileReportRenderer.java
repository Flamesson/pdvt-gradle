package org.izumi.pdvt.gradle;

import java.io.File;
import java.nio.file.Path;
import javax.annotation.Nonnull;

import org.gradle.api.artifacts.Configuration;
import org.gradle.internal.deprecation.DeprecatableConfiguration;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public abstract class AbstractFileReportRenderer implements FileReportRenderer {
    protected Path output;

    protected AbstractFileReportRenderer() {
        this((Path) null);
    }

    protected AbstractFileReportRenderer(String output) {
        this.output = Path.of(output);
    }

    protected AbstractFileReportRenderer(File output) {
        this(output.toPath());
    }

    protected AbstractFileReportRenderer(Path output) {
        this.output = output;
    }

    @Override
    public void setOutput(String path) {
        this.output = Path.of(path);
    }

    @Override
    public void setOutput(File file) {
        this.output = file.toPath();
    }

    @Override
    public void setOutput(Path path) {
        this.output = path;
    }

    @Override
    public void setOutputFile(@Nonnull File file) {
        setOutput(file);
    }

    protected boolean canBeResolved(Configuration configuration) {
        boolean isDeprecatedForResolving = ((DeprecatableConfiguration) configuration).getResolutionAlternatives() != null;
        return configuration.isCanBeResolved() && !isDeprecatedForResolving;
    }
}
