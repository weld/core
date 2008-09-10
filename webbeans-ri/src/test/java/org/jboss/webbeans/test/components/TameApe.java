package org.jboss.webbeans.test.components;

import javax.ejb.Remote;
import javax.webbeans.BoundTo;
import javax.webbeans.Named;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Tame;

@Remote
@Production
@BoundTo("/beans/ape")
@Tame
@Named
public interface TameApe extends Animal
{

}
