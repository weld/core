package org.jboss.webbeans.test.unit.implementation.exceptions;

import javax.inject.AnnotationLiteral;
import javax.inject.CreationException;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ExceptionHandlingTest extends AbstractWebBeansTest
{

   @Test(expectedExceptions=FooException.class)
   public void testCreationExceptionWrapsRealExceptionForSimpleBean() throws Exception
   {
      new RunInDependentContext()
      {
         
         @Override
         protected void execute() throws Exception
         {
            try
            {
               getCurrentManager().getInstanceByType(Lorry_Broken.class);
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
      new RunInDependentContext()
      {
         
         @Override
         protected void execute() throws Exception
         {
            try
            {
               getCurrentManager().getInstanceByType(Ship.class, new AnnotationLiteral<Large>() {});
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
