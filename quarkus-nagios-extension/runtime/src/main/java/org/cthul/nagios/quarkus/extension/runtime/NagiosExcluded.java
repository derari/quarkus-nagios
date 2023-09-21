package org.cthul.nagios.quarkus.extension.runtime;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

import jakarta.inject.Qualifier;

@Target(TYPE)
@Retention(RUNTIME)
@Qualifier
@Documented
public @interface NagiosExcluded {
}
