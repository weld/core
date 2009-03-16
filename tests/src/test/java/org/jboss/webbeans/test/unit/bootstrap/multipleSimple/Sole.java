package org.jboss.webbeans.test.unit.bootstrap.multipleSimple;

import javax.annotation.Named;
import javax.inject.Production;

@Production
@Whitefish
@Named("whitefish")
class Sole implements ScottishFish
{

}
