package org.jboss.webbeans.test.beans;

import javax.webbeans.Specializes;

import org.jboss.webbeans.test.annotations.Modern;
import org.jboss.webbeans.test.annotations.Motorized;

@Modern @Motorized @Specializes
public class Tractor extends Plough
{

}
