package org.jboss.webbeans.test.unit.implementation;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Stereotype;
import javax.inject.DeploymentType;
import javax.inject.Production;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.testng.annotations.Test;

public class ClassAnnotatedItemTest
{
   
   @Test
   public void testDeclaredAnnotations()
   {
      AnnotatedClass<Order> annotatedElement = AnnotatedClassImpl.of(Order.class);
      assert annotatedElement.getAnnotationsAsSet().size() == 1;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getType().equals(Order.class);
   }
   
   @Test
   public void testMetaAnnotations()
   {
      AnnotatedClass<Order> annotatedElement = AnnotatedClassImpl.of(Order.class);
      Set<Annotation> annotations = annotatedElement.getMetaAnnotations(DeploymentType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Production.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      AnnotatedClass<Order> annotatedElement = AnnotatedClassImpl.of(Order.class);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getMetaAnnotations(Stereotype.class).size() == 0;
      AnnotatedClass<Antelope> classWithNoAnnotations = AnnotatedClassImpl.of(Antelope.class);
      assert classWithNoAnnotations.getAnnotationsAsSet().size() == 0;
   }
   
}
