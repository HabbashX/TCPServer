package com.habbashx.tcpserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that is used to indicate a local variable that may hold an empty
 * or invalid value. This is commonly used for variables that are initialized with
 * user-provided input, often requiring subsequent validation or handling for proper
 * operation.
 *
 * The presence of this annotation serves as a hint that the annotated variable could
 * potentially contain an empty or undesired state, thus requiring caution in subsequent
 * processing.
 *
 * This annotation is intended for use with local variables only and can help improve
 * code readability by explicitly marking variables that are expected to be validated or
 * may have specific conditions for their usage.
 *
 * Retention policy: RUNTIME - The annotation is available at runtime for reflective
 * operations.
 * Target: LOCAL_VARIABLE - This annotation can only be applied to local variables.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.LOCAL_VARIABLE)
public @interface PossibleEmpty {
}
