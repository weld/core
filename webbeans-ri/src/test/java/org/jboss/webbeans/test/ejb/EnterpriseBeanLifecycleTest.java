package org.jboss.webbeans.test.ejb;

import javax.webbeans.UnremovedException;

import org.jboss.webbeans.bean.BeanFactory;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.ejb.valid.GoodDoggie;
import org.jboss.webbeans.test.ejb.valid.LocalGoodDoggie;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Sections
 * 
 * 6.4. Lifecycle of stateful session enterprise Web beans
 * 6.5. Lifecycle of stateless session and singleton enterprise Web Beans
 * 6.9. Lifecycle of EJB beans
 * 
 * Mostly overlapping with other tests...
 * 
 * @author Nicklas Karlsson 
 */

@SpecVersion("20081206")
public class EnterpriseBeanLifecycleTest extends AbstractTest
{
   /**
    * Initializes the EJB descriptors for the EJBs about to be used
    */
   @BeforeMethod
   public void setupEjbDescriptors()
   {
      addToEjbCache(GoodDoggie.class);
      addToEjbCache(LocalGoodDoggie.class);
   }
   
   /**
    * When the create() method is called, the Web Bean manager creates and
    * returns an enterprise bean proxy
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "stub" })
   @SpecAssertion(section = "6.4")
   public void testProxyCreated()
   {
      assert false;
   }

   /**
    * When the destroy() method is called, the Web Bean manager calls the Web
    * Bean remove method upon the proxy
    */
   @Test(groups = { "enterpriseBeans", "clientProxy", "lifecycle", "stub"})
   @SpecAssertion(section = "6.4")
   public void testRemoveMethodCalled()
   {
      visited = false;
      DependentContext.INSTANCE.setActive(true);
      EnterpriseBean<GoodDoggie> bean = BeanFactory.createEnterpriseBean(GoodDoggie.class, manager);
      manager.addBean(bean);
      GoodDoggie doggie = manager.getInstance(bean);
      bean.destroy(doggie);
      assert visited = true;
   }

   /**
    * For each remove method parameter, the Web Bean manager passes the object
    * returned by Manager.getInstanceByType()
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "removeMethod", "stub" })
   @SpecAssertion(section = "6.4")
   public void testFieldInjections()
   {
      assert false;
   }

   /**
    * If the enterprise Web Bean has no Web Bean remove method, the Web Bean
    * manager throws an UnremovedException.
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "removeMethod", "stub" }, expectedExceptions = UnremovedException.class)
   @SpecAssertion(section = "6.4")
   public void testNoRemoveMethodFails()
   {
      assert false;
   }

   /**
    * If the underlying EJB was already destroyed by direct invocation of a
    * remove method by the application, the Web Bean manager ignores the
    * instance, and is not required to call any remove method
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "removeMethod", "stub" })
   @SpecAssertion(section = "6.4")
   public void testNoRemoveMethodsCalledIfEnterpriseBeanAlreadyRemoved()
   {
      assert false;
   }

   /**
    * When the destroy() method is called, the Web Bean manager simply discards
    * the proxy and all EJB local object references.
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "removeMethod", "stub" })
   @SpecAssertion(section = "6.5")
   public void testProxyAndLocalObjectReferencesDiscardedForStatelessEnterpriseBeans()
   {
      assert false;
   }
   
   /**
    * The Web Bean manager initializes the values of all injected fields. For
    * each injected field, the Web Bean manager sets the value to the object
    * returned by Manager.getInstanceByType().
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "stub" })
   @SpecAssertion(section = "6.9")
   public void testFieldInjectionsOnRemoveMethods()
   {
      assert false;
   }   

   /**
    * Next, if the EJB bean instance is an instance of a Web Bean, the Web Bean
    * manager initializes the values of any fields with initial values specified
    * in XML,
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "stub" })
   @SpecAssertion(section = "6.9")
   public void testInitXMLDefinedValuesOnWebWeanEnterpriseBeans()
   {
      assert false;
   }

   /**
    * Next, the Web Bean manager calls all initializer methods. For each
    * initializer method parameter, the Web Bean manager passes the object
    * returned by Manager.getInstanceByType().
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "stub" })
   @SpecAssertion(section = "6.9")
   public void testInitializerMethodsCalledWithCurrentParameterValues()
   {
      assert false;
   }

   /**
    * Finally, the Web Bean manager builds the interceptor and decorator stacks
    * for the instance
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "interceptors", "stub" })
   @SpecAssertion(section = "6.9")
   public void testInterceptorStackIsBuilt()
   {
      assert false;
   }

   /**
    * Finally, the Web Bean manager builds the interceptor and decorator stacks
    * for the instance
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "decorators", "stub" })
   @SpecAssertion(section = "6.9")
   public void testDecoratorStackIsBuilt()
   {
      assert false;
   }

   /**
    * When the EJB container destroys an instance of an EJB bean, the Web Bean
    * manager intercepts the @PreDestroy callback and destroys all dependent
    * objects, after the callback returns from the bean instance
    */
   @Test(groups = { "enterpriseBeans", "lifecycle", "stub" })
   @SpecAssertion(section = "6.9")
   public void testDependentObjectsDestroyed()
   {
      assert false;
   }

}
