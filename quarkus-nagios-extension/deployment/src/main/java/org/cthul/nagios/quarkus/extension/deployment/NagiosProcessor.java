package org.cthul.nagios.quarkus.extension.deployment;

import org.cthul.nagios.quarkus.extension.runtime.NagiosStatusHandler;
import org.cthul.nagios.quarkus.extension.runtime.NagiosStatusReporter;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;

class NagiosProcessor {

    private static final String FEATURE = "nagios";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem reporter() {
        return AdditionalBeanBuildItem.unremovableOf(NagiosStatusReporter.class);
    }

    @BuildStep
    RouteBuildItem route(NagiosConfig config) {
        return RouteBuildItem.builder()
                .management()
                .route(config.rootPath)
                .routeConfigKey("quarkus.nagios.root-path")
                .handler(new NagiosStatusHandler())
                .displayOnNotFoundPage()
                .blockingRoute()
                .build();
    }
}
