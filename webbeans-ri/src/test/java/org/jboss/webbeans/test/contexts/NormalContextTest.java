package org.jboss.webbeans.test.contexts;

import javax.webbeans.manager.Context;

import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 *         This class tests a basic context against section 8 of the
 *         specification
 * 
 */
@SpecVersion("20081206")
public class NormalContextTest extends AbstractTest
{
   Context context;

   @BeforeMethod
   public void initContext()
   {
      context = new RequestContext()
      {
      };
   }

   /**
    * return an existing instance of the given contextual type, or
    */
   @Test(groups = { "contexts", "stub" })
   @SpecAssertion(section = "9.2")
   public void testGetReturnsExistingInstace()
   {
      assert false;
   }

   /**
    * if the value of the create parameter is false, return a null value, or
    */
   @Test(groups = { "contexts", "stub" })
   @SpecAssertion(section = "9.2")
   public void testGetWithCreateFalseReturnsNull()
   {
      assert false;
   }

   /**
    * if the value of the create parameter is true, create a new instance of the
    * given contextual type by calling Bean.create() and return the new
    * instance.
    */
   @Test(groups = { "contexts", "stub" })
   @SpecAssertion(section = "9.2")
   public void testGetWithCreateTrueReturnsNewInstance()
   {
      assert false;
   }

   /**
    * The get() method may not return a null value unless the create parameter
    * is false or Contextual.create() returns a null value
    */
   @Test(groups = { "contexts", "stub" })
   @SpecAssertion(section = "9.2")
   public void testGetMayNotReturnNullUnlessCreateIsFalseOrContextualCreateReturnsNull()
   {
      assert false;
   }

   /**
    * The get() method may not create a new instance of the given contextual
    * type unless the create parameter is true
    */
   @Test(groups = { "contexts", "stub" })
   @SpecAssertion(section = "9.2")
   public void testGetMayNotCreateNewInstanceUnlessCreateIsTrue()
   {
      assert false;
   }

   /**
    * The Context implementation is responsible for destroying any contextual
    * instance it creates by passing the instance to the destroy() method of the
    * Contextual object representing the contextual type.
    */
   public void testContextDestroysBeansWhenDestroyed()
   {
      assert false;
   }

   /**
    * A destroyed instance must not subsequently be returned by the get()
    * method.
    */
   @Test(groups = { "contexts", "stub" })
   @SpecAssertion(section = "9.2")
   public void testDestroyedInstanceMustNotBeReturnedByGet()
   {
      assert false;
   }

   /**
    * When a scope is inactive, any invocation of the get() from the current
    * thread upon the Context object for that scope results in a
    * ContextNotActiveException.
    */
   @Test(groups = { "contexts", "stub" })
   @SpecAssertion(section = "9.2")
   public void testInvokingGetOnInactiveContextFails()
   {
      assert false;
   }

   /**
    * There may be no more than one mapped instance per contextual type per
    * thread
    */
   @Test(groups = { "contexts", "stub" })
   @SpecAssertion(section = "9.3")
   public void testOnlyMappedInstancePerContextualTypePerThread()
   {
      assert false;
   }

   /*
    * @Test(groups = "contexts")
    * 
    * @SpecAssertion(section = "9.3") public void
    * testGetWithCreateFalseReturnsNull() { Bean<Tuna> tunaBean =
    * BeanFactory.createSimpleBean(Tuna.class, manager); assert
    * context.get(tunaBean, false) == null; }
    * 
    * @Test(groups = "contexts")
    * 
    * @SpecAssertion(section = "8.1") public void
    * testGetWithCreateTrueReturnsBean() { Bean<Tuna> tunaBean =
    * BeanFactory.createSimpleBean(Tuna.class, manager); assert
    * context.get(tunaBean, true) != null; }
    * 
    * @Test(groups = "contexts", expectedExceptions =
    * ContextNotActiveException.class)
    * 
    * @SpecAssertion(section = "8.1") public void
    * testInactiveContextThrowsContextNotActiveException() { ((AbstractContext)
    * context).setActive(false); context.get(null, false); assert true; }
    * 
    * @Test(groups = "contexts")
    * 
    * @SpecAssertion(section = "8.1") public void
    * testReturnsCorrectExistingBean() { Bean<Tuna> tunaBean =
    * BeanFactory.createSimpleBean(Tuna.class, manager); Tuna firstTuna =
    * context.get(tunaBean, true); Tuna secondTuna = context.get(tunaBean,
    * false); assert firstTuna == secondTuna; }
    * 
    * @Test(groups = { "contexts", "producerMethod" })
    * 
    * @SpecAssertion(section = "8.1") public void
    * testProducerMethodReturningNullOK() throws SecurityException,
    * NoSuchMethodException { SimpleBean<SpiderProducer> producer =
    * createSimpleBean(SpiderProducer.class, manager);
    * manager.addBean(producer); Method nullProducer =
    * SpiderProducer.class.getMethod("produceShelob");
    * ProducerMethodBean<Tarantula> shelobBean =
    * createProducerMethodBean(Tarantula.class, nullProducer, producer,
    * manager); assert shelobBean.create() == null; }
    */
}
