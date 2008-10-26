package org.jboss.webbeans.test;

import static org.jboss.webbeans.test.util.Util.createSimpleModel;

import org.jboss.webbeans.model.bean.BeanModel;
import org.jboss.webbeans.test.beans.Bear;
import org.testng.annotations.Test;

@SpecVersion("PDR")
public class ApiTypeTest extends AbstractTest 
{
	
	@Test @SpecAssertion(section="2.2")
	public void testApiTypeContainsObject()
	{
		BeanModel<?, ?> model = createSimpleModel(Bear.class, manager);
		assert model.getApiTypes().contains(Object.class);
	}

}
