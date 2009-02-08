package org.jboss.webbeans.porting;

import javax.el.ELContext;

import org.jboss.webbeans.mock.el.EL;

public class ELImpl implements org.jboss.jsr299.tck.spi.EL
{
   
   
   @SuppressWarnings("unchecked")
   public <T> T evaluateValueExpression(String expression, Class<T> expectedType)
   {
      ELContext elContext = EL.createELContext();
      return (T) EL.EXPRESSION_FACTORY.createValueExpression(elContext, expression, expectedType).getValue(elContext);
   }
 
   @SuppressWarnings("unchecked")
   public <T> T evaluateMethodExpression(String expression, Class<T> expectedType, Class<?>[] expectedParamTypes, Object[] expectedParams)
   {
      ELContext elContext = EL.createELContext();
      return (T) EL.EXPRESSION_FACTORY.createMethodExpression(elContext, expression, expectedType, expectedParamTypes).invoke(elContext, expectedParams);
   }
   
}
