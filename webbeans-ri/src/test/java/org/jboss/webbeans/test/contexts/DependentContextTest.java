package org.jboss.webbeans.test.contexts;

import java.lang.reflect.Method;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.beans.Fox;
import org.jboss.webbeans.test.beans.FoxRun;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tarantula;
import org.testng.annotations.Test;

@SpecVersion("20081206")
public class DependentContextTest extends AbstractTest
{

   /**
    * No injected instance of the Web Bean is ever shared between multiple
    * injection points.
    */
   @Test(groups = { "contexts", "injection" })
   @SpecAssertion(section = "9.4")
   public void testInstanceNotSharedBetweenInjectionPoints()
   {
      Bean<FoxRun> foxRunBean = SimpleBean.of(FoxRun.class, manager);
      Bean<Fox> foxBean = SimpleBean.of(Fox.class, manager);
      manager.addBean(foxBean);
      FoxRun foxRun = foxRunBean.create();
      assert !foxRun.fox.equals(foxRun.anotherFox);
   }

   /**
    * Any instance of the Web Bean that is used to evaluate a Unified EL
    * expression exists to service that evaluation only.
    */
   @Test(groups = { "stub", "contexts", "el" })
   @SpecAssertion(section = "9.4")
   public void testInstanceUsedForElEvalutionNotShared()
   {
      assert false;
   }

   /**
    * Any instance of the Web Bean that receives a producer method, producer
    * field, disposal method or observer method invocation exists to service
    * that invocation only
    */
   @Test(groups = { "contexts", "producerMethod" })
   @SpecAssertion(section = "9.4")
   public void testInstanceUsedForProducerMethodNotShared() throws Exception
   {
      SimpleBean<SpiderProducer> spiderProducer = SimpleBean.of(SpiderProducer.class, manager);
      manager.addBean(spiderProducer);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodBean<Tarantula> tarantulaBean = ProducerMethodBean.of(method, spiderProducer, manager);
      Tarantula tarantula = tarantulaBean.create();
      Tarantula tarantula2 = tarantulaBean.create();
      assert tarantula != null;
      assert tarantula2 != null;
      assert tarantula != tarantula2;
   }

   /**
    * Any instance of the Web Bean that receives a producer method, producer
    * field, disposal method or observer method invocation exists to service
    * that invocation only
    */
   @Test(groups = { "contexts", "producerMethod", "stub" })
   @SpecAssertion(section = "9.4")
   public void testInstanceUsedForProducerFieldNotShared() throws Exception
   {
      assert false;
   }

   /**
    * Any instance of the Web Bean that receives a producer method, producer
    * field, disposal method or observer method invocation exists to service
    * that invocation only
    */
   @Test(groups = { "stub", "contexts", "disposalMethod" })
   @SpecAssertion(section = "9.4")
   public void testInstanceUsedForDisposalMethodNotShared()
   {
      assert false;
   }

   /**
    * Any instance of the Web Bean that receives a producer method, producer
    * field, disposal method or observer method invocation exists to service
    * that invocation only
    */
   @Test(groups = { "stub", "contexts", "observerMethod" })
   @SpecAssertion(section = "9.4")
   public void testInstanceUsedForObserverMethodNotShared()
   {
      assert false;
   }

   /**
    * Every invocation of the get() operation of the Context object for the @Dependent
    * scope with the value true for the create parameter returns a new instance
    * of the given Web Bean
    */
   @Test(groups = "contexts")
   @SpecAssertion(section = "9.4")
   public void testContextGetWithCreateTrueReturnsNewInstance()
   {
      Bean<Fox> foxBean = SimpleBean.of(Fox.class, manager);
      manager.addBean(foxBean);
      DependentContext context = new DependentContext();
      context.setActive(true);
      assert context.get(foxBean, true) != null;
      assert context.get(foxBean, true) instanceof Fox;
   }

   /**
    * Every invocation of the get() operation of the Context object for the @Dependent
    * scope with the value false for the create parameter returns a null value
    */
   @Test(groups = "contexts")
   @SpecAssertion(section = "9.4")
   public void testContextGetWithCreateFalseReturnsNull()
   {
      Bean<Fox> foxBean = SimpleBean.of(Fox.class, manager);
      manager.addBean(foxBean);
      DependentContext context = new DependentContext();
      context.setActive(true);
      assert context.get(foxBean, false) == null;
   }

   /**
    * The @Dependent scope is inactive except:
    */
   @Test(groups = "contexts", expectedExceptions = ContextNotActiveException.class)
   @SpecAssertion(section = "9.4")
   public void testContextIsInactive()
   {
      manager.getContext(Dependent.class).isActive();
   }

   /**
    * when an instance of a Web Bean with scope @Dependent is created by the Web
    * Bean manager to receive a producer method, producer field, disposal method
    * or observer method invocation, or
    */
   @Test(groups = { "stub", "contexts", "producerMethod" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveWhenInvokingProducerMethod()
   {
      assert false;
   }

   /**
    * when an instance of a Web Bean with scope @Dependent is created by the Web
    * Bean manager to receive a producer method, producer field, disposal method
    * or observer method invocation, or
    */
   @Test(groups = { "stub", "contexts", "producerField" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveWhenInvokingProducerField()
   {
      assert false;
   }

   /**
    * when an instance of a Web Bean with scope @Dependent is created by the Web
    * Bean manager to receive a producer method, producer field, disposal method
    * or observer method invocation, or
    */
   @Test(groups = { "stub", "contexts", "disposalMethod" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveWhenInvokingDisposalMethod()
   {
      assert false;
   }

   /**
    * when an instance of a Web Bean with scope @Dependent is created by the Web
    * Bean manager to receive a producer method, producer field, disposal method
    * or observer method invocation, or
    */
   @Test(groups = { "stub", "contexts", "observerMethod" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveWhenInvokingObserverMethod()
   {
      assert false;
   }

   /**
    * while a Unified EL expression is evaluated, or
    */
   @Test(groups = { "stub", "contexts", "el" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveWhenEvaluatingElExpression()
   {
      assert false;
   }

   /**
    * when the Web Bean manager is creating or destroying a Web Bean instance or
    * injecting its dependencies, or
    */
   @Test(groups = { "contexts", "beanLifecycle" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveDuringBeanCreation()
   {
      // Slightly roundabout, but I can't see a better way to test atm
      Bean<FoxRun> foxRunBean = SimpleBean.of(FoxRun.class, manager);
      Bean<Fox> foxBean = SimpleBean.of(Fox.class, manager);
      manager.addBean(foxBean);
      FoxRun foxRun = foxRunBean.create();
      assert foxRun.fox != null;
   }

   /**
    * when the Web Bean manager is creating or destroying a Web Bean instance or
    * injecting its dependencies, or
    */
   @Test(groups = { "stub", "contexts", "beanDestruction" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveDuringBeanDestruction()
   {
      assert false;
   }

   /**
    * when the Web Bean manager is creating or destroying a Web Bean instance or
    * injecting its dependencies, or
    */
   @Test(groups = { "contexts", "injection" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveDuringInjection()
   {
      Bean<FoxRun> foxRunBean = SimpleBean.of(FoxRun.class, manager);
      Bean<Fox> foxBean = SimpleBean.of(Fox.class, manager);
      manager.addBean(foxBean);
      FoxRun foxRun = foxRunBean.create();
      assert foxRun.fox != null;
   }

   /**
    * when the Web Bean manager is injecting dependencies of an EJB bean or
    * Servlet or when an EJB bean @PostConstruct or @PreDestroy callback is
    * invoked by the EJB container
    */
   @Test(groups = { "contexts", "injection", "stub", "ejb3" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveDuringEJBDependencyInjection()
   {
      assert false;
   }

   /**
    * when the Web Bean manager is injecting dependencies of an EJB bean or
    * Servlet or when an EJB bean @PostConstruct or @PreDestroy callback is
    * invoked by the EJB container
    */
   @Test(groups = { "contexts", "injection", "stub", "servlet" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveDuringServletDependencyInjection()
   {
      assert false;
   }

   /**
    * when the Web Bean manager is injecting dependencies of an EJB bean or
    * Servlet or when an EJB bean @PostConstruct or @PreDestroy callback is
    * invoked by the EJB container
    */
   @Test(groups = { "contexts", "postconstruct", "stub", "ejb3" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveDuringEJBPostConstruct()
   {
      assert false;
   }

   /**
    * when the Web Bean manager is injecting dependencies of an EJB bean or
    * Servlet or when an EJB bean @PostConstruct or @PreDestroy callback is
    * invoked by the EJB container
    */
   @Test(groups = { "contexts", "predestroy", "stub", "ejb3" })
   @SpecAssertion(section = "9.4")
   public void testContextIsActiveDuringEJBPreDestroy()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "constructor" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromConstructor()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "removeMethod" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromRemoveMethod()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "initalizerMethod" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromInitializerMethod()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "producerMethod" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromProducerMethod()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "disposalMethod" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromDisposalMethod()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "preDestroy" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromPreDestroy()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "postConstruct" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromPostConstruct()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "interceptor" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromInterceptorOfActiveMethod()
   {
      assert false;
   }

   /**
    * A Web Bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from the Web Bean constructor, from the
    * Web Bean remove method, from initializer methods, from producer methods,
    * from disposal methods, from @PostConstruct and @PreDestroy callbacks and
    * from Web Beans interceptors or decorators for any of these methods
    */
   @Test(groups = { "stub", "contexts", "decorator" })
   @SpecAssertion(section = "9.4.1")
   public void testWebBeanMayCreateInstanceFromDecoratorOfActiveMethod()
   {
      assert false;
   }

   /**
    * An EJB bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from initializer methods, from @PostConstruct
    * and @PreDestroy callbacks and from Web Beans interceptors for these
    * methods.
    */
   @Test(groups = { "stub", "contexts", "ejb3", "initializerMethod" })
   @SpecAssertion(section = "9.4.1")
   public void testEjbBeanMayCreateInstanceFromInitializer()
   {
      assert false;
   }

   /**
    * An EJB bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from initializer methods, from @PostConstruct
    * and @PreDestroy callbacks and from Web Beans interceptors for these
    * methods.
    */
   @Test(groups = { "stub", "contexts", "ejb3", "postConstruct" })
   @SpecAssertion(section = "9.4.1")
   public void testEjbBeanMayCreateInstanceFromPostConstruct()
   {
      assert false;
   }

   /**
    * An EJB bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from initializer methods, from @PostConstruct
    * and @PreDestroy callbacks and from Web Beans interceptors for these
    * methods.
    */
   @Test(groups = { "stub", "contexts", "ejb3", "preDestroy" })
   @SpecAssertion(section = "9.4.1")
   public void testEjbBeanMayCreateInstanceFromPreDestroy()
   {
      assert false;
   }

   /**
    * An EJB bean may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from initializer methods, from @PostConstruct
    * and @PreDestroy callbacks and from Web Beans interceptors for these
    * methods.
    */
   @Test(groups = { "stub", "contexts", "ejb3", "interceptor" })
   @SpecAssertion(section = "9.4.1")
   public void testEjbBeanMayCreateInstanceFromInterceptorOfActiveMethod()
   {
      assert false;
   }

   /**
    * A Servlet may create an instance of a Web Bean with scope type @Dependent
    * by calling Manager.getInstance() from initializer methods
    */
   @Test(groups = { "stub", "contexts", "servlet", "initializerMethod" })
   @SpecAssertion(section = "9.4.1")
   public void testServletBeanMayCreateInstanceFromInitializer()
   {
      assert false;
   }

   /**
    * destroy all dependent objects of a Web Bean instance when the instance is
    * destroyed,
    */
   @Test(groups = { "stub", "contexts", "beanDestruction" })
   @SpecAssertion(section = "9.4.2")
   public void testDestroyingParentDestroysDependents()
   {
      assert false;
   }

   /**
    * destroy all dependent objects of an EJB bean or Servlet when the EJB bean
    * or Servlet is destroyed,
    */
   @Test(groups = { "stub", "contexts", "ejb3" })
   @SpecAssertion(section = "9.4.2")
   public void testDestroyingEjbDestroysDependents()
   {
      assert false;
   }

   /**
    * destroy all dependent objects of an EJB bean or Servlet when the EJB bean
    * or Servlet is destroyed,
    */
   @Test(groups = { "stub", "contexts", "servlet" })
   @SpecAssertion(section = "9.4.2")
   public void testDestroyingServletDestroysDependents()
   {
      assert false;
   }

   /**
    * destroy all @Dependent scoped contextual instances created during an EL
    * expression evaluation when the evaluation completes, and
    */
   @Test(groups = { "stub", "contexts", "el" })
   @SpecAssertion(section = "9.4.2")
   public void testDependentsDestroyedWhenElEvaluationCompletes()
   {
      assert false;
   }

   /**
    * destroy any @Dependent scoped contextual instance created to receive a
    * producer method, producer field, disposal method or observer method
    * invocation when the invocation completes
    */
   @Test(groups = { "stub", "contexts", "producerMethod" })
   @SpecAssertion(section = "9.4.2")
   public void testDependentsDestroyedWhenProducerMethodCompletes()
   {
      assert false;
   }
   
   /**
    * destroy any @Dependent scoped contextual instance created to receive a
    * producer method, producer field, disposal method or observer method
    * invocation when the invocation completes
    */
   @Test(groups = { "stub", "contexts", "producerField" })
   @SpecAssertion(section = "9.4.2")
   public void testDependentsDestroyedWhenProducerFieldCompletes()
   {
      assert false;
   }

   /**
    * destroy any @Dependent scoped contextual instance created to receive a
    * producer method, producer field, disposal method or observer method
    * invocation when the invocation completes
    */
   @Test(groups = { "stub", "contexts", "disposalMethod" })
   @SpecAssertion(section = "9.4.2")
   public void testDependentsDestroyedWhenDisposalMethodCompletes()
   {
      assert false;
   }
   
   /**
    * destroy any @Dependent scoped contextual instance created to receive a
    * producer method, producer field, disposal method or observer method
    * invocation when the invocation completes
    */
   @Test(groups = { "stub", "contexts", "observerMethod" })
   @SpecAssertion(section = "9.4")
   public void testDependentsDestroyedWhenObserverMethodEvaluationCompletes()
   {
      assert false;
   }

}
