package org.izumi.pdvt.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public class PdvtPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("pdvt", PdvtPluginExtension.class);

        project.getTasks().register("generateFile", PdvtGenerationTask.class, config -> {
            config.setDescription("Generates .pdvt file for all configurations.");
            config.setGroup("pdvt");
        });
    }
}
