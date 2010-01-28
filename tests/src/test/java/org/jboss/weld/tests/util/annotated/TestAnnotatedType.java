package org.jboss.weld.tests.util.annotated;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * AnnotatedType implementation for adding beans in the BeforeBeanDiscovery
 * event
 * 
 * @author Stuart Douglas
 * 
 */
class TestAnnotatedType<X> extends AbstractTestAnnotatedElement implements AnnotatedType<X>
{

   private final Set<AnnotatedConstructor<X>> constructors;
   private final Set<AnnotatedField<? super X>> fields;
   private final Set<AnnotatedMethod<? super X>> methods;

   private final Class<X> javaClass;

   TestAnnotatedType(Class<X> clazz, TestAnnotationStore typeAnnotations, Map<Field, TestAnnotationStore> fieldAnnotations, Map<Method, TestAnnotationStore> methodAnnotations, Map<Method, Map<Integer, TestAnnotationStore>> methodParameterAnnotations, Map<Constructor<X>, TestAnnotationStore> constructorAnnotations, Map<Constructor<X>, Map<Integer, TestAnnotationStore>> constructorParameterAnnotations)
   {
      super(clazz, typeAnnotations);
      this.javaClass = clazz;
      this.constructors = new HashSet<AnnotatedConstructor<X>>();
      for (Constructor<?> c : clazz.getConstructors())
      {
         TestAnnotatedConstructor<X> nc = new TestAnnotatedConstructor<X>(this, c, constructorAnnotations.get(c), constructorParameterAnnotations.get(c));
         constructors.add(nc);
      }
      this.methods = new HashSet<AnnotatedMethod<? super X>>();
      for (Method m : clazz.getMethods())
      {
         TestAnnotatedMethod<X> met = new TestAnnotatedMethod<X>(this, m, methodAnnotations.get(m), methodParameterAnnotations.get(m));
         methods.add(met);
      }
      this.fields = new HashSet<AnnotatedField<? super X>>();
      for (Field f : clazz.getFields())
      {
         TestAnnotatedField<X> b = new TestAnnotatedField<X>(this, f, fieldAnnotations.get(f));
         fields.add(b);
      }
   }

   public Set<AnnotatedConstructor<X>> getConstructors()
   {
      return Collections.unmodifiableSet(constructors);
   }

   public Set<AnnotatedField<? super X>> getFields()
   {
      return Collections.unmodifiableSet(fields);
   }

   public Class<X> getJavaClass()
   {
      return javaClass;
   }

   public Set<AnnotatedMethod<? super X>> getMethods()
   {
      return Collections.unmodifiableSet(methods);
   }

}
