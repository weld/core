package org.jboss.weld.test.unit.implementation.annotatedItem;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Qualifier;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.WeldClassImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

@Artifact(addCurrentPackage = false)
@Classes({Animal.class, Antelope.class, Kangaroo.class, Order.class, Random.class})
public class ClassAnnotatedItemTest extends AbstractWeldTest
{
	
   private final ClassTransformer transformer = new ClassTransformer(new TypeStore());
   
   @Test
   public void testDeclaredAnnotations()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of(Order.class, transformer);
      assert annotatedElement.getAnnotations().size() == 1;
      assert annotatedElement.getAnnotation(Random.class) != null;
      assert annotatedElement.getJavaClass().equals(Order.class);
   }
   
   @Test
   public void testMetaAnnotations()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of(Order.class, transformer);
      Set<Annotation> annotations = annotatedElement.getMetaAnnotations(Qualifier.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Random.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      WeldClass<Order> annotatedElement = WeldClassImpl.of(Order.class, transformer);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getMetaAnnotations(Stereotype.class).size() == 0;
      WeldClass<Antelope> classWithNoAnnotations = WeldClassImpl.of(Antelope.class, transformer);
      assert classWithNoAnnotations.getAnnotations().size() == 0;
   }
   
   @Test(groups = "broken")
   public void testNonStaticInnerClassWithGenericTypes()
   {
      AnnotatedType at = WeldClassImpl.of(new Kangaroo().procreate().getClass(), transformer);
      WeldClassImpl.of(at, transformer);
   }

}
