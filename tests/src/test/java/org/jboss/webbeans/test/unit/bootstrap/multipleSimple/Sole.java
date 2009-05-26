package org.jboss.webbeans.test.unit.bootstrap.multipleSimple;

import javax.enterprise.inject.Named;
import javax.enterprise.inject.deployment.Production;

@Production
@Whitefish
@Named("whitefish")
class Sole implements ScottishFish
{

}
