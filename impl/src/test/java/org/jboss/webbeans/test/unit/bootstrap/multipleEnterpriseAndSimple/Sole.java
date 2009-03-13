package org.jboss.webbeans.test.unit.bootstrap.multipleEnterpriseAndSimple;

import javax.annotation.Named;
import javax.inject.Production;

@Production
@Whitefish
@Named("whitefish")
class Sole implements ScottishFish
{

}
