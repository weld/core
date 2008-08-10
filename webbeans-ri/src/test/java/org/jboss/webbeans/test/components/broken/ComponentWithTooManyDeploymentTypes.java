package org.jboss.webbeans.test.components.broken;

import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;

@Production
@AnotherDeploymentType
public class ComponentWithTooManyDeploymentTypes
{

}
