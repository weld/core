package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createProducerMethodBean;
import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

import java.lang.reflect.Method;

import javax.webbeans.DefinitionException;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tarantula;
import org.testng.annotations.Test;

public class ProducerMethodBeanLifecycleTest extends AbstractTest
{
   
   @Test(groups="producerMethod") @SpecAssertion(section="5.6")
   public void testProducerMethodBeanCreate() throws Exception
   {
      SimpleBean<SpiderProducer> spiderProducer = createSimpleWebBean(SpiderProducer.class, manager); 
      manager.addBean(spiderProducer);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodBean<Tarantula> tarantulaBean = createProducerMethodBean(Tarantula.class, method, manager, spiderProducer);
      Tarantula tarantula = tarantulaBean.create();
      assert tarantula != null;
   }

   @Test(groups="producerMethod") @SpecAssertion(section="3.3")
   public void testNonDependentProducerMethodThatReturnsNull()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="producerMethod") @SpecAssertion(section="3.3")
   public void testDependentProducerMethodThatReturnsNull()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups="specialization") @SpecAssertion(section="3.3.3")
   public void testSpecializedBeanAlwaysUsed()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"disposalMethod", "beanLifecycle"}) @SpecAssertion(section="3.3.4")
   public void testDisposalMethodCalled()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"disposalMethod", "beanLifecycle"}) @SpecAssertion(section="3.3.4")
   public void testDisposalMethodHasParametersInjected()
   {
      // TODO Placeholder
      assert false;
   }
   
   
   @Test(groups="producerMethod") @SpecAssertion(section={"3.4", "5.6"})
   public void testProducerMethodReturnsNullIsDependent()
   {
      assert false;
   }
   
   @Test(groups="producerMethod", expectedExceptions=DefinitionException.class) @SpecAssertion(section={"3.4", "5.6"})
   public void testProducerMethodReturnsNullIsNotDependent()
   {
      
   }
   
}
