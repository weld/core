package org.jboss.webbeans.test.unit.event;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.webbeans.DeploymentType;

@Target( { TYPE, METHOD })
@Retention(RUNTIME)
@Documented
@DeploymentType
@interface AnotherDeploymentType
{

}
