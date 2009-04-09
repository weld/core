package org.jboss.webbeans.persistence;

import java.lang.annotation.Annotation;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.util.ApiAbstraction;
import org.jboss.webbeans.util.ApiAbstraction.Dummy;
import org.jboss.webbeans.util.ApiAbstraction.DummyEnum;

public class PersistenceApiAbstraction extends ApiAbstraction implements Service
{

   public final Class<? extends Annotation> PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
   public final Object EXTENDED_PERSISTENCE_CONTEXT_ENUM_VALUE;
   public final Class<?> PERSISTENCE_CONTEXT_TYPE_CLASS;
   public final Class<? extends Annotation> ENTITY_CLASS;
   public final Class<? extends Annotation> MAPPED_SUPERCLASS_CLASS;
   public final Class<? extends Annotation> EMBEDDABLE_CLASS;

   /**
    * @param resourceLoader
    */
   public PersistenceApiAbstraction(ResourceLoader resourceLoader)
   {
      super(resourceLoader);
      PERSISTENCE_CONTEXT_ANNOTATION_CLASS = annotationTypeForName("javax.persistence.PersistenceContext");
      PERSISTENCE_CONTEXT_TYPE_CLASS = classForName("javax.persistence.PersistenceContextType");
      if (PERSISTENCE_CONTEXT_TYPE_CLASS.getClass().equals( Dummy.class)) 
      {
         EXTENDED_PERSISTENCE_CONTEXT_ENUM_VALUE = enumValue(PERSISTENCE_CONTEXT_TYPE_CLASS, "EXTENDED");
      } 
      else
      {
         EXTENDED_PERSISTENCE_CONTEXT_ENUM_VALUE = DummyEnum.DUMMY_VALUE;
      }
      ENTITY_CLASS = annotationTypeForName("javax.persistence.Entity");
      MAPPED_SUPERCLASS_CLASS = annotationTypeForName("javax.persistence.MappedSuperclass");
      EMBEDDABLE_CLASS = annotationTypeForName("javax.persistence.Embeddable");
   }
   
}