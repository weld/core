package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.lang.reflect.Method;

import javax.webbeans.IllegalProductException;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.broken.BrokenSpiderProducer;
import org.testng.annotations.Test;

public class ProducerMethodBeanLifecycleTest extends AbstractTest
{
   
   @Test(groups="producerMethod") @SpecAssertion(section="5.6")
   public void testProducerMethodBeanCreate() throws Exception
   {
      SimpleBean<SpiderProducer> spiderProducer = createSimpleBean(SpiderProducer.class); 
      manager.addBean(spiderProducer);
      Method method = SpiderProducer.class.getMethod("produceTarantula");
      ProducerMethodBean<Tarantula> tarantulaBean = createProducerMethodBean(Tarantula.class, method, spiderProducer);
      Tarantula tarantula = tarantulaBean.create();
      assert tarantula != null;
   }
   
   @Test(groups={"stub", "specialization"}) @SpecAssertion(section="3.3.3")
   public void testSpecializedBeanAlwaysUsed()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"stub", "disposalMethod", "beanLifecycle"}) @SpecAssertion(section="3.3.4")
   public void testDisposalMethodCalled()
   {
      // TODO Placeholder
      assert false;
   }
   
   @Test(groups={"stub", "disposalMethod", "beanLifecycle"}) @SpecAssertion(section="3.3.4")
   public void testDisposalMethodHasParametersInjected()
   {
      // TODO Placeholder
      assert false;
   }
   
   
   @Test(groups="producerMethod") @SpecAssertion(section={"3.4", "5.6", "8.3"})
   public void testProducerMethodReturnsNullIsDependent() throws Exception
   {
      SimpleBean<SpiderProducer> spiderProducer = createSimpleBean(SpiderProducer.class); 
      manager.addBean(spiderProducer);
      Method method = SpiderProducer.class.getMethod("getNullSpider");
      ProducerMethodBean<Spider> spiderBean = createProducerMethodBean(Spider.class, method, spiderProducer);
      Spider spider = spiderBean.create();
      assert spider == null;
   }
   
   @Test(groups="producerMethod", expectedExceptions=IllegalProductException.class) @SpecAssertion(section={"3.4", "5.6"})
   public void testProducerMethodReturnsNullIsNotDependent() throws Exception
   {
      SimpleBean<BrokenSpiderProducer> spiderProducer = createSimpleBean(BrokenSpiderProducer.class);
      manager.addBean(spiderProducer);
      Method method = BrokenSpiderProducer.class.getMethod("getRequestScopedSpider");
      createProducerMethodBean(Spider.class, method, spiderProducer).create();
   }
   
}
