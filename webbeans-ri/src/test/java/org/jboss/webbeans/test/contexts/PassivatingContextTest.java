package org.jboss.webbeans.test.contexts;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.ConversationScoped;
import javax.webbeans.DefinitionException;
import javax.webbeans.IllegalProductException;
import javax.webbeans.RequestScoped;
import javax.webbeans.SessionScoped;
import javax.webbeans.UnserializableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.MetaDataCache;
import org.jboss.webbeans.bean.AbstractProducerBean;
import org.jboss.webbeans.bean.BeanFactory;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.contexts.invalid.CityProducer;
import org.jboss.webbeans.test.contexts.invalid.CityProducer2;
import org.jboss.webbeans.test.contexts.invalid.CityProducer3;
import org.jboss.webbeans.test.contexts.invalid.Loviisa;
import org.jboss.webbeans.test.contexts.invalid.Peraseinajoki;
import org.jboss.webbeans.test.contexts.invalid.Espoo;
import org.jboss.webbeans.test.contexts.invalid.Forssa;
import org.jboss.webbeans.test.contexts.invalid.Hamina;
import org.jboss.webbeans.test.contexts.invalid.Jamsa;
import org.jboss.webbeans.test.contexts.invalid.Kaarina;
import org.jboss.webbeans.test.contexts.invalid.Kotka;
import org.jboss.webbeans.test.contexts.invalid.Kuopio;
import org.jboss.webbeans.test.contexts.invalid.Maarianhamina;
import org.jboss.webbeans.test.contexts.invalid.Mikkeli;
import org.jboss.webbeans.test.contexts.invalid.Nokia;
import org.jboss.webbeans.test.contexts.invalid.Pietarsaari;
import org.jboss.webbeans.test.contexts.invalid.Porvoo;
import org.jboss.webbeans.test.contexts.invalid.Raisio;
import org.jboss.webbeans.test.contexts.invalid.Salo;
import org.jboss.webbeans.test.contexts.invalid.Uusikaupunki;
import org.jboss.webbeans.test.contexts.invalid.Vantaa;
import org.jboss.webbeans.test.contexts.invalid.Violation;
import org.jboss.webbeans.test.contexts.invalid.Violation2;
import org.jboss.webbeans.test.contexts.valid.Hyvinkaa;
import org.jboss.webbeans.test.contexts.valid.Joensuu;
import org.jboss.webbeans.test.contexts.valid.Jyvaskyla;
import org.jboss.webbeans.test.contexts.valid.Turku;
import org.jboss.webbeans.test.contexts.valid.Vaasa;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * 
 */
@SpecVersion("20081206")
@SuppressWarnings("unused")
public class PassivatingContextTest extends AbstractTest
{
   @BeforeMethod
   public void initContext()
   {
      addToEjbCache(Turku.class);
      addToEjbCache(Kaarina.class);
      addToEjbCache(Espoo.class);
      addToEjbCache(Maarianhamina.class);
      addToEjbCache(Porvoo.class);
      addToEjbCache(Pietarsaari.class);
   }

   /**
    * EJB local objects are serializable. Therefore, an enterprise Web Bean may
    * declare any passivating scope.
    */
   @Test(groups = { "contexts", "passivation", "enterpriseBean" })
   @SpecAssertion(section = "9.5")
   public void testEJBWebBeanCanDeclarePassivatingScope()
   {
      EnterpriseBean<Turku> bean = BeanFactory.createEnterpriseBean(Turku.class, manager);
      assert true;
   }

   /**
    * Simple Web Beans are not required to be serializable. If a simple Web Bean
    * declares a passivating scope, and the implementation class is not
    * serializable, a DefinitionException is thrown by the Web Bean manager at
    * initialization time.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleWebBeanWithNonSerializableImplementationClassFails()
   {
      registerBeans(new Class<?>[] { Hamina.class });
      manager.validate();
   }

   /**
    * Simple Web Beans are not required to be serializable. If a simple Web Bean
    * declares a passivating scope, and the implementation class is not
    * serializable, a DefinitionException is thrown by the Web Bean manager at
    * initialization time.
    */
   @Test(groups = {"contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testSimpleWebBeanWithSerializableImplementationClassOK()
   {
      SimpleBean<Jyvaskyla> bean = BeanFactory.createSimpleBean(Jyvaskyla.class, manager);
      assert true;
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testIsSessionScopePassivating()
   {
      assert MetaDataCache.instance().getScopeModel(SessionScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testIsConversationScopePassivating()
   {
      assert MetaDataCache.instance().getScopeModel(ConversationScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testIsApplicationScopeNonPassivating()
   {
      assert !MetaDataCache.instance().getScopeModel(ApplicationScoped.class).isPassivating();
   }

   /**
    * The built-in session and conversation scopes are passivating. No other
    * built-in scope is passivating.
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testIsRequestScopeNonPassivating()
   {
      assert !MetaDataCache.instance().getScopeModel(RequestScoped.class).isPassivating();
   }

   /**
    * the Web Bean declares a passivating scope type, and context passivation
    * occurs, or
    * 
    * @throws IOException
    * @throws ClassNotFoundException
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testSimpleWebBeanDeclaringPassivatingScopeIsSerializedWhenContextIsPassivated() throws IOException, ClassNotFoundException
   {
      SimpleBean<Jyvaskyla> bean = BeanFactory.createSimpleBean(Jyvaskyla.class, manager);
      assert testSerialize(bean);
   }

   @SuppressWarnings("unchecked")
   private <T> boolean testSerialize(Bean<T> bean) throws IOException, ClassNotFoundException
   {
      manager.addBean(bean);
      T instance = manager.getInstance(bean);
      byte[] data = serialize(instance);
      T resurrected = (T) deserialize(data);
      return resurrected.getClass().equals(instance.getClass());
   }

   /**
    * the Web Bean is an EJB stateful session bean, and it is passivated by the
    * EJB container.
    * 
    * @throws ClassNotFoundException
    * @throws IOException
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testStatefulEJBIsSerializedWhenPassivatedByEJBContainer() throws IOException, ClassNotFoundException
   {
      EnterpriseBean<Turku> bean = BeanFactory.createEnterpriseBean(Turku.class, manager);
      assert testSerialize(bean);
   }

   /**
    * On the other hand, dependent objects (including interceptors and
    * decorators with scope @Dependent) of a stateful session bean or of a Web
    * Bean with a passivating scope must be serialized and deserialized along
    * with their owner
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentInterceptorsOfStatefulEnterpriseBeanMustBeSerializable()
   {
      EnterpriseBean<Kaarina> bean = BeanFactory.createEnterpriseBean(Kaarina.class, manager);
   }

   /**
    * On the other hand, dependent objects (including interceptors and
    * decorators with scope @Dependent) of a stateful session bean or of a Web
    * Bean with a passivating scope must be serialized and deserialized along
    * with their owner
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentDecoratorsOfStatefulEnterpriseBeanMustBeSerializable()
   {
      EnterpriseBean<Porvoo> bean = BeanFactory.createEnterpriseBean(Porvoo.class, manager);
   }

   /**
    * On the other hand, dependent objects (including interceptors and
    * decorators with scope @Dependent) of a stateful session bean or of a Web
    * Bean with a passivating scope must be serialized and deserialized along
    * with their owner
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentInterceptorsOfWebBeanWithPassivatingScopeMustBeSerializable()
   {
      SimpleBean<Kotka> bean = BeanFactory.createSimpleBean(Kotka.class, manager);
   }

   /**
    * On the other hand, dependent objects (including interceptors and
    * decorators with scope @Dependent) of a stateful session bean or of a Web
    * Bean with a passivating scope must be serialized and deserialized along
    * with their owner
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentDecoratorsOfWebBeansWithPassivatingScopeMustBeSerializable()
   {
      SimpleBean<Raisio> bean = BeanFactory.createSimpleBean(Raisio.class, manager);
   }

   /**
    * EJB local objects are serializable. Therefore, any reference to an
    * enterprise Web Bean of scope @Dependent is serializable.
    * 
    * @throws ClassNotFoundException
    * @throws IOException
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testDependentEJBsAreSerializable() throws IOException, ClassNotFoundException
   {
      SimpleBean<Vaasa> bean = BeanFactory.createSimpleBean(Vaasa.class, manager);
      assert testSerialize(bean);
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "stub", "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoStatefulSessionBeanFails()
   {
      manager.addBean(BeanFactory.createSimpleBean(Violation.class, manager));
      EnterpriseBean<Espoo> bean = BeanFactory.createEnterpriseBean(Espoo.class, manager);
      bean.postConstruct(null);
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoNonTransientFieldOfWebBeanWithPassivatingScopeFails()
   {
      registerBeans(new Class<?>[] { Violation.class, Vantaa.class });
      manager.validate();
      // T12 instance = manager.getInstanceByType(T12.class);
      // System.out.println(instance.test());
   }

   private void registerBeans(Class<?>[] classes)
   {
      for (Class<?> clazz : classes)
      {
         if (CurrentManager.rootManager().getEjbDescriptorCache().containsKey(clazz))
         {
            CurrentManager.rootManager().addBean(BeanFactory.createEnterpriseBean(clazz, manager));
         }
         else
         {
            CurrentManager.rootManager().addBean(BeanFactory.createSimpleBean(clazz, manager));
         }
      }
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoTransientFieldOK()
   {
      SimpleBean<Joensuu> bean = BeanFactory.createSimpleBean(Joensuu.class, manager);
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoConstructorParameterOfWebBeanWithPassivatingScopeFails()
   {
      registerBeans(new Class<?>[] { Violation.class, Loviisa.class} );
      manager.validate();
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoInitializerParameterOfWebBeanWithPassivatingScopeFails()
   {
      registerBeans(new Class<?>[] { Violation.class, Forssa.class });
      manager.validate();
   }

   /**
    * If a simple Web Bean of scope @Dependent and a non-serializable
    * implementation class is injected into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * UnserializableDependencyException must be thrown by the Web Bean manager
    * at initialization time.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = UnserializableDependencyException.class)
   @SpecAssertion(section = "9.5")
   public void testSimpleDependentWebBeanWithNonSerializableImplementationInjectedIntoProducerMethodParameterWithPassivatingScopeFails()
   {
      manager.addBean(BeanFactory.createSimpleBean(Violation.class, manager));
      Bean<?> producerBean = registerProducerBean(Peraseinajoki.class, "create", Violation2.class);
      manager.validate();
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    * 
    * @throws NoSuchMethodException
    * @throws SecurityException
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoStatefulEnterpriseBeanFails() throws SecurityException, NoSuchMethodException
   {
      registerProducerBean(CityProducer2.class, "create", Violation.class);
      EnterpriseBean<Maarianhamina> ejb = BeanFactory.createEnterpriseBean(Maarianhamina.class, manager);
      ejb.postConstruct(null);
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoNonTransientFieldOfWebBeanWithPassivatingScopeFails()
   {
      registerProducerBean(CityProducer2.class, "create", Violation.class);
      getInstance(Nokia.class).ping();
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoTransientFieldOfWebBeanWithPassivatingScopeOK()
   {
      SimpleBean<CityProducer2> bean = BeanFactory.createSimpleBean(CityProducer2.class, manager);
      SimpleBean<Hyvinkaa> ejb = BeanFactory.createSimpleBean(Hyvinkaa.class, manager);
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoConstructorParameterOfWebBeanWithPassivatingScopeFails()
   {
      registerProducerBean(CityProducer2.class, "create", Violation.class);
      getInstance(Loviisa.class).ping();
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoInitializerParameterOfWebBeanWithPassivatingScopeFails()
   {
      registerProducerBean(CityProducer2.class, "create", Violation.class);
      getInstance(Kuopio.class).ping();
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerMethodReturnsNonSerializableObjectForInjectionIntoProducerMethodParameterWithPassivatingScopeFails()
   {
      registerProducerBean(CityProducer3.class, "create", Violation.class);
      Bean<?> producerBean = registerProducerBean(Jamsa.class, "create", Violation.class);
      Jamsa producerInstance = (Jamsa)producerBean.create();
      producerInstance.ping();
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    * 
    * @throws NoSuchFieldException
    * @throws SecurityException
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerFieldReturnsNonSerializableObjectForInjectionIntoStatefulSessionBeanFails() throws SecurityException, NoSuchFieldException
   {
      registerProducerBean(CityProducer.class, "reference", Violation.class);
      EnterpriseBean<Pietarsaari> bean = BeanFactory.createEnterpriseBean(Pietarsaari.class, manager);
      // TODO: hack
      bean.postConstruct(null);
      assert true;
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerFieldReturnsNonSerializableObjectForInjectionIntoNonTransientFieldOfWebBeanWithPassivatingScopeFails()
   {
      registerProducerBean(CityProducer.class, "reference", Violation.class);
      getInstance(Uusikaupunki.class).ping();
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" })
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerFieldReturnsNonSerializableObjectForInjectionIntoTransientFieldOfWebBeanWithPassivatingScopeOK()
   {
      registerProducerBean(CityProducer.class, "reference", Violation.class);
      getInstance(Salo.class).ping();
      assert true;
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerFieldReturnsNonSerializableObjectForInjectionIntoConstructorParameterOfWebBeanWithPassivatingScopeFails()
   {
      registerProducerBean(CityProducer.class, "reference", Violation.class);
      getInstance(Loviisa.class).ping();
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerFieldReturnsNonSerializableObjectForInjectionIntoInitializerParameterOfWebBeanWithPassivatingScopeFails()
   {
      registerProducerBean(CityProducer.class, "reference", Violation.class);
      getInstance(Mikkeli.class).ping();
   }

   private <T> T getInstance(Class<T> clazz)
   {
      Bean<T> bean = null;
      if (CurrentManager.rootManager().getEjbDescriptorCache().containsKey(clazz))
      {
         bean = BeanFactory.createEnterpriseBean(clazz, manager);
      }
      else
      {
         bean = BeanFactory.createSimpleBean(clazz, manager);
      }
      manager.addBean(bean);
      return manager.getInstance(bean);
   }

   private boolean hasField(Class<?> clazz, String name)
   {
      try
      {
         Field field = clazz.getDeclaredField(name);
      }
      catch (NoSuchFieldException e)
      {
         return false;
      }
      return true;
   }

   private Method getMethod(Class<?> clazz, String name)
   {
      for (Method method : clazz.getDeclaredMethods())
      {
         if (method.getName().equals(name))
         {
            return method;
         }
      }
      return null;
   }

   private AbstractProducerBean<?, ?> registerProducerBean(Class<?> producerBeanClass, String fieldOrMethodName, Class<?> productClass)
   {
      SimpleBean<?> producerContainerBean = BeanFactory.createSimpleBean(producerBeanClass, manager);
      manager.addBean(producerContainerBean);
      AbstractProducerBean<?, ?> producerBean = null;
      try
      {
         if (hasField(producerBeanClass, fieldOrMethodName))
         {
            Field producerField = producerBeanClass.getDeclaredField(fieldOrMethodName);
            producerBean = BeanFactory.createProducerFieldBean(productClass, producerField, producerContainerBean, manager);
         }
         else
         {
            Method producerMethod = getMethod(producerBeanClass, fieldOrMethodName);
            producerBean = BeanFactory.createProducerMethodBean(productClass, producerMethod, producerContainerBean, manager);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not initialize producer bean", e);
      }
      manager.addBean(producerBean);
      return producerBean;
   }

   /**
    * If a producer method or field of scope @Dependent returns a
    * non-serializable object for injection into a stateful session bean, into a
    * non-transient field, Web Bean constructor parameter or initializer method
    * parameter of a Web Bean which declares a passivating scope type, or into a
    * parameter of a producer method which declares a passivating scope type, an
    * IllegalProductException is thrown by the Web Bean manager.
    */
   @Test(groups = { "contexts", "passivation" }, expectedExceptions = IllegalProductException.class)
   @SpecAssertion(section = "9.5")
   public void testDependentScopedProducerFieldReturnsNonSerializableObjectForInjectionIntoProducerMethodParameterWithPassivatingScopeFails()
   {
      registerProducerBean(CityProducer.class, "reference", Violation.class);
      Bean<?> producerBean = registerProducerBean(Jamsa.class, "create", Violation.class);
      Jamsa producerInstance = (Jamsa)producerBean.create();
      producerInstance.ping();

   }

   /**
    * The Web Bean manager must guarantee that JMS endpoint proxy objects are
    * serializable.
    */
   @Test(groups = { "stub", "contexts", "passivation", "jms" })
   @SpecAssertion(section = "9.5")
   public void testJMSEndpointProxyIsSerializable()
   {
      assert false;
   }
}
