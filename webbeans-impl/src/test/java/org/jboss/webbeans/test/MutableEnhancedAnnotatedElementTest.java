package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.webbeans.Current;
import javax.webbeans.DeploymentType;
import javax.webbeans.Named;
import javax.webbeans.Production;
import javax.webbeans.ScopeType;

import org.jboss.webbeans.CurrentBinding;
import org.jboss.webbeans.test.components.Order;
import org.jboss.webbeans.util.EnhancedAnnotatedElement;
import org.jboss.webbeans.util.MutableEnhancedAnnotatedElement;
import org.junit.Test;

public class MutableEnhancedAnnotatedElementTest
{
   
   @Test
   public void testDeclaredAnnotations()
   {
      EnhancedAnnotatedElement annotatedElement = new MutableEnhancedAnnotatedElement(Order.class);
      assert annotatedElement.getAnnotations().size() == 2;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getAnnotation(Named.class) != null;
   }
   
   @Test
   public void testMutability()
   {
      MutableEnhancedAnnotatedElement annotatedElement = new MutableEnhancedAnnotatedElement(Order.class);
      assert annotatedElement.getAnnotations().size() == 2;
      annotatedElement.add(new CurrentBinding());
      assert annotatedElement.getAnnotations().size() == 3;
      assert annotatedElement.getAnnotation(Production.class) != null;
      assert annotatedElement.getAnnotation(Named.class) != null;
      assert annotatedElement.getAnnotation(Current.class) != null;
   }
   
   @Test
   public void testMetaAnnotations()
   {
      EnhancedAnnotatedElement annotatedElement = new MutableEnhancedAnnotatedElement(Order.class);
      Set<Annotation> annotations = annotatedElement.getAnnotations(DeploymentType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Production.class.equals(production.annotationType());
   }
   
   @Test
   public void testMutableMetaAnnotations()
   {
      MutableEnhancedAnnotatedElement annotatedElement = new MutableEnhancedAnnotatedElement(Order.class);
      annotatedElement.add(new CurrentBinding());
      Set<Annotation> annotations = annotatedElement.getAnnotations(ScopeType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Current.class.equals(production.annotationType());
   }
   
}
