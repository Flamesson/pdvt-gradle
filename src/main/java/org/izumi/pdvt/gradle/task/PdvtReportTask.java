package org.izumi.pdvt.gradle.task;

import java.util.HashSet;
import java.util.Set;

import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.diagnostics.ProjectBasedReportTask;
import org.gradle.internal.deprecation.DeprecatableConfiguration;
import org.izumi.pdvt.gradle.Dictionary;
import org.izumi.pdvt.gradle.File;
import org.izumi.pdvt.gradle.Mappings;
import org.izumi.pdvt.gradle.parameters.Parameters;
import org.izumi.pdvt.gradle.parameters.ParametersStub;

@NonNullApi
public abstract class PdvtReportTask extends ProjectBasedReportTask implements DependenciesReportTask {
    protected static final String BUILD_DIRECTORY_PATH = "build/";
    protected final PdvtReportRenderer defaultRenderer = new PdvtReportRenderer(this::getParameters);
    private Parameters parameters = new ParametersStub();

    @Override
    public void generate(Project project) {
        //do nothing
    }

    @Override
    public PdvtReportRenderer getRenderer() {
        return defaultRenderer;
    }

    @Internal
    public ConfigurationContainer getTaskConfigurations() {
        return getProject().getConfigurations();
    }

    @Internal
    protected void lifecycle(String s) {
        getLogger().lifecycle(s);
    }

    @Internal
    protected void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Internal
    protected Parameters getParameters() {
        return parameters;
    }

    @Internal
    protected void compose(File result, Dictionary dictionary, Mappings mappings) {
        result.appendln(getSignature());
        result.appendln(getDictionarySectionName());
        result.appendln(dictionary);
        result.appendln(getMappingsSectionName());
        result.appendln(mappings);
    }

    @Internal
    protected Set<Configuration> getReportConfigurations() {
        return getNonDeprecatedTaskConfigurations();
    }

    @Internal
    protected Set<Configuration> getNonDeprecatedTaskConfigurations() {
        Set<Configuration> filteredConfigurations = new HashSet<>();
        for (Configuration configuration : getTaskConfigurations()) {
            if (!((DeprecatableConfiguration) configuration).isFullyDeprecated()) {
                filteredConfigurations.add(configuration);
            }
        }

        return filteredConfigurations;
    }

    @Internal
    private String getTag() {
        if (getParameters().withVersions()) {
            return "[versioned]";
        } else {
            return "[unversioned]";
        }
    }

    @Internal
    private String getVersion() {
        return "[" + 1 + "]";
    }

    @Internal
    private String getSignature() {
        return getTag() + getVersion();
    }

    @Internal
    private String getDictionarySectionName() {
        return "[dictionary]";
    }

    @Internal
    private String getMappingsSectionName() {
        return "[mappings]";
    }
}
