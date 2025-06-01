package com.habbashx.tcpserver.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Container annotation is used to define metadata for a class that specifies
 * persistent file storage and an optional description. This annotation allows association
 * of a file path containing non-volatile data and provides contextual information about
 * the intended usage or purpose of the file.
 *
 * It is typically used to annotate classes that manage and persist domain-specific
 * data in a structured file.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Container {

    /**
     * Specifies the name or path of the file associated with the annotated container.
     *
     * @return the file name or path as a string
     */
    String file();

    /**
     * Provides an optional description for the annotated type. This description can be used
     * to supply additional contextual or explanatory information.
     *
     * @return A string representing the description, or an empty string if no description is provided.
     */
    String description() default "";
}
