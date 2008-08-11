package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.webbeans.DeploymentType;
import javax.webbeans.Production;
import javax.webbeans.Stereotype;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.SimpleAnnotatedType;
import org.jboss.webbeans.test.components.Antelope;
import org.jboss.webbeans.test.components.Order;
import org.junit.Test;

public class ClassAnnotatedItemTest
{
   
   @Test
   public void testDeclaredAnnotations()
   {
      AnnotatedType annotatedElement = new SimpleAnnotatedType(Order.class);
      assert annotatedElement.getAnnotations().size() == 1;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getAnnotatedClass().equals(Order.class);
   }
   
   @Test
   public void testMetaAnnotations()
   {
      AnnotatedItem annotatedElement = new SimpleAnnotatedType(Order.class);
      Set<Annotation> annotations = annotatedElement.getAnnotations(DeploymentType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Production.class.equals(production.annotationType());
   }
   
   @Test
   public void testEmpty()
   {
      AnnotatedItem annotatedElement = new SimpleAnnotatedType(Order.class);
      assert annotatedElement.getAnnotation(Stereotype.class) == null;
      assert annotatedElement.getAnnotations(Stereotype.class).size() == 0;
      AnnotatedItem classWithNoAnnotations = new SimpleAnnotatedType(Antelope.class);
      assert classWithNoAnnotations.getAnnotations().size() == 0;
   }
   
}
