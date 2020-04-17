package org.jboss.weld.tests.beanDeployment.managed.missingClassDependency;

import jakarta.inject.Named;

// The interface Fish doesn't get deployed
@Named
public class Herring implements Fish {

}
