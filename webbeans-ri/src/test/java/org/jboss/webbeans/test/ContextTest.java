package org.jboss.webbeans.test;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.BasicContext;
import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.SimpleComponentModel;
import org.jboss.webbeans.test.components.Tuna;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * 
 * This class tests a basic context against section 8 of the specification
 *
 */
@SpecVersion("20081020")
public class ContextTest extends AbstractTest
{
   Context context;
   
   @BeforeMethod
   public void initContext() {
      context = new BasicContext(RequestScoped.class);
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.1")
   public void testGetWithCreateFalseReturnsNull() {
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedItem(Tuna.class), super.manager), manager);      
      assert context.get(tunaBean, false) == null;
   }

   @Test(groups="contexts") @SpecAssertion(section="8.1")
   public void testGetWithCreateTrueReturnsBean() {
      Bean<Tuna> tunaBean = new BeanImpl<Tuna>(new SimpleComponentModel<Tuna>(new SimpleAnnotatedType<Tuna>(Tuna.class), getEmptyAnnotatedItem(Tuna.class), super.manager), manager);
      assert context.get(tunaBean, true) != null;
   }
   
   @Test(groups="contexts", expectedExceptions=ContextNotActiveException.class) @SpecAssertion(section="8.1")
   public void testInactiveContextThrowsContextNotActiveException() {
      ((BasicContext)context).setActive(false);
      context.get(null, false);
      assert true;
   }

}
