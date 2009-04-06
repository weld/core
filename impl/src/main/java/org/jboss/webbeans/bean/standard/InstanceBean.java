package org.jboss.webbeans.bean.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Instance;
import javax.inject.Obtains;
import javax.inject.TypeLiteral;

import org.jboss.webbeans.InstanceImpl;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injection.resolution.AnnotatedItemTransformer;
import org.jboss.webbeans.literal.ObtainsLiteral;

public class InstanceBean extends AbstractFacadeBean<Instance<?>>
{

   private static final Class<Instance<?>> TYPE = new TypeLiteral<Instance<?>>() {}.getRawType();
   private static final Set<? extends Type> DEFAULT_TYPES = new HashSet<Type>(Arrays.asList(TYPE, Object.class));
   private static final Obtains OBTAINS = new ObtainsLiteral();
   private static final Set<Annotation> DEFAULT_BINDINGS = new HashSet<Annotation>(Arrays.asList(OBTAINS));
   private static final Set<Class<? extends Annotation>> FILTERED_ANNOTATION_TYPES = new HashSet<Class<? extends Annotation>>(Arrays.asList(Obtains.class));
   public static final AnnotatedItemTransformer TRANSFORMER = new FacadeBeanAnnotatedItemTransformer(TYPE, OBTAINS);
   
   
   public static AbstractFacadeBean<Instance<?>> of(ManagerImpl manager)
   {
      return new InstanceBean(manager);
   }
   
   protected InstanceBean(ManagerImpl manager)
   {
      super(manager);
   }

   @Override
   public Class<Instance<?>> getType()
   {
      return TYPE;
   }

   @Override
   public Set<? extends Type> getTypes()
   {
      return DEFAULT_TYPES;
   }
   
   @Override
   public Set<Annotation> getBindings()
   {
      return DEFAULT_BINDINGS;
   }

   @Override
   protected Instance<?> newInstance(Type type, Set<Annotation> annotations)
   {
      return InstanceImpl.of(type, getManager(), annotations);
   }

   @Override
   protected Set<Class<? extends Annotation>> getFilteredAnnotationTypes()
   {
      return FILTERED_ANNOTATION_TYPES;
   }
   
   @Override
   public String toString()
   {
      return "Built-in implicit javax.inject.Instance bean";
   }
   
}
