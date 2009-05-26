package org.jboss.webbeans.test.unit.bootstrap.multipleSimple;

import javax.enterprise.inject.Named;
import javax.enterprise.inject.deployment.Production;

@Production
@Named
class Salmon implements ScottishFish
{

}
