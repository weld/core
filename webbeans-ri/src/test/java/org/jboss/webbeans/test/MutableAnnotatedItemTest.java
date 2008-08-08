package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.Current;

import org.jboss.webbeans.bindings.CurrentBinding;
import org.jboss.webbeans.util.MutableAnnotatedItem;
import org.junit.Test;

public class MutableAnnotatedItemTest
{
   
   @Test
   public void testMutability()
   {
      MutableAnnotatedItem annotatedElement = new MutableAnnotatedItem(null, new HashMap<Class<? extends Annotation>, Annotation>());
      assert annotatedElement.getAnnotations().size() == 0;
      annotatedElement.add(new CurrentBinding());
      assert annotatedElement.getAnnotations().size() == 1;
      assert annotatedElement.getAnnotation(Current.class) != null;
   }
   
   @Test
   public void testMutableMetaAnnotations()
   {
      MutableAnnotatedItem annotatedElement = new MutableAnnotatedItem(null, new HashMap<Class<? extends Annotation>, Annotation>());
      annotatedElement.add(new CurrentBinding());
      Set<Annotation> annotations = annotatedElement.getAnnotations(BindingType.class);
      assert annotations.size() == 1;
      Iterator<Annotation> it = annotations.iterator();
      Annotation production = it.next();
      assert Current.class.equals(production.annotationType());
   }
   
}
