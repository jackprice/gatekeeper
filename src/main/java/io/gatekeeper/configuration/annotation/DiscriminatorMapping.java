package io.gatekeeper.configuration.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DiscriminatorMapping {
    public String name();
    public Class mappedTo();
    public boolean isDefault() default false;
}
