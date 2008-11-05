package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

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

@SpecVersion("PDR")
public class InitializerMethodTest extends AbstractTest
{
   
   @Test(expectedExceptions=DefinitionException.class, groups="initializerMethod") @SpecAssertion(section="3.7")
   public void testStaticInitializerMethodNotAllowed()
   {
      createSimpleWebBean(Dottrel.class, manager);
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
   
   @Test(groups={"initializerMethod"}) @SpecAssertion(section={"3.7", "5.3", "3.7.2", "3.7.3"})
   public void testMultipleInitializerMethodsAreCalled()
   {
      manager.addBean(createSimpleWebBean(Fox.class, manager));
      manager.addBean(createSimpleWebBean(Chicken.class, manager));
      
      Bean<ChickenHutch> chickenHutchBean = createSimpleWebBean(ChickenHutch.class, manager);
      ChickenHutch chickenHutch = chickenHutchBean.create();
      assert chickenHutch.fox != null;
      assert chickenHutch.chicken != null;
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.7.1")
   public void testInitializerMethodAnnotatedProduces()
   {
      createSimpleWebBean(Pheasant.class, manager);
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.7.1")
   public void testInitializerMethodAnnotatedDestructor()
   {
      createSimpleWebBean(Shrike.class, manager);
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.7.1")
   public void testInitializerMethodHasParameterAnnotatedDisposes()
   {
      createSimpleWebBean(Capercaillie.class, manager);
   }
   
   @Test(groups="initializerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section="3.7.1")
   public void testInitializerMethodHasParameterAnnotatedObserves()
   {
      createSimpleWebBean(Grouse.class, manager);
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
   
   @Test(groups={"initializerMethod", "webbeansxml"}) @SpecAssertion(section="3.7.2")
   public void testBeanHasAllInitializerMethodsDeclaredInJavaAndXml()
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
