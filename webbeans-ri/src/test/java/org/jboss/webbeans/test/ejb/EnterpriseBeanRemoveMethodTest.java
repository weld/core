package org.jboss.webbeans.test.ejb;

import javax.webbeans.DefinitionException;
import javax.webbeans.RequestScoped;
import javax.webbeans.UnremovedException;

import org.jboss.webbeans.bean.BeanFactory;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.ejb.invalid.Armant;
import org.jboss.webbeans.test.ejb.invalid.GoldenRetriever;
import org.jboss.webbeans.test.ejb.invalid.JackRussellTerrier;
import org.jboss.webbeans.test.ejb.invalid.Pumi;
import org.jboss.webbeans.test.ejb.invalid.Rottweiler;
import org.jboss.webbeans.test.ejb.invalid.RussellTerrier;
import org.jboss.webbeans.test.ejb.invalid.Saluki;
import org.jboss.webbeans.test.ejb.invalid.Spitz;
import org.jboss.webbeans.test.ejb.valid.GoodDoggie;
import org.jboss.webbeans.test.ejb.valid.Koirus;
import org.jboss.webbeans.test.ejb.valid.Toller;
import org.jboss.webbeans.test.ejb.valid.WelshCorgie;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Sections
 * 
 * 3.3.5. Web Bean remove methods 3.3.5.1. Declaring a Web Bean remove method
 * using annotations. 3.3.5.2. Declaring a Web Bean remove method using XML
 * 3.3.5.3. Remove method parameters
 * 
 * @author Nicklas Karlsson
 */
@SpecVersion("20081206")
@SuppressWarnings("unused")
public class EnterpriseBeanRemoveMethodTest extends AbstractTest
{

   /**
    * Initializes the EJB descriptors for the EJBs about to be used
    */
   @BeforeMethod
   public void setupEjbDescriptors()
   {
      addToEjbCache(JackRussellTerrier.class);
      addToEjbCache(Pumi.class);
      addToEjbCache(RussellTerrier.class);
      addToEjbCache(Rottweiler.class);
      addToEjbCache(GoldenRetriever.class);
      addToEjbCache(Armant.class);
      addToEjbCache(Spitz.class);
      addToEjbCache(Toller.class);
      addToEjbCache(WelshCorgie.class);
      addToEjbCache(Koirus.class);
      addToEjbCache(GoodDoggie.class);
   }

   /**
    * EJB spec
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatelessEnterpriseBeansWithRemoveMethodsFails()
   {
      EnterpriseBean<Armant> bean = BeanFactory.createEnterpriseBean(Armant.class, manager);
   }

   /**
    * When the Web Bean manager destroys an enterprise Web Bean instance that is
    * an EJB stateful session bean, it calls the Web Bean remove method
    */
   @Test(groups = { "enterpriseBeans", "removeMethod", "lifecycle", "stub" })
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanRemoveMethodCalledWhenDestroyedByManager()
   {
      EnterpriseBeanRemoveMethodTest.visited = false;
      EnterpriseBean<Toller> bena = BeanFactory.createEnterpriseBean(Toller.class, manager);
      RequestContext context = (RequestContext) manager.getContext(RequestScoped.class);
      Toller instance = context.get(bena, true);
      context.destroy();
      assert EnterpriseBeanRemoveMethodTest.visited;
   }

   /**
    * The Web Bean remove method is a remove method of the EJB stateful session
    * bean.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod", "lifecycle", "stub" })
   @SpecAssertion(section = "3.3.5")
   public void testWebBeanRemoveMethodIsEJBRemoveMethod()
   {
      assert false;
   }

   /**
    * If an enterprise Web Bean that is a stateful session bean and does not
    * have a Web Bean remove method declares any scope other than @Dependent, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeDependentScoped()
   {
      EnterpriseBean<Pumi> bean = BeanFactory.createEnterpriseBean(Pumi.class, manager);
   }

   /**
    * If an enterprise Web Bean that is a stateful session bean and does not
    * have a Web Bean remove method declares any scope other than @Dependent, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" })
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeDependentScoped2()
   {
      EnterpriseBean<WelshCorgie> bean = BeanFactory.createEnterpriseBean(WelshCorgie.class, manager);
   }

   /**
    * If an instance of an enterprise Web Bean that is a stateful session bean
    * and does not have a Web Bean remove method is not explicitly destroyed by
    * the application before the Web Bean manager attempts to destroy the
    * instance, an UnremovedException is thrown by the Web Bean manager
    */
   @Test(groups = { "enterpriseBeans", "removeMethod", "stub" }, expectedExceptions = UnremovedException.class)
   @SpecAssertion(section = "3.3.5")
   public void testStatefulEnterpriseBeanWithoutRemoveMethodMustBeRemovedByApplicationBeforeManager()
   {
      EnterpriseBean<Toller> bean = BeanFactory.createEnterpriseBean(Toller.class, manager);
      Toller instance = manager.getInstance(bean);
      RequestContext context = (RequestContext) manager.getContext(RequestScoped.class);
      context.destroy();
   }

   /**
    * If the scope is @Dependent, the application may call any EJB remove method
    * of an instance of the enterprise Web Bean, but then no parameters will be
    * passed to the method by the Web Bean manager
    */
   @Test(groups = { "enterpriseBeans", "removeMethod", "lifecycle", "stub" })
   @SpecAssertion(section = "3.3.5")
   public void applicationMayCallRemoveMethodOnDependentScopedSessionEnterpriseBeansButNoParametersArePassed()
   {
      assert false;
   }

   /**
    * If the application directly calls an EJB remove method of an instance of
    * an enterprise Web Bean that is a stateful session bean and declares any
    * scope other than @Dependent, an UnsupportedOperationException is thrown.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod", "lifecycle", "stub" }, expectedExceptions = UnsupportedOperationException.class)
   @SpecAssertion(section = "3.3.5")
   public void applicationCannotCallRemoveMethodOnNonDependentScopedSessionEnterpriseBean()
   {
      assert false;
   }

   /**
    * If the application directly calls an EJB remove method of an instance of
    * an enterprise Web Bean that is a stateful session bean and has scope
    * @Dependent, the Web Bean manager ignores the instance when instead of
    * destroying it
    */
   @Test(groups = { "enterpriseBeans", "removeMethod", "lifecycle", "stub" })
   @SpecAssertion(section = "3.3.5")
   public void applicationMayCallRemoveMethodOnDependentScopedSessionEnterpriseBeansButInstanceIsNotDestroyed()
   {
      assert false;
   }

   @Test(groups = { "enterpriseBeans", "removeMethod", "lifecycle", "stub" })
   @SpecAssertion(section = "3.3.5")
   public void testApplicationRemoveMethodCallRemovesInstanceFromContext()
   {
      EnterpriseBean<Toller> bean = BeanFactory.createEnterpriseBean(Toller.class, manager);
      RequestContext context = (RequestContext) manager.getContext(RequestScoped.class);
      Toller instance = context.get(bean, true);
      instance.bye();
      instance = context.get(bean, false);
      assert instance == null;
   }

   /**
    * If an enterprise Web Bean defined using annotations does not explicitly
    * declare a Web Bean remove method using @Destructor, and exactly one remove
    * method that accepts no parameters exists, then that remove method is the
    * Web Bean remove method.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" })
   @SpecAssertion(section = { "3.3.5.1" })
   public void testEnterpriseBeanWithoutDestructorUsesNoArgsRemoveAnnotatedMethodAsWebBeansRemoveMethod()
   {
      EnterpriseBean<Toller> bean = BeanFactory.createEnterpriseBean(Toller.class, manager);
      assert "bye".equals(bean.getRemoveMethod().getName());
   }

   /**
    * Otherwise, if no remove method that accepts no parameters exists, or if
    * multiple remove methods that accept no parameters exist, the enterprise
    * Web Bean has no Web Bean remove method.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" })
   @SpecAssertion(section = { "3.3.5.1" })
   public void testEnterpriseBeanWithoutDestructorAndNoOrMultipleNoArgsRemoveMethodsHasNoWebBeansRemoveMethod()
   {
      EnterpriseBean<Koirus> bean = BeanFactory.createEnterpriseBean(Koirus.class, manager);
      assert bean.getRemoveMethod() == null;
   }

   /**
    * If an enterprise Web Bean defined using annotations has more than one
    * method annotated @Destructor, a DefinitionException is thrown by the Web
    * Bean manager at initialization time.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = { "3.3.5.1" })
   public void testMultipleDestructorAnnotatedMethodsFails()
   {
      EnterpriseBean<Rottweiler> bean = BeanFactory.createEnterpriseBean(Rottweiler.class, manager);
   }

   /**
    * If an enterprise Web Bean defined using annotations has a method annotated
    * 
    * @Destructor, and that method is not an EJB remove method, a
    *              DefinitionException is thrown by the Web Bean manager at
    *              initialization time.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = { "3.3.5.1" })
   public void testDestructorAnnotatedMethodNotRemoveAnnotatedFails()
   {
      EnterpriseBean<RussellTerrier> bean = BeanFactory.createEnterpriseBean(RussellTerrier.class, manager);
   }

   @Test(groups = { "enterpriseBeans", "removeMethod" })
   @SpecAssertion(section = { "3.3.5.1" })
   public void testDestructorAnnotatedSingleRemoveMethod()
   {
      EnterpriseBean<GoodDoggie> bean = BeanFactory.createEnterpriseBean(GoodDoggie.class, manager);
   }

   /**
    * If a Web Bean remove method is annotated @Initializer or @Produces, has a
    * parameter annotated @Disposes, or has a parameter annotated @Observes, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = { "3.3.5.1" })
   public void testRemoveMethodIsInitializerFails()
   {
      EnterpriseBean<Saluki> bean = BeanFactory.createEnterpriseBean(Saluki.class, manager);
   }

   /**
    * If a Web Bean remove method is annotated @Initializer or @Produces, has a
    * parameter annotated @Disposes, or has a parameter annotated @Observes, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = { "3.3.5.1" })
   public void testRemoveMethodIsProducerFails()
   {
      EnterpriseBean<Spitz> bean = BeanFactory.createEnterpriseBean(Spitz.class, manager);
   }

   /**
    * If a Web Bean remove method is annotated @Initializer or @Produces, has a
    * parameter annotated @Disposes, or has a parameter annotated @Observes, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = { "3.3.5.1" })
   public void testRemoveMethodWithDisposesParameterFails()
   {
      EnterpriseBean<GoldenRetriever> bean = BeanFactory.createEnterpriseBean(GoldenRetriever.class, manager);
   }

   /**
    * If a Web Bean remove method is annotated @Initializer or @Produces, has a
    * parameter annotated @Disposes, or has a parameter annotated @Observes, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = { "3.3.5.1" })
   public void testRemoveMethodWithObservesParameterFails()
   {
      EnterpriseBean<JackRussellTerrier> bean = BeanFactory.createEnterpriseBean(JackRussellTerrier.class, manager);
   }

   /**
    * If an enterprise Web Bean defined using XML does not explicitly declare a
    * Web Bean remove method using XML, and exactly one remove method that
    * accepts no parameters exists, then that remove method is the Web Bean
    * remove method
    */
   @Test(groups = { "enterpriseBeans", "webbeansxml", "removeMethod", "stub" })
   @SpecAssertion(section = { "3.3.5.1", "3.3.5.2" })
   public void testXMLDefinedEnterpriseBeanWithoutDestructorUsesNoArgsRemoveAnnotatedMethodAsWebBeansRemoveMethod()
   {
      assert false;
   }

   /**
    * Otherwise, if no remove method that accepts no parameters exists, or if
    * multiple remove methods that accept no parameters exist, the enterprise
    * Web Bean has no Web Bean remove method.
    */
   @Test(groups = { "enterpriseBeans", "webbeansxml", "removeMethod", "stub" })
   @SpecAssertion(section = { "3.3.5.1", "3.3.5.2" })
   public void testXMLDefinedEnterpriseBeanWithoutDestructorAndNoOrMultipleNoArgsRemoveMethodsHasNoWebBeansRemoveMethod()
   {
      assert false;
   }

   /**
    * If the implementation class of an enterprise Web Bean declared in XML does
    * not have an EJB remove method with the name and parameter types declared
    * in XML, a NonexistentMethodException is thrown by the Web Bean manager at
    * initialization time
    */
   @Test(groups = { "enterpriseBeans", "webbeansxml", "removeMethod", "stub" })
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanWithoutMatchingRemoveMethodFails()
   {
      assert false;
   }

   /**
    * If an enterprise Web Bean defined using XML declares more than one Web
    * Bean remove method in XML, a DefinitionException is thrown by the Web Bean
    * manager at initialization time.
    */
   @Test(groups = { "enterpriseBeans", "webbeansxml", "removeMethod", "stub" })
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanWithMultipleRemoveMethodsFails()
   {
      assert false;
   }

   /**
    * When a Web Bean remove method is declared in XML, the Web Bean manager
    * ignores binding annotations applied to the Java method parameters
    */
   @Test(groups = { "enterpriseBeans", "webbeansxml", "removeMethod", "stub" })
   @SpecAssertion(section = "3.3.5.2")
   public void testXMLDefinedEnterpriseBeanIgnoresBindingAnnotationOnParameters()
   {
      assert false;
   }

   /**
    * If the Web Bean remove method has parameters, the Web Bean manager calls
    * Manager.getInstanceByType() to determine a value for each parameter and
    * calls the method with these parameter values.
    */
   @Test(groups = { "enterpriseBeans", "removeMethod", "stub" })
   @SpecAssertion(section = "3.3.5.3")
   public void testRemoveMethodParameterResolving()
   {
      assert false;
   }

}
