package org.jboss.weld.test.unit.implementation.annotatedItem.anonymous;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact
public class ClassAnnotatedItemTest extends AbstractWeldTest
{
	
   private final ClassTransformer transformer = new ClassTransformer(new TypeStore());
   
   @Test(groups = "broken")
   public void testNonStaticInnerClassWithGenericTypes()
   {
      AnnotatedType at = WeldClassImpl.of(new Kangaroo().procreate().getClass(), transformer);
      WeldClassImpl.of(at, transformer);
   }

}
