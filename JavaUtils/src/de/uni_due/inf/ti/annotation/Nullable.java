// Created: 2014-10-??

package de.uni_due.inf.ti.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is applied to reference types that may assume the value
 * {@code null}.
 * 
 * @author H.J. Sander Bruggink
 */
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@Documented
@Retention(RetentionPolicy.CLASS)
public @interface Nullable
{
}
