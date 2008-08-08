package org.jboss.webbeans.test.components;

import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.annotations.RequestScopedAnimalStereotype;

@RequestScopedAnimalStereotype
@RequestScoped
@Production
public class Goldfish implements Animal
{

}
