package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.test.components.ClassWithNoAnnotations;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.util.AnnotatedItem;
import org.jboss.webbeans.util.ClassAnnotatedItem;
import org.junit.Test;

public class ClassAnnotatedItemTest
{
   
   @Test
   public void testDeclaredAnnotations()
   {
      AnnotatedItem annotatedElement = new ClassAnnotatedItem(Order.class);
      assert annotatedElement.getAnnotations().size() == 2;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getAnnotation(Named.class) != null;
      System.out.println(annotatedElement.getAnnotatedClass());
      assert annotatedElement.getAnnotatedClass().equals(Order.class);
   }
   
   @Test
   public void testMetaAnnotations()
   {
      AnnotatedItem annotatedElement = new ClassAnnotatedItem(Order.class);
      Set<Annotation> annotations = annotatedElement.getAnnotations(DeploymentType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Production.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      AnnotatedItem annotatedElement = new ClassAnnotatedItem(Order.class);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getAnnotations(Stereotype.class).size() == 0;
      AnnotatedItem classWithNoAnnotations = new ClassAnnotatedItem(ClassWithNoAnnotations.class);
      assert classWithNoAnnotations.getAnnotations().size() == 0;
   }
   
}
