package org.jboss.webbeans.atinject.tck;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.AnnotationLiteral;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;

import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Tire;
import org.jboss.webbeans.atinject.tck.util.ForwardingAnnotatedConstructor;
import org.jboss.webbeans.atinject.tck.util.ForwardingAnnotatedField;
import org.jboss.webbeans.atinject.tck.util.ForwardingAnnotatedMethod;
import org.jboss.webbeans.atinject.tck.util.ForwardingAnnotatedParameter;
import org.jboss.webbeans.atinject.tck.util.ForwardingAnnotatedType;

public class BeanDisambiguator implements Extension
{

   private static final Annotation PLAIN_LITERAL = new AnnotationLiteral<Plain>()
   {
   };

   /**
    * Modufy the class metadata that 299 will use when building beans
    */
   public void observe(@Observes ProcessAnnotatedType<?> pat)
   {
      addPlainQualifierToTireBean(pat);
      addPlainQualifierToConvertibleConstructor(pat);
      addPlainQualifierToConvertiblePlainTireField(pat);
      addPlainQualifierToConvertibleInjectInstanceMethodWithManyArgs(pat);
   }
   
   /**
    * Adjust the injectInstanceMethodWithManyArgs injectable method on {@link Convertible} so that parameters 
    * 2 and 6 ({@code plainTire} and {@code plainTireProvider}) additionally have the @Plain annotation 
    */
   private <X> void addPlainQualifierToConvertibleInjectInstanceMethodWithManyArgs(ProcessAnnotatedType<X> pat)
   {
      if (pat.getAnnotatedType().getJavaClass().equals(Convertible.class))
      {
         final AnnotatedType<X> original = pat.getAnnotatedType();

         final Set<AnnotatedMethod<? super X>> methods = new HashSet<AnnotatedMethod<? super X>>();
         for (final AnnotatedMethod<? super X> method : original.getMethods())
         {
            if (method.getJavaMember().getName().equals("injectInstanceMethodWithManyArgs"))
            {
               methods.add(qualifyParameterWithPlain(method, 2, 6));
            }
            else
            {
               methods.add(method);
            }
         }

         pat.setAnnotatedType(new ForwardingAnnotatedType<X>()
         {

            @Override
            protected AnnotatedType<X> delegate()
            {
               return original;
            }

            @Override
            public Set<AnnotatedMethod<? super X>> getMethods()
            {
               return methods;
            }

         });
      }
   }
   
   
   /**
    * Add the @Plain qualifier to the field {@code fieldPlainTire} and @{code fieldPlainTireProvider} of {@link Convertible}
    * 
    */
   private <X> void addPlainQualifierToConvertiblePlainTireField(ProcessAnnotatedType<X> pat)
   {
      if (pat.getAnnotatedType().getJavaClass().equals(Convertible.class))
      {
         final AnnotatedType<X> original = pat.getAnnotatedType();
         
         final Set<AnnotatedField<? super X>> fields = new HashSet<AnnotatedField<? super X>>();
         
         for (final AnnotatedField<? super X> field : original.getFields())
         {
            if (field.getJavaMember().getName().equals("fieldPlainTire") || field.getJavaMember().getName().equals("fieldPlainTireProvider"))
            {
               fields.add(addPlainQualifierToField(field));
            }
            else
            {
               fields.add(field);
            }
         }
         
         pat.setAnnotatedType(new ForwardingAnnotatedType<X>()
         {
            
            @Override
            public Set<AnnotatedField<? super X>> getFields()
            {
               return fields;
            }
            
            @Override
            protected AnnotatedType<X> delegate()
            {
               return original;
            }
            
         });
      }
   }

   /**
    * Add the @Plain qualifier to the parameters 2 and 6 ({@code plainTire} and {@code plainTireProvider}) of the constructor of {@link Convertible}
    * 
    */
   private <X> void addPlainQualifierToConvertibleConstructor(ProcessAnnotatedType<X> pat)
   {
      if (pat.getAnnotatedType().getJavaClass().equals(Convertible.class))
      {
         final AnnotatedType<X> original = pat.getAnnotatedType();

         final Set<AnnotatedConstructor<X>> constructors = new HashSet<AnnotatedConstructor<X>>();
         for (final AnnotatedConstructor<X> constructor : original.getConstructors())
         {
            if (constructor.isAnnotationPresent(Inject.class))
            {
               constructors.add(qualifyParameterWithPlain(constructor, 2, 6));
            }
            else
            {
               constructors.add(constructor);
            }
         }

         pat.setAnnotatedType(new ForwardingAnnotatedType<X>()
         {

            @Override
            protected AnnotatedType<X> delegate()
            {
               return original;
            }

            @Override
            public Set<AnnotatedConstructor<X>> getConstructors()
            {
               return constructors;
            }

         });
      }
   }

   /**
    * Add the @Plain annotation to the {@link Tire} class
    * 
    */
   private <X> void addPlainQualifierToTireBean(ProcessAnnotatedType<X> pat)
   {
      if (pat.getAnnotatedType().getJavaClass().equals(Tire.class))
      {
         final Set<Annotation> annotations = new HashSet<Annotation>();
         annotations.addAll(pat.getAnnotatedType().getAnnotations());
         annotations.add(PLAIN_LITERAL);
         final AnnotatedType<X> original = pat.getAnnotatedType();
         pat.setAnnotatedType(new ForwardingAnnotatedType<X>()
         {

            @Override
            protected AnnotatedType<X> delegate()
            {
               return original;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType)
            {
               if (annotationType.equals(Plain.class))
               {
                  return (A) PLAIN_LITERAL;
               }
               else
               {
                  return delegate().getAnnotation(annotationType);
               }
            }

            @Override
            public Set<Annotation> getAnnotations()
            {
               return annotations;
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
            {
               if (annotationType.equals(Plain.class))
               {
                  return true;
               }
               else
               {
                  return delegate().isAnnotationPresent(annotationType);
               }
            }

         });
      }
   }
   
   /**
    * Utility method to add the @Plain qualifier to a list of parameters
    */
   private <X> List<AnnotatedParameter<X>> qualifyParameterWithPlain(final List<AnnotatedParameter<X>> parameters, Integer... position)
   {
      Collection<Integer> positions = Arrays.asList(position);
      final List<AnnotatedParameter<X>> newParameters = new ArrayList<AnnotatedParameter<X>>();

      for (final AnnotatedParameter<X> parameter : parameters)
      {
         if (positions.contains(parameter.getPosition()))
         {
            newParameters.add(addPlainQualifierToParameter(parameter));
         }
         else
         {
            newParameters.add(parameter);
         }
      }
      
      return newParameters;
      
   }
   
   /**
    * Utility method to add the @Plain qualifier to a method
    */
   private <X> AnnotatedMethod<X> qualifyParameterWithPlain(final AnnotatedMethod<X> method, Integer... position)
   {
      final List<AnnotatedParameter<X>> parameters = qualifyParameterWithPlain(method.getParameters(), position);
      return new ForwardingAnnotatedMethod<X>()
      {

         @Override
         public List<AnnotatedParameter<X>> getParameters()
         {
            return parameters;
         }

         @Override
         protected AnnotatedMethod<X> delegate()
         {
            return method;
         }

      };
   }
   
   /**
    * Utility method to add the @Plain qualifier to a constructor
    */
   private <X> AnnotatedConstructor<X> qualifyParameterWithPlain(final AnnotatedConstructor<X> constructor, Integer... position)
   {
      final List<AnnotatedParameter<X>> parameters = qualifyParameterWithPlain(constructor.getParameters(), position);
      return new ForwardingAnnotatedConstructor<X>()
      {

         @Override
         public List<AnnotatedParameter<X>> getParameters()
         {
            return parameters;
         }

         @Override
         protected AnnotatedConstructor<X> delegate()
         {
            return constructor;
         }

      };
   }
   
   /**
    * Utility method to add the @Plain qualifier to a parameter
    */
   private <X> AnnotatedParameter<X> addPlainQualifierToParameter(final AnnotatedParameter<X> parameter)
   {
      final Set<Annotation> annotations = new HashSet<Annotation>();
      annotations.addAll(parameter.getAnnotations());
      annotations.add(PLAIN_LITERAL);
      return new ForwardingAnnotatedParameter<X>()
      {
         
         @Override
         protected AnnotatedParameter<X> delegate()
         {
            return parameter;
         }
         
         @SuppressWarnings("unchecked")
         @Override
         public <A extends Annotation> A getAnnotation(Class<A> annotationType)
         {
            if (annotationType.equals(Plain.class))
            {
               return (A) PLAIN_LITERAL;
            }
            else
            {
               return delegate().getAnnotation(annotationType);
            }
         }
         
         @Override
         public Set<Annotation> getAnnotations()
         {
            return annotations;
         }
         
         @Override
         public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
         {
            if (annotationType.equals(Plain.class))
            {
               return true;
            }
            else
            {
               return delegate().isAnnotationPresent(annotationType);
            }
         }
         
      };
   }
   
   /**
    * Utility method to add the @Plain qualifier to a field
    */
   private <X> AnnotatedField<X> addPlainQualifierToField(final AnnotatedField<X> field)
   {
      final Set<Annotation> annotations = new HashSet<Annotation>();
      annotations.addAll(field.getAnnotations());
      annotations.add(PLAIN_LITERAL);
      return new ForwardingAnnotatedField<X>()
      {
         
         @SuppressWarnings("unchecked")
         @Override
         public <A extends Annotation> A getAnnotation(Class<A> annotationType)
         {
            if (annotationType.equals(Plain.class))
            {
               return (A) PLAIN_LITERAL;
            }
            else
            {
               return delegate().getAnnotation(annotationType);
            }
         }
         
         @Override
         public Set<Annotation> getAnnotations()
         {
            return annotations;
         }
         
         @Override
         public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
         {
            if (annotationType.equals(Plain.class))
            {
               return true;
            }
            else
            {
               return delegate().isAnnotationPresent(annotationType);
            }
         }
         
         @Override
         protected AnnotatedField<X> delegate()
         {
            return field;
         }
         
      };
   }


}
