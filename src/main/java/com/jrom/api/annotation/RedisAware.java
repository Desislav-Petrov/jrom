package com.jrom.api.annotation;

import java.lang.annotation.*;

/**
 * Every class to be handled by jrom needs to have this annotation
 *
 * @author des
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface RedisAware {

    /**
     * Used as a key for the redis structure that will hold data about this object
     * @return
     */
    String namespace();
}
