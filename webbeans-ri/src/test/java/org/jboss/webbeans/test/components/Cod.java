package org.jboss.webbeans.test.components;

import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Chunky;
import org.jboss.webbeans.test.annotations.Whitefish;

@Production
@Whitefish
@Chunky
public class Cod implements ScottishFish
{

}
