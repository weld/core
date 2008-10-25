package org.jboss.webbeans.test;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.BasicContext;
import org.jboss.webbeans.BeanImpl;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.model.bean.SimpleBeanModel;
import org.jboss.webbeans.test.components.Tuna;
import org.jboss.webbeans.test.util.Util;
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
      Bean<Tuna> tunaBean = Util.createSimpleWebBean(Tuna.class, manager);      
      assert context.get(tunaBean, false) == null;
   }

   @Test(groups="contexts") @SpecAssertion(section="8.1")
   public void testGetWithCreateTrueReturnsBean() {
      Bean<Tuna> tunaBean = Util.createSimpleWebBean(Tuna.class, manager);      
      assert context.get(tunaBean, true) != null;
   }
   
   @Test(groups="contexts", expectedExceptions=ContextNotActiveException.class) @SpecAssertion(section="8.1")
   public void testInactiveContextThrowsContextNotActiveException() {
      ((BasicContext)context).setActive(false);
      context.get(null, false);
      assert true;
   }
   
   @Test(groups="contexts") @SpecAssertion(section="8.1")
   public void testReturnsCorrectExistingBean() {
      Bean<Tuna> tunaBean = Util.createSimpleWebBean(Tuna.class, manager);      
      Tuna firstTuna = context.get(tunaBean, true);
      Tuna secondTuna = context.get(tunaBean, false);
      assert firstTuna == secondTuna;
   }

   @Test(groups="contexts") @SpecAssertion(section="8.1")
   public void testProducerMethodReturningNullOK() {
      // TODO
      assert false;
   }

   @Test(groups="contexts")
   public void testRemoveBean() {
      Bean<Tuna> tunaBean = Util.createSimpleWebBean(Tuna.class, manager);      
      Tuna firstTuna = context.get(tunaBean, true);
      ((BasicContext)context).remove(manager, tunaBean);
      assert context.get(tunaBean, false) == null;
   }
}
