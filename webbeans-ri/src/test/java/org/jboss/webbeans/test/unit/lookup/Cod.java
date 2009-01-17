package org.jboss.webbeans.test.unit.lookup;

import javax.webbeans.Named;
import javax.webbeans.Production;


@Production
@Whitefish
@Chunky(realChunky=true)
@Named("whitefish")
class Cod implements ScottishFish
{

}
