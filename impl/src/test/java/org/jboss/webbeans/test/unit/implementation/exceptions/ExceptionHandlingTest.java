package org.jboss.webbeans.test.unit.implementation.exceptions;

import javax.inject.CreationException;

import org.jboss.webbeans.test.unit.AbstractTest;
import org.testng.annotations.Test;

public class ExceptionHandlingTest extends AbstractTest
{

   @Test(expectedExceptions=FooException.class)
   public void testCreationExceptionWrapsRealExceptionForSimpleBean() throws Exception
   {
      deployBeans(Lorry_Broken.class);
      new RunInDependentContext()
      {
         
         @Override
         protected void execute() throws Exception
         {
            try
            {
               manager.getInstanceByType(Lorry_Broken.class);
            }
            catch (Exception e) 
            {
               if (e instanceof CreationException)
               {
                  throw (Exception) e.getCause();
               }
            }
         }
         
      }.run();
   }
   

   @Test(expectedExceptions=FooException.class)
   public void testCreationExceptionWrapsRealExceptionForProducerBean() throws Exception
   {
      deployBeans(ShipProducer_Broken.class);
      new RunInDependentContext()
      {
         
         @Override
         protected void execute() throws Exception
         {
            try
            {
               manager.getInstanceByType(Ship.class);
            }
            catch (Exception e) 
            {
               if (e instanceof CreationException)
               {
                  throw (Exception) e.getCause();
               }
            }
         }
         
      }.run();
   }
   
}
