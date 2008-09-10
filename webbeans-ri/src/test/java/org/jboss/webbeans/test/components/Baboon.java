package org.jboss.webbeans.test.components;

import javax.ejb.Remote;
import javax.webbeans.BoundTo;
import javax.webbeans.Production;

@Remote
@Production
@BoundTo("/beans/baboon")
public interface Baboon extends Animal
{

}
