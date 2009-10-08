/**
 * 
 */
package org.jboss.weld.test.unit.activities.current;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Scope;

@Target( { TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@Documented
@Scope
@Inherited
@interface NonNormalScope {}