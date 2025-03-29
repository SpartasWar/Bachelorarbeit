package de.uni_due.inf.ti.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Non null by default
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({
	ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD,
	ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE,
	ElementType.TYPE_PARAMETER
})
public @interface NonNullByDefault
{
}
