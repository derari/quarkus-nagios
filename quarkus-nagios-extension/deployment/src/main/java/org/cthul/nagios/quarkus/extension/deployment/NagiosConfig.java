package org.cthul.nagios.quarkus.extension.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "nagios")
public class NagiosConfig {

    /**
     * Root path
     */
    @ConfigItem(defaultValue = "nagios")
    String rootPath;

    /**
     * Default group
     */
    @ConfigItem(defaultValue = "/well/or/group/")
    String defaultGroup;

    /**
     * Management enabled
     */
    @ConfigItem(name = "management.enabled", defaultValue = "true")
    boolean managementEnabled;
}
