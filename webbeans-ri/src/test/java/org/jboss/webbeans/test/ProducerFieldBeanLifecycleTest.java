package org.jboss.webbeans.test;

import static org.jboss.webbeans.util.BeanFactory.createProducerFieldBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.lang.reflect.Field;

import javax.webbeans.IllegalProductException;

import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.beans.OtherSpiderProducer;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.Tarantula;
import org.jboss.webbeans.test.beans.broken.OtherBrokenSpiderProducer;
import org.testng.annotations.Test;

public class ProducerFieldBeanLifecycleTest extends AbstractTest
{
   
   @Test(groups="producerField") @SpecAssertion(section="5.6")
   public void testProducerFieldBeanCreate() throws Exception
   {
      SimpleBean<OtherSpiderProducer> spiderProducer = createSimpleBean(OtherSpiderProducer.class); 
      manager.addBean(spiderProducer);
      Field method = OtherSpiderProducer.class.getField("produceTarantula");
      ProducerFieldBean<Tarantula> tarantulaBean = createProducerFieldBean(Tarantula.class, method, spiderProducer);
      Tarantula tarantula = tarantulaBean.create();
      assert tarantula != null;
   }
   
   @Test(groups={"stub", "specialization"}) @SpecAssertion(section="3.3.3")
   public void testSpecializedBeanAlwaysUsed()
   {
      // TODO Placeholder
      assert false;
   }
   
   
   @Test(groups="producerField") @SpecAssertion(section={"3.5", "5.6", "8.3"})
   public void testProducerFieldReturnsNullIsDependent() throws Exception
   {
      SimpleBean<OtherSpiderProducer> spiderProducer = createSimpleBean(OtherSpiderProducer.class); 
      manager.addBean(spiderProducer);
      Field method = OtherSpiderProducer.class.getField("getNullSpider");
      ProducerFieldBean<Spider> spiderBean = createProducerFieldBean(Spider.class, method, spiderProducer);
      Spider spider = spiderBean.create();
      assert spider == null;
   }
   
   @Test(groups="producerField", expectedExceptions=IllegalProductException.class) @SpecAssertion(section={"3.5", "5.6"})
   public void testProducerFieldReturnsNullIsNotDependent() throws Exception
   {
      SimpleBean<OtherBrokenSpiderProducer> spiderProducer = createSimpleBean(OtherBrokenSpiderProducer.class);
      manager.addBean(spiderProducer);
      Field method = OtherBrokenSpiderProducer.class.getField("getRequestScopedSpider");
      createProducerFieldBean(Spider.class, method, spiderProducer).create();
   }
   
}
