package org.jboss.weld.tests.util.annotated;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * 
 * @author Stuart Douglas
 * 
 */
abstract class AbstractTestAnnotatedCallable<X, Y extends Member> extends AbstractTestAnnotatedMember<X, Y> implements AnnotatedCallable<X>
{

   private final List<AnnotatedParameter<X>> parameters;

   protected AbstractTestAnnotatedCallable(AnnotatedType<X> declaringType, Y member, Class<?> memberType, Class<?>[] parameterTypes, TestAnnotationStore annotations, Map<Integer, TestAnnotationStore> parameterAnnotations)
   {
      super(declaringType, member, memberType, annotations);
      this.parameters = getAnnotatedParameters(this, parameterTypes, parameterAnnotations);
   }

   public List<AnnotatedParameter<X>> getParameters()
   {
      return Collections.unmodifiableList(parameters);
   }

   public AnnotatedParameter<X> getParameter(int index)
   {
      return parameters.get(index);

   }  
   
   private static <X, Y extends Member> List<AnnotatedParameter<X>> getAnnotatedParameters(AbstractTestAnnotatedCallable<X, Y> callable, Class<?>[] parameterTypes, Map<Integer, TestAnnotationStore> parameterAnnotations)
   {
      List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>();
      int len = parameterTypes.length;
      for (int i = 0; i < len; ++i)
      {
         TestAnnotationBuilder builder = new TestAnnotationBuilder();
         if (parameterAnnotations != null && parameterAnnotations.containsKey(i))
         {
            builder.addAll(parameterAnnotations.get(i));
         }
         TestAnnotatedParameter<X> p = new TestAnnotatedParameter<X>(callable, parameterTypes[i], i, builder.create());
         parameters.add(p);
      }
      return parameters;
   }

}
