package org.jboss.webbeans.test.beans;

import javax.ejb.Remote;
import javax.webbeans.Named;
import javax.webbeans.Production;

import org.jboss.webbeans.test.annotations.Tame;

@Remote
@Production
// TODO @BoundTo("/beans/tame/orangutan")
@Tame
@Named
public interface TameOrangutan extends Orangutan
{

}
