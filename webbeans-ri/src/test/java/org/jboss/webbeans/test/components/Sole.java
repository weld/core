package org.jboss.webbeans.test.components;

import javax.webbeans.Named;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Whitefish;

@Production
@Whitefish
@Named("whitefish")
public class Sole implements ScottishFish
{

}
