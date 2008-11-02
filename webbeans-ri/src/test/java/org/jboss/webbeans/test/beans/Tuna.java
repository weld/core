package org.jboss.webbeans.test.beans;

import javax.webbeans.Current;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;

@AnotherDeploymentType
@RequestScoped
public class Tuna
{
   @Current Animal animal;
   
   public String getState() {
      return "tuned";
   }

}
