package org.jboss.webbeans;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.DeploymentType;

/**
 * Deployment type for Web Beans beans
 * 
 * @author Pete Muir
 *
 */
@Target( { TYPE, METHOD })
@Retention(RUNTIME)
@Documented
@DeploymentType
public @interface WebBean
{

}
