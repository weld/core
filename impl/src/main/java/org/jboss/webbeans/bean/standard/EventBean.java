package org.jboss.webbeans.bean.standard;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.event.Event;
import javax.event.Fires;
import javax.inject.Obtains;
import javax.inject.TypeLiteral;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.injection.resolution.AnnotatedItemTransformer;
import org.jboss.webbeans.literal.FiresLiteral;

public class EventBean extends AbstractFacadeBean<Event<?>>
{

   private static final Class<Event<?>> TYPE = new TypeLiteral<Event<?>>() {}.getRawType();
   private static final Set<? extends Type> DEFAULT_TYPES = new HashSet<Type>(Arrays.asList(TYPE, Object.class));
   private static final Fires FIRES = new FiresLiteral();
   private static final Set<Annotation> DEFAULT_BINDINGS = new HashSet<Annotation>(Arrays.asList(FIRES));
   public static final AnnotatedItemTransformer TRANSFORMER = new FacadeBeanAnnotatedItemTransformer(Event.class, FIRES);
   private static final Set<Class<? extends Annotation>> FILTERED_ANNOTATION_TYPES = new HashSet<Class<? extends Annotation>>(Arrays.asList(Obtains.class));
   
   
   public static AbstractFacadeBean<Event<?>> of(ManagerImpl manager)
   {
      return new EventBean(manager);
   }
   
   protected EventBean(ManagerImpl manager)
   {
      super(manager);
   }

   @Override
   public Class<Event<?>> getType()
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
   protected Event<?> newInstance(Type type, Set<Annotation> annotations)
   {
      return EventImpl.of(type, getManager(), annotations);
   }

   @Override
   protected Set<Class<? extends Annotation>> getFilteredAnnotationTypes()
   {
      return FILTERED_ANNOTATION_TYPES;
   }
   
   @Override
   public String toString()
   {
      return "Built-in implicit javax.event.Event bean";
   }
   
}
