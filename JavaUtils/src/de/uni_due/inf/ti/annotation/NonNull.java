package de.uni_due.inf.ti.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is applied to reference types that cannot assume the value
 * {@code null}.
 * <p>
 * For return values, the semantics is that the method will not return
 * {@code null} if the method arguments satisfy the pre-conditions of the 
 * method.
 * <p>
 * For method parameters, the semantics is that the method only functions
 * when the parameter is non-null; otherwise the method has undefined behavior
 * (typically, however, a {@link NullPointerException} will be thrown).
 * 
 * @author H.J. Sander Bruggink
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface NonNull
{
}
