package org.jboss.weld.tests.unit.reflection.method;

import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.introspector.jlr.WeldMethodImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.testng.annotations.Test;

public class WeldMethodTest
{
	
   private final ClassTransformer transformer = new ClassTransformer(new TypeStore());
   private final Class<Choice<?, ?>> CHOICE_LITERAL = new TypeLiteral<Choice<?, ?>>() {}.getRawType();
   
   @Test(description = "WELD-221")
   public void testMethodReturnsGenericTypeOfClass() throws Exception
   {
      WeldClass<Choice<?, ?>> clazz = WeldClassImpl.of(CHOICE_LITERAL, transformer);
      WeldMethod<Choice<?, ?>, Choice<?, ?>> method = WeldMethodImpl.of(Choice.class.getMethod("aMethod"), clazz, transformer);
      assert method.getTypeClosure().size() == 3;
   }

}
