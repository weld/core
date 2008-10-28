package org.jboss.webbeans.test.beans;

import org.jboss.webbeans.test.annotations.Expensive;
import org.jboss.webbeans.test.annotations.Whitefish;

@Expensive(cost=50, veryExpensive=true)
@Whitefish
public class Halibut implements Animal
{
   
}
