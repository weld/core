package org.jboss.webbeans.test.beans;

import org.jboss.webbeans.test.annotations.Expensive;
import org.jboss.webbeans.test.annotations.Whitefish;

@Expensive(cost=60, veryExpensive=true)
@Whitefish
public class RoundWhitefish implements Animal
{
   
}
