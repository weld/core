package org.jboss.weld.tests.util.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.inject.spi.AnnotatedType;

/**
 * Class for constructing a new AnnotatedType. A new instance of builder must be
 * used for each annotated type.
 * 
 * No annotations will be read from the underlying class definition, all
 * annotations must be added explicitly
 * 
 * @author Stuart Douglas
 * @author Pete Muir
 * 
 */
public class TestAnnotatedTypeBuilder<X>
{
   private Map<Field, TestAnnotationBuilder> fields = new HashMap<Field, TestAnnotationBuilder>();
   private Map<Method, TestAnnotationBuilder> methods = new HashMap<Method, TestAnnotationBuilder>();
   private Map<Method, Map<Integer, TestAnnotationBuilder>> methodParameters = new HashMap<Method, Map<Integer, TestAnnotationBuilder>>();
   private Map<Constructor<X>, TestAnnotationBuilder> constructors = new HashMap<Constructor<X>, TestAnnotationBuilder>();
   private Map<Constructor<X>, Map<Integer, TestAnnotationBuilder>> constructorParameters = new HashMap<Constructor<X>, Map<Integer, TestAnnotationBuilder>>();
   private TestAnnotationBuilder typeAnnotations = new TestAnnotationBuilder();
   private Class<X> underlying;

   public TestAnnotatedTypeBuilder(Class<X> underlying)
   {
      this.underlying = underlying;
   
   }

   public TestAnnotatedTypeBuilder<X> addToClass(Annotation a)
   {
      typeAnnotations.add(a);
      return this;
   }

   public TestAnnotatedTypeBuilder<X> addToField(Field field, Annotation a)
   {
      TestAnnotationBuilder annotations = fields.get(field);
      if (annotations == null)
      {
         annotations = new TestAnnotationBuilder();
         fields.put(field, annotations);
      }
      annotations.add(a);
      return this;
   }

   public TestAnnotatedTypeBuilder<X> addToMethod(Method method, Annotation a)
   {
      TestAnnotationBuilder annotations = methods.get(method);
      if (annotations == null)
      {
         annotations = new TestAnnotationBuilder();
         methods.put(method, annotations);
      }
      annotations.add(a);
      return this;
   }

   public TestAnnotatedTypeBuilder<X> addToMethodParameter(Method method, int parameter, Annotation a)
   {
      Map<Integer, TestAnnotationBuilder> anmap = methodParameters.get(method);
      if (anmap == null)
      {
         anmap = new HashMap<Integer, TestAnnotationBuilder>();
         methodParameters.put(method, anmap);
      }
      TestAnnotationBuilder annotations = anmap.get(parameter);
      if (annotations == null)
      {
         annotations = new TestAnnotationBuilder();
         anmap.put(parameter, annotations);
      }
      annotations.add(a);
      return this;
   }

   public TestAnnotatedTypeBuilder<X> addToConstructor(Constructor<X> constructor, Annotation a)
   {
      TestAnnotationBuilder annotations = constructors.get(constructor);
      if (annotations == null)
      {
         annotations = new TestAnnotationBuilder();
         constructors.put(constructor, annotations);
      }
      annotations.add(a);
      return this;
   }

   public TestAnnotatedTypeBuilder<X> addToConstructorParameter(Constructor<X> constructor, int parameter, Annotation a)
   {
      Map<Integer, TestAnnotationBuilder> anmap = constructorParameters.get(constructor);
      if (anmap == null)
      {
         anmap = new HashMap<Integer, TestAnnotationBuilder>();
         constructorParameters.put(constructor, anmap);
      }
      TestAnnotationBuilder annotations = anmap.get(parameter);
      if (annotations == null)
      {
         annotations = new TestAnnotationBuilder();
         anmap.put(parameter, annotations);
      }
      annotations.add(a);
      return this;
   }

   public AnnotatedType<X> create()
   {
      Map<Constructor<X>, Map<Integer, TestAnnotationStore>> constructorParameterAnnnotations = new HashMap<Constructor<X>, Map<Integer,TestAnnotationStore>>();
      Map<Constructor<X>, TestAnnotationStore> constructorAnnotations = new HashMap<Constructor<X>, TestAnnotationStore>();
      Map<Method, Map<Integer, TestAnnotationStore>> methodParameterAnnnotations = new HashMap<Method, Map<Integer,TestAnnotationStore>>();
      Map<Method, TestAnnotationStore> methodAnnotations = new HashMap<Method, TestAnnotationStore>();
      Map<Field, TestAnnotationStore> fieldAnnotations = new HashMap<Field, TestAnnotationStore>();
      
      for (Entry<Field, TestAnnotationBuilder> e : fields.entrySet())
      {
         fieldAnnotations.put(e.getKey(), e.getValue().create());
      }
      
      for (Entry<Method, TestAnnotationBuilder> e : methods.entrySet())
      {
         methodAnnotations.put(e.getKey(), e.getValue().create());
      }
      for (Entry<Method, Map<Integer, TestAnnotationBuilder>> e : methodParameters.entrySet())
      {
         Map<Integer, TestAnnotationStore> parameterAnnotations = new HashMap<Integer, TestAnnotationStore>();
         methodParameterAnnnotations.put(e.getKey(), parameterAnnotations);
         for (Entry<Integer, TestAnnotationBuilder> pe : e.getValue().entrySet())
         {
            parameterAnnotations.put(pe.getKey(), pe.getValue().create());
         }
      }
      
      for (Entry<Constructor<X>, TestAnnotationBuilder> e : constructors.entrySet())
      {
         constructorAnnotations.put(e.getKey(), e.getValue().create());
      }
      for (Entry<Constructor<X>, Map<Integer, TestAnnotationBuilder>> e : constructorParameters.entrySet())
      {
         Map<Integer, TestAnnotationStore> parameterAnnotations = new HashMap<Integer, TestAnnotationStore>();
         constructorParameterAnnnotations.put(e.getKey(), parameterAnnotations);
         for (Entry<Integer, TestAnnotationBuilder> pe : e.getValue().entrySet())
         {
            parameterAnnotations.put(pe.getKey(), pe.getValue().create());
         }
      }

      return new TestAnnotatedType<X>(underlying, typeAnnotations.create(), fieldAnnotations, methodAnnotations, methodParameterAnnnotations, constructorAnnotations, constructorParameterAnnnotations);
   }

}
