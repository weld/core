package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createProducerMethodBean;
import static org.jboss.webbeans.test.util.Util.createSimpleModel;
import static org.jboss.webbeans.test.util.Util.createSimpleWebBean;

import java.lang.reflect.Method;

import javax.webbeans.Production;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.model.bean.BeanModel;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.testng.annotations.Test;

/**
 * This test class should be used for common assertions about Web Beans
 * 
 * @author Pete Muir
 *
 */
public class CommonWebBeanTest extends AbstractTest 
{

   // TODO This should actually somehow test the reverse - that the container throws a definition exception if any of these occur
   
	@Test @SpecAssertion(section="2")
	public void testApiTypesNonEmpty()
	{
	   BeanModel<?, ?> model = createSimpleModel(RedSnapper.class, manager);
      assert model.getApiTypes().size() > 0;
	}
	
	@Test @SpecAssertion(section="2")
	public void testBindingTypesNonEmpty()
	{
	   BeanModel<?, ?> model = createSimpleModel(RedSnapper.class, manager);
      assert model.getBindingTypes().size() > 0;
	}
	
	@Test @SpecAssertion(section="2")
	public void testHasScopeType()
	{
	   BeanModel<?, ?> model = createSimpleModel(RedSnapper.class, manager);
      assert model.getScopeType().equals(RequestScoped.class);
	}
	
	@Test @SpecAssertion(section="2")
	public void testHasDeploymentType()
	{
		BeanModel<?, ?> model = createSimpleModel(RedSnapper.class, manager);
		assert model.getDeploymentType().equals(Production.class);
	}
	
	@Test(groups="producerMethod") @SpecAssertion(section="4.2")
   public void testIsNullable() throws Exception
   {
	   SimpleBean<SpiderProducer> spiderProducerBean = createSimpleWebBean(SpiderProducer.class, manager);
	   manager.addBean(spiderProducerBean);
      Method method = SpiderProducer.class.getMethod("getWolfSpiderSize");
      Bean<Integer> bean = createProducerMethodBean(int.class, method, manager);
      assert !bean.isNullable();
      method = SpiderProducer.class.getMethod("makeASpider");
      Bean<Spider> spiderBean = createProducerMethodBean(Spider.class, method, manager);
      assert spiderBean.isNullable();
   }
	
}
