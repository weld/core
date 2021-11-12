package org.jboss.weld.tests.beanDeployment.managed.missingClassDependency;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

// The interface Fish doesn't get deployed
@Named
@Dependent
public class Herring implements Fish {

}
