package org.jboss.webbeans.test.components;

import javax.ejb.Remote;
import javax.webbeans.Production;

@Remote
@Production
// TODO @BoundTo("/beans/baboon")
public interface Baboon extends Animal
{

}
