package org.jboss.webbeans.test;

import java.lang.reflect.Method;

import javax.webbeans.Production;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.test.beans.Animal;
import org.jboss.webbeans.test.beans.DeadlyAnimal;
import org.jboss.webbeans.test.beans.DeadlySpider;
import org.jboss.webbeans.test.beans.DependentFinalTuna;
import org.jboss.webbeans.test.beans.RedSnapper;
import org.jboss.webbeans.test.beans.Spider;
import org.jboss.webbeans.test.beans.SpiderProducer;
import org.jboss.webbeans.test.beans.Tarantula;
import org.testng.annotations.Test;

/**
 * This test class should be used for common assertions about Web Beans
 * 
 * @author Pete Muir
 *
 */
@SpecVersion("20081206")
public class CommonWebBeanTest extends AbstractTest 
{

   // TODO This should actually somehow test the reverse - that the container throws a definition exception if any of these occur
   
	@Test @SpecAssertion(section="2")
	public void testApiTypesNonEmpty()
	{
	   Bean<?> model = SimpleBean.of(RedSnapper.class, manager);
      assert model.getTypes().size() > 0;
	}
	
	@Test @SpecAssertion(section="2")
	public void testBindingTypesNonEmpty()
	{
	   Bean<?> model = SimpleBean.of(RedSnapper.class, manager);
      assert model.getBindingTypes().size() > 0;
	}
	
	@Test @SpecAssertion(section="2")
	public void testHasScopeType()
	{
	   Bean<?> model = SimpleBean.of(RedSnapper.class, manager);
      assert model.getScopeType().equals(RequestScoped.class);
	}
	
	@Test @SpecAssertion(section="2")
	public void testHasDeploymentType()
	{
		Bean<?> model = SimpleBean.of(RedSnapper.class, manager);
		assert model.getDeploymentType().equals(Production.class);
	}
	
	@Test(groups="producerMethod") @SpecAssertion(section="5.2")
   public void testIsNullable() throws Exception
   {
	   SimpleBean<SpiderProducer> spiderProducerBean = SimpleBean.of(SpiderProducer.class, manager);
	   manager.addBean(spiderProducerBean);
      Method method = SpiderProducer.class.getMethod("getWolfSpiderSize");
      Bean<Integer> bean = ProducerMethodBean.of(method, spiderProducerBean, manager);
      assert !bean.isNullable();
      method = SpiderProducer.class.getMethod("makeASpider");
      Bean<Spider> spiderBean = ProducerMethodBean.of(method, spiderProducerBean, manager);
      assert spiderBean.isNullable();
   }
	
   
   @Test @SpecAssertion(section={"3.2.2", "2.2"})
   public void testApiTypes()
   {
      Bean<Tarantula> bean = SimpleBean.of(Tarantula.class, manager);
      assert bean.getTypes().size() == 6;
      assert bean.getTypes().contains(Tarantula.class);
      assert bean.getTypes().contains(Spider.class);
      assert bean.getTypes().contains(Animal.class);
      assert bean.getTypes().contains(Object.class);
      assert bean.getTypes().contains(DeadlySpider.class);
      assert bean.getTypes().contains(DeadlyAnimal.class);
   }
   
   @Test @SpecAssertion(section="2.2")
   public void testFinalApiType()
   {
      SimpleBean.of(DependentFinalTuna.class, manager);
   }
	
}
