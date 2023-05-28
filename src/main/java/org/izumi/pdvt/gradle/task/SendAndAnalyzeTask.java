package org.izumi.pdvt.gradle.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.izumi.pdvt.gradle.Dictionary;
import org.izumi.pdvt.gradle.File;
import org.izumi.pdvt.gradle.Mappings;
import org.izumi.pdvt.gradle.PdvtPluginExtension;
import org.izumi.pdvt.gradle.parameters.ExtensionParameters;
import org.izumi.pdvt.gradle.validation.ValidationResult;

@NonNullApi
public class SendAndAnalyzeTask extends PdvtReportTask {

    @Override
    public void generate(Project project) {
        final PdvtPluginExtension extension = project.getExtensions().getByType(PdvtPluginExtension.class);
        setParameters(new ExtensionParameters(extension));

        final ValidationResult validationOfClientCode = validateClientCode();
        if (validationOfClientCode.isNotValid()) {
            validationOfClientCode.joinMessagesAndCall(this::lifecycle);
            return;
        }

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
        if (getParameters().isDebug()) {
            logger.recreate();
        }

        lifecycle("-> Prepared intermediate files");

        final PdvtReportRenderer renderer = getRenderer();
        renderer.setDictionary(dictionary);
        renderer.setMappings(mappings);
        renderer.setLog(logger);

        final Set<Configuration> configurations = getReportConfigurations();
        for (Configuration configuration : configurations) {
            renderer.render(configuration);
            lifecycle("\t\tHandled \"" + configuration.getName() + "\" configuration");
        }

        renderer.renderItself(project);
        lifecycle("\t\tRendered first-level dependencies of the original project");

        renderer.complete();
        lifecycle("\tCompleted handling of configurations");

        final File result = new File(
                BUILD_DIRECTORY_PATH + String.format("%s_%s_%s.pdvt", UUID.randomUUID(), project.getName(), date)
        );
        result.recreate();
        compose(result, dictionary, mappings);
        dictionary.delete();
        mappings.delete();
        lifecycle("\tJoined intermediate files into single");

        new SendableReport(result, getParameters(), getLogger(), this::uriProvider).send();
        lifecycle("\tSent generated file to UI-part");
        result.delete();
        lifecycle("-> Task is completed");
    }

    private String uriProvider(Code code) {
        return code.getServer() + "/client/" + code.getCodeword() + "/files/add-and-analyze";
    }

    private ValidationResult validateClientCode() {
        final Optional<String> clientCodeOptional = getParameters().getClientCode();
        if (clientCodeOptional.isEmpty()) {
            return ValidationResult.failure().addMessage("Can't execute task because client code is not specified");
        }

        final String raw = clientCodeOptional.get();
        final String decoded = new String(Base64.getDecoder().decode(raw));
        final int firstColonIndex = decoded.indexOf(':');
        if (firstColonIndex == -1) {
            return ValidationResult.failure().addMessage("Can't execute task because client code is not valid. " +
                    "Please, copy it from UI");
        }

        return ValidationResult.success();
    }
}
