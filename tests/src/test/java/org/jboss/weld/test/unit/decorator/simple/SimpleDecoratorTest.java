package org.jboss.weld.test.unit.decorator.simple;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.jsr299.BeansXml;
import org.jboss.weld.test.AbstractWebBeansTest;
import org.testng.annotations.Test;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@Artifact
@BeansXml("beans.xml")
public class SimpleDecoratorTest extends AbstractWebBeansTest
{
   @Test
   public void testSimpleDecorator()
   {
      SimpleBean simpleBean = getCurrentManager().getInstanceByType(SimpleBean.class);
      
      resetDecorators();
      assert simpleBean.echo1(1) == 1;
      assertDecorators(true, false, false);
      
      resetDecorators();
      assert simpleBean.echo2(2) == 2;
      assertDecorators(false, true, false);

      //Only SimpleDecorator1 gets invoked, although I think SimpleDecorator2 should get invoked too
      resetDecorators();
      assert simpleBean.echo3(3) == 3;
      assertDecorators(false, false, true);
      
      resetDecorators();
      assert simpleBean.echo4(4) == 4; 
      assertDecorators(false, false, false);
   }
   
   private void resetDecorators()
   {
      SimpleDecorator1.reset();
      SimpleDecorator2.reset();
   }
   
   private void assertDecorators(boolean echo1, boolean echo2, boolean echo3)
   {
      assert SimpleDecorator1.echo1 == echo1;
      assert SimpleDecorator1.echo3 == echo3;
      assert SimpleDecorator2.echo2 == echo2;
      assert SimpleDecorator2.echo3 == echo3;
   }
}

