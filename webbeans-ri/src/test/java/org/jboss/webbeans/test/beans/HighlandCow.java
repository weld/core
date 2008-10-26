package org.jboss.webbeans.test.beans;

import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.annotations.HornedMammalStereotype;
import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;
import org.jboss.webbeans.test.annotations.Tame;

@HornedMammalStereotype
@RequestScopedAnimalStereotype
@RequestScoped
@Tame
public class HighlandCow implements Animal
{

}
