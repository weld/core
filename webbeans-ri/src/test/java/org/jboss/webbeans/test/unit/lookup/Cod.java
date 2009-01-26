package org.jboss.webbeans.test.unit.lookup;

import javax.annotation.Named;
import javax.inject.Production;


@Production
@Whitefish
@Chunky(realChunky=true)
@Named("whitefish")
class Cod implements ScottishFish
{

}
