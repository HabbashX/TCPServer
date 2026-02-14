package com.habbashx.tcpserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


/**
 * An annotation that indicates a local variable may hold an empty value.
 * This annotation is intended for use in cases where the variable may still
 * conform to its expected type but could conceptually represent an "empty"
 * or "default" state.
 * <p>
 * The use of this annotation serves as a marker and provides clarity for
 * code readers and maintainers. It communicates that the variable in question
 * may intentionally contain no meaningful data, though it is not null.
 * <p>
 * Common scenarios for using this annotation include:
 * - Situations where an argument retrieved from a list or input may be empty.
 * - Variables holding values that are permissible to be empty but must still
 * adhere to their data type constraints.
 * <p>
 * This annotation should be applied only to local variables.
 */
@Target(ElementType.LOCAL_VARIABLE)
public @interface MaybeEmpty {
}
