package org.jboss.weld.tests.contexts.sessionInvalidation;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class SomeBean implements Serializable{
   private static final long serialVersionUID = 5318736702438067705L;
   public static final String DEFAULT_PROPERTY_VALUE = "default";
   
   private String prop = DEFAULT_PROPERTY_VALUE;

   public String getProp() {
      return prop;
   }

   public void setProp(String prop) {
      this.prop = prop;
   }

}
