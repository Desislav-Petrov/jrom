package com.jrom.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * To be used on fields that need to be mapped on a separate datastructure and linked to the declaring class
 *
 * @author des
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Standalone {

    /**
     * If it's lazy load, when its requested. If it's eager, it's loaded at the moment of
     * load of the main object.
     */
    public enum ExternalType {
        LAZY, EAGER
    }

    /**
     * Pick up strategy for the external
     * @return
     */
    ExternalType externalType() default ExternalType.EAGER;

    /**
     * Data structure key for the external
     *
     * @return
     */
    String externalNamespace();

    /**
     * Method that provides the id for this external. Must be unique for different instances
     * or an override can occure
     * @return
     */
    String idMethodProvider();

    /**
     * If this is true, all updates/deletes will be applied to this entity as well
     * @return
     */
    boolean cascade() default false;
}
