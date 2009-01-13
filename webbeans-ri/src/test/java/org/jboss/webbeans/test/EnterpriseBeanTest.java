package org.jboss.webbeans.test;

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.webbeans.examples.Translator;
import org.testng.annotations.Test;




public class EnterpriseBeanTest extends AbstractEjbEmbeddableTest
{

   @Test(expectedExceptions=UnsupportedOperationException.class)
   public void test() throws NamingException
   {
      
      InitialContext ctx = new InitialContext();
      Translator translator = (Translator) ctx.lookup("SentenceTranslator/local");
      try
      {
         translator.translate("foo");
      }
      catch (EJBException e)
      {
         if (e.getCausedByException() instanceof RuntimeException)
         {
            throw (RuntimeException) e.getCausedByException();
         }
         else 
         {
            throw new RuntimeException(e.getCausedByException());
         }
      }
   }
   
}
