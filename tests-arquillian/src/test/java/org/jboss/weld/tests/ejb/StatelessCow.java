package org.jboss.weld.tests.ejb;

import javax.ejb.Stateless;

@Stateless
public class StatelessCow {
   public String call() {
      return "moo";
   }
}
