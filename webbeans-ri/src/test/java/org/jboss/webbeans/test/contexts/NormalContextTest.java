package org.jboss.webbeans.test.contexts;

import static org.jboss.webbeans.util.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.lang.reflect.Method;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.contexts.AbstractContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.util.BeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * @author Pete Muir
 * 
 * This class tests a basic context against section 8 of the specification
 *
 */
@SpecVersion("PDR")
public class NormalContextTest extends AbstractTest
{
   Context context;
   
   @BeforeMethod
   public void initContext() {
      context = new RequestContext() {};
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.1")
   public void testGetWithCreateFalseReturnsNull() {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class);      
      assert context.get(tunaBean, false) == null;
   }

   @Test(groups="contexts") @SpecAssertion(section="8.1")
   public void testGetWithCreateTrueReturnsBean() {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class);      
      assert context.get(tunaBean, true) != null;
   }
   
   @Test(groups="contexts", expectedExceptions=ContextNotActiveException.class) @SpecAssertion(section="8.1")
   public void testInactiveContextThrowsContextNotActiveException() {
      ((AbstractContext)context).setActive(false);
      context.get(null, false);
      assert true;
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.1")
   public void testReturnsCorrectExistingBean() {
      Bean<Tuna> tunaBean = BeanFactory.createSimpleBean(Tuna.class);      
      Tuna firstTuna = context.get(tunaBean, true);
      Tuna secondTuna = context.get(tunaBean, false);
      assert firstTuna == secondTuna;
   }

   @Test(groups={"contexts", "producerMethod"}) @SpecAssertion(section="8.1")
   public void testProducerMethodReturningNullOK() throws SecurityException, NoSuchMethodException {
      SimpleBean<SpiderProducer> producer = createSimpleBean(SpiderProducer.class);
      manager.addBean(producer);
      Method nullProducer = SpiderProducer.class.getMethod("produceShelob");  
      ProducerMethodBean<Tarantula> shelobBean = createProducerMethodBean(Tarantula.class, nullProducer, producer);
      assert shelobBean.create() == null;
   }
   
}
