package org.izumi.pdvt.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.izumi.pdvt.gradle.task.GenerateAndSendPdvtFileTask;
import org.izumi.pdvt.gradle.task.GeneratePdvtFileTask;
import org.izumi.pdvt.gradle.task.SendAndAnalyzeTask;
import org.izumi.pdvt.gradle.task.SendPdvtFileTask;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public class PdvtPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("pdvt", PdvtPluginExtension.class);

        project.getTasks().register("generate", GeneratePdvtFileTask.class, config -> {
            config.setDescription("Generates .pdvt file for all configurations.");
            config.setGroup("pdvt");
        });
        project.getTasks().register("generateAndSend", GenerateAndSendPdvtFileTask.class, config -> {
            config.setDescription("Generates .pdvt file for all configurations and sends it to UI. " +
                    "Client code must be filled");
            config.setGroup("pdvt");
        });
        project.getTasks().register("send", SendPdvtFileTask.class, config -> {
            config.setDescription("Generates .pdvt-temp file, sends it to UI. " +
                    ".pdvt file will be deleted after send. Client code must be filled");
            config.setGroup("pdvt");
        });
        project.getTasks().register("sendAndAnalyze", SendAndAnalyzeTask.class, config -> {
            config.setDescription("Generates .pdvt-temp file, sends it to UI and invokes analyze. " +
                    ".pdvt file will be deleted after send. Client code must be filled");
            config.setGroup("pdvt");
        });
    }
}
