package org.jboss.webbeans.test.unit.bootstrap.environments;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.deployment.Production;

@Production
@RequestScoped
class SeaBass implements Animal
{

}
