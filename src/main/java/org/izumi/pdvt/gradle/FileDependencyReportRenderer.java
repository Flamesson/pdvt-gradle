package org.izumi.pdvt.gradle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.function.Supplier;

import org.gradle.api.NonNullApi;
import org.gradle.api.tasks.diagnostics.internal.dependencies.AsciiDependencyReportRenderer;
import org.gradle.internal.logging.text.StreamingStyledTextOutput;
import org.gradle.internal.logging.text.StyledTextOutput;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
@NonNullApi
public class FileDependencyReportRenderer extends AsciiDependencyReportRenderer {
    private final Supplier<StyledTextOutput> textOutputSupplier;

    public FileDependencyReportRenderer(String outputFilePath) {
        this.textOutputSupplier = () -> Utils.silently(
                () -> new StreamingStyledTextOutput(new BufferedWriter(new FileWriter(outputFilePath, false)))
        );
    }

    @Override
    public StyledTextOutput getTextOutput() {
        return textOutputSupplier.get();
    }
}
