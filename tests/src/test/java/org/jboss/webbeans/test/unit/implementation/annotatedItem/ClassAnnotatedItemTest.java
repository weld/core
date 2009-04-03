package org.jboss.webbeans.test.unit.implementation.annotatedItem;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Stereotype;
import javax.inject.DeploymentType;
import javax.inject.Production;

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.test.unit.AbstractWebBeansTest;
import org.testng.annotations.Test;

@Artifact
public class ClassAnnotatedItemTest extends AbstractWebBeansTest
{
	
   private final ClassTransformer transformer = new ClassTransformer();
   
   @Test
   public void testDeclaredAnnotations()
   {
      AnnotatedClass<Order> annotatedElement = AnnotatedClassImpl.of(Order.class, transformer);
      assert annotatedElement.getAnnotationsAsSet().size() == 1;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getRawType().equals(Order.class);
   }
   
   @Test
   public void testMetaAnnotations()
   {
      AnnotatedClass<Order> annotatedElement = AnnotatedClassImpl.of(Order.class, transformer);
      Set<Annotation> annotations = annotatedElement.getMetaAnnotations(DeploymentType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Production.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      AnnotatedClass<Order> annotatedElement = AnnotatedClassImpl.of(Order.class, transformer);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getMetaAnnotations(Stereotype.class).size() == 0;
      AnnotatedClass<Antelope> classWithNoAnnotations = AnnotatedClassImpl.of(Antelope.class, transformer);
      assert classWithNoAnnotations.getAnnotationsAsSet().size() == 0;
   }
   
}
