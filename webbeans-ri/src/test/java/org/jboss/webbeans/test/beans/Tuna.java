package org.jboss.webbeans.test.beans;

import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.annotations.AnotherDeploymentType;

@AnotherDeploymentType
@RequestScoped
public class Tuna
{
   public String getState() {
      return "tuned";
   }

}
