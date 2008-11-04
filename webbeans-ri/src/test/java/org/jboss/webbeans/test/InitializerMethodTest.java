package org.jboss.webbeans.test;

import javax.webbeans.DefinitionException;
import javax.webbeans.NonexistentMethodException;

import org.testng.annotations.Test;

public class InitializerMethodTest
{
   
   @Test(expectedExceptions=DefinitionException.class, groups="initializerMethod") @SpecAssertion(section="3.7")
   public void testStaticInitializerMethodNotAllowed()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "servlet"}) @SpecAssertion(section="3.7")
   public void testInitializerMethodCalledOnServlet()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "ejb3"}) @SpecAssertion(section="3.7")
   public void testInitializerMethodCalledOnEJBSessionBean()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "ejb3"}) @SpecAssertion(section="3.7")
   public void testInitializerMethodCalledOnEJBMDB()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "ejb3", "singletons"}) @SpecAssertion(section="3.7")
   public void testInitializerMethodCalledOnEJBSingleton()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "ejb3"}) @SpecAssertion(section="3.7")
   public void testInitializerMethodNotABusinessMethod()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "interceptors"}) @SpecAssertion(section="3.7")
   public void testMethodInterceptorNotCalledOnInitializerMethod()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod"}) @SpecAssertion(section="3.7")
   public void testMultipleInitializerMethodsAreCalled()
   {
      assert false;
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.7.1")
   public void testInitializerMethodAnnotatedProduces()
   {
      assert false;
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.7.1")
   public void testInitializerMethodAnnotatedDestructor()
   {
      assert false;
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.7.1")
   public void testInitializerMethodHasParameterAnnotatedDisposes()
   {
      assert false;
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.7.1")
   public void testInitializerMethodHasParameterAnnotatedObserves()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "webbeansxml"}) @SpecAssertion(section="3.7.2")
   public void testInitializerMethodDeclaredInXml()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "webbeansxml"}) @SpecAssertion(section="3.7.2")
   public void testInitializerMethodDeclaredInXmlIgnoresBindingAnnotationsInJava()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "webbeansxml"}, expectedExceptions=NonexistentMethodException.class) @SpecAssertion(section="3.7.2")
   public void testInitializerMethodDeclaredInXmlDoesNotExist()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod"}) @SpecAssertion(section="3.7.2")
   public void testBeanHasAllInitializerMethodsDeclaredInJava()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod", "webbeansxml"}) @SpecAssertion(section="3.7.2")
   public void testBeanHasAllInitializerMethodsDeclaredInJavaAndXml()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod"}) @SpecAssertion(section="3.7.3")
   public void testInitializerMethodParametersAreInjected()
   {
      assert false;
   }
   
   /*

   @Test(groups="initializerMethod") @SpecAssertion(section="3.7")
   public void test
   {
      assert false;
   }

    */
   
}
