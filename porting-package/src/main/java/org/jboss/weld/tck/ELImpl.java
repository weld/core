package org.jboss.weld.tck;

import javax.el.ELContext;

import org.jboss.jsr299.tck.api.JSR299Configuration;
import org.jboss.testharness.api.Configurable;
import org.jboss.testharness.api.Configuration;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.mock.el.EL;

public class ELImpl implements org.jboss.jsr299.tck.spi.EL, Configurable
{
   
   private JSR299Configuration configuration;
   
   
   @SuppressWarnings("unchecked")
   public <T> T evaluateValueExpression(String expression, Class<T> expectedType)
   {
      ELContext elContext = createELContext();
      return (T) EL.EXPRESSION_FACTORY.createValueExpression(elContext, expression, expectedType).getValue(elContext);
   }
 
   @SuppressWarnings("unchecked")
   public <T> T evaluateMethodExpression(String expression, Class<T> expectedType, Class<?>[] expectedParamTypes, Object[] expectedParams)
   {
      ELContext elContext = createELContext();
      return (T) EL.EXPRESSION_FACTORY.createMethodExpression(elContext, expression, expectedType, expectedParamTypes).invoke(elContext, expectedParams);
   }
   
   public ELContext createELContext()
   {
      if (configuration.getManagers().getManager() instanceof BeanManagerImpl)
      {
         return EL.createELContext((BeanManagerImpl) configuration.getManagers().getManager());
      }
      else
      {
         throw new IllegalStateException("Wrong manager");
      }
   }

   public void setConfiguration(Configuration configuration)
   {
      if (configuration instanceof JSR299Configuration)
      {
         this.configuration = (JSR299Configuration) configuration; 
      }
      else
      {
         throw new IllegalArgumentException("Can only use ELImpl in the CDI TCK");
      }
   }
   
}
