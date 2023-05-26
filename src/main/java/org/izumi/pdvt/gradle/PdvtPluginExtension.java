package org.izumi.pdvt.gradle;

import org.gradle.api.provider.Property;

public interface PdvtPluginExtension {
    Property<Boolean> getWithVersions();
    Property<Boolean> getIsDeep();
    Property<String> getGroupFilter();
    Property<Boolean> getIsDebug();
    Property<String> getClientCode();
}
