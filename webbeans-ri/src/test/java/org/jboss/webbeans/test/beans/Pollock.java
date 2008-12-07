package org.jboss.webbeans.test.beans;

import javax.webbeans.Dependent;

import org.jboss.webbeans.test.annotations.AnimalStereotype;
import org.jboss.webbeans.test.annotations.FishStereotype;

@AnimalStereotype
@FishStereotype
@Dependent
public class Pollock implements Animal
{
   
}
