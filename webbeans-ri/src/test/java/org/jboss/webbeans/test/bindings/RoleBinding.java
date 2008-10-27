package org.jboss.webbeans.test.bindings;

import javax.webbeans.AnnotationLiteral;

import org.jboss.webbeans.test.annotations.Role;

public class RoleBinding extends AnnotationLiteral<Role> implements Role
{
   private String value = null;

   public RoleBinding(String value)
   {
      this.value = value;
   }

   public String value()
   {
      return value;
   }

}
