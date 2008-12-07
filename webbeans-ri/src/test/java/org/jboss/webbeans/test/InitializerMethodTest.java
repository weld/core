package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import javax.webbeans.DefinitionException;
import javax.webbeans.NonexistentMethodException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.test.beans.Chicken;
import org.jboss.webbeans.test.beans.ChickenHutch;
import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.broken.Capercaillie;
import org.jboss.webbeans.test.beans.broken.Dottrel;
import org.jboss.webbeans.test.beans.broken.Grouse;
import org.jboss.webbeans.test.beans.broken.Pheasant;
import org.jboss.webbeans.test.beans.broken.Shrike;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class InitializerMethodTest extends AbstractTest
{
   
   @Test(expectedExceptions=DefinitionException.class, groups="initializerMethod") @SpecAssertion(section="3.8")
   public void testStaticInitializerMethodNotAllowed()
   {
      createSimpleBean(Dottrel.class);
   }
   
   @Test(groups={"stub", "initializerMethod", "servlet"}) @SpecAssertion(section="3.8")
   public void testInitializerMethodCalledOnServlet()
   {
      assert false;
   }
   
   @Test(groups={"stub", "initializerMethod", "ejb3"}) @SpecAssertion(section="3.8")
   public void testInitializerMethodCalledOnEJBSessionBean()
   {
      assert false;
   }
   
   @Test(groups={"stub", "initializerMethod", "ejb3"}) @SpecAssertion(section="3.8")
   public void testInitializerMethodCalledOnEJBMDB()
   {
      assert false;
   }
   
   @Test(groups={"stub", "initializerMethod", "ejb3", "singletons"}) @SpecAssertion(section="3.8")
   public void testInitializerMethodCalledOnEJBSingleton()
   {
      assert false;
   }
   
   @Test(groups={"stub", "initializerMethod", "ejb3"}) @SpecAssertion(section="3.8")
   public void testInitializerMethodNotABusinessMethod()
   {
      assert false;
   }
   
   @Test(groups={"stub", "initializerMethod", "interceptors"}) @SpecAssertion(section="3.8")
   public void testMethodInterceptorNotCalledOnInitializerMethod()
   {
      assert false;
   }
   
   @Test(groups={"initializerMethod"}) @SpecAssertion(section={"3.8", "5.3", "3.8.2", "3.8.3"})
   public void testMultipleInitializerMethodsAreCalled()
   {
      manager.addBean(createSimpleBean(Fox.class));
      manager.addBean(createSimpleBean(Chicken.class));
      
      Bean<ChickenHutch> chickenHutchBean = createSimpleBean(ChickenHutch.class);
      ChickenHutch chickenHutch = chickenHutchBean.create();
      assert chickenHutch.fox != null;
      assert chickenHutch.chicken != null;
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section={"3.8.1", "3.4.2"})
   public void testInitializerMethodAnnotatedProduces()
   {
      createSimpleBean(Pheasant.class);
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.8.1")
   public void testInitializerMethodAnnotatedDestructor()
   {
      createSimpleBean(Shrike.class);
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.8.1")
   public void testInitializerMethodHasParameterAnnotatedDisposes()
   {
      createSimpleBean(Capercaillie.class);
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.8.1")
   public void testInitializerMethodHasParameterAnnotatedObserves()
   {
      createSimpleBean(Grouse.class);
   }
   
   @Test(groups={"stub", "initializerMethod", "webbeansxml"}) @SpecAssertion(section="3.8.2")
   public void testInitializerMethodDeclaredInXml()
   {
      assert false;
   }
   
   @Test(groups={"stub", "initializerMethod", "webbeansxml"}) @SpecAssertion(section="3.8.2")
   public void testInitializerMethodDeclaredInXmlIgnoresBindingAnnotationsInJava()
   {
      assert false;
   }
   
   @Test(groups={"stub", "initializerMethod", "webbeansxml"}, expectedExceptions=NonexistentMethodException.class) @SpecAssertion(section="3.8.2")
   public void testInitializerMethodDeclaredInXmlDoesNotExist()
   {
      assert false;
   }
   
   @Test(groups={"stub", "initializerMethod", "webbeansxml"}) @SpecAssertion(section="3.8.2")
   public void testBeanHasAllInitializerMethodsDeclaredInJavaAndXml()
   {
      assert false;
   }
   
   /*

   @Test(groups="initializerMethod") @SpecAssertion(section="3.8")
   public void test
   {
      assert false;
   }

    */
   
}
