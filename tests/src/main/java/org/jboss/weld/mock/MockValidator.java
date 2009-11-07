package org.jboss.weld.mock;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;

public class MockValidator implements Validator
{

   public BeanDescriptor getConstraintsForClass(Class<?> clazz)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> T unwrap(Class<T> type)
   {
      if (type.equals(Validator.class))
      {
         return type.cast(this);
      }
      else
      {
         throw new ValidationException();
      }
   }

   public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups)
   {
      // TODO Auto-generated method stub
      return null;
   }


}
