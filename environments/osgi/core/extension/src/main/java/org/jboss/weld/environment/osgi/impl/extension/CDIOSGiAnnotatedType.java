/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.osgi.impl.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.*;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import org.jboss.weld.environment.osgi.api.annotation.Filter;
import org.jboss.weld.environment.osgi.api.annotation.OSGiService;

/**
 * CDI-OSGi annotated type. Wrap regular CDI annotated types in order to enable
 * CDI-OSGi features.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class CDIOSGiAnnotatedType<T> implements AnnotatedType<T>
{
   private static Logger logger = LoggerFactory.getLogger(
           CDIOSGiAnnotatedType.class);

   AnnotatedType<T> annotatedType;

   Set<AnnotatedConstructor<T>> constructors =
                                new HashSet<AnnotatedConstructor<T>>();

   Set<AnnotatedMethod<? super T>> methods =
                                   new HashSet<AnnotatedMethod<? super T>>();

   Set<AnnotatedField<? super T>> fields =
                                  new HashSet<AnnotatedField<? super T>>();

   public CDIOSGiAnnotatedType(AnnotatedType<T> annotatedType) throws Exception
   {
      logger.debug("Creation of a new CDIOSGiAnnotatedType wrapping {}",
                   annotatedType);
      this.annotatedType = annotatedType;
      process();
   }

   private void process() throws Exception
   {
      if (getJavaClass().getAnnotation(Filter.class) != null)
      {
         StringBuilder msg = new StringBuilder();
         msg.append("Filter qualifier: ")
                 .append(getAnnotation(Filter.class))
                 .append(", does not apply to bean class (")
                 .append(getBaseType())
                 .append(')');
         logger.error(msg.toString());
         throw new Exception(msg.toString());
      }
      for (Annotation annotation : getJavaClass().getAnnotations())
      {
         if (annotation.annotationType().isAnnotationPresent(Filter.class))
         {
            StringBuilder msg = new StringBuilder();
            msg.append("Filter stereotype: ")
                    .append(annotation)
                    .append(", does not apply to bean class (")
                    .append(getBaseType())
                    .append(')');
            logger.error(msg.toString());
            throw new Exception(msg.toString());
         }
      }
      for (AnnotatedConstructor<T> constructor : annotatedType.getConstructors())
      {
         logger.trace("Processing constructor {}", constructor);
         if (isCDIOSGiConstructor(constructor))
         {
            constructors.add(new CDIOSGiAnnotatedConstructor<T>(constructor));
         }
         else
         {
            constructors.add(constructor);
         }
      }
      for (AnnotatedMethod<? super T> method : annotatedType.getMethods())
      {
         logger.trace("Processing method {}", method);
         if (isCDIOSGiMethod(method))
         {
            methods.add(new CDIOSGiAnnotatedMethod<T>(method));
         }
         else
         {
            methods.add(method);
         }
      }
      for (AnnotatedField<? super T> field : annotatedType.getFields())
      {
         logger.trace("Processing field {}", field);
         if (isCDIOSGiField(field))
         {
            fields.add(new CDIOSGiAnnotatedField<T>(field));
         }
         else
         {
            fields.add(field);
         }
      }
   }

   @Override
   public Class getJavaClass()
   {
      return annotatedType.getJavaClass();
   }

   @Override
   public Set<AnnotatedConstructor<T>> getConstructors()
   {
      return constructors;
   }

   @Override
   public Set<AnnotatedMethod<? super T>> getMethods()
   {
      return methods;
   }

   @Override
   public Set<AnnotatedField<? super T>> getFields()
   {
      return fields;
   }

   @Override
   public Type getBaseType()
   {
      return annotatedType.getBaseType();
   }

   @Override
   public Set<Type> getTypeClosure()
   {
      return annotatedType.getTypeClosure();
   }

   @Override
   public <T extends Annotation> T getAnnotation(Class<T> annotationType)
   {
      return annotationType.getAnnotation(annotationType);
   }

   @Override
   public Set<Annotation> getAnnotations()
   {
      return annotatedType.getAnnotations();
   }

   @Override
   public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return annotatedType.isAnnotationPresent(annotationType);
   }

   private boolean isCDIOSGiField(AnnotatedField<? super T> field)
   {
      if (field.isAnnotationPresent(Inject.class) && field.isAnnotationPresent(
              OSGiService.class))
      {
         return true;
      }
      return false;
   }

   private boolean isCDIOSGiMethod(AnnotatedMethod<? super T> method)
   {
      if (method.isAnnotationPresent(Inject.class))
      {
         for (AnnotatedParameter parameter : method.getParameters())
         {
            if (parameter.isAnnotationPresent(OSGiService.class))
            {
               return true;
            }
         }
      }
      return false;
   }

   private boolean isCDIOSGiConstructor(AnnotatedConstructor<T> constructor)
   {
      if (constructor.isAnnotationPresent(Inject.class))
      {
         for (AnnotatedParameter parameter : constructor.getParameters())
         {
            if (parameter.isAnnotationPresent(OSGiService.class))
            {
               return true;
            }
         }
      }
      return false;
   }

}
