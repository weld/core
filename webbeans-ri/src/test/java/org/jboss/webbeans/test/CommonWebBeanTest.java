package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleModel;

import javax.webbeans.Production;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.model.bean.BeanModel;
import org.jboss.webbeans.test.beans.RedSnapper;
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
   public void testIsNullable()
   {
      assert false;
   }
	
}
