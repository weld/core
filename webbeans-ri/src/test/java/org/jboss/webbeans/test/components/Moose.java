package org.jboss.webbeans.test.components;

import javax.webbeans.Named;

import org.jboss.webbeans.test.annotations.MammalStereotype;

@MammalStereotype
@Named("aMoose")
public class Moose implements Animal
{

}
