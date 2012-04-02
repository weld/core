package org.jboss.weld.resolution;

import static org.jboss.weld.util.collections.WeldCollections.immutableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.runtime.RuntimeAnnotatedMembers;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.QualifierModel;

/**
 * Optmized representation of a qualifier. JDK annotation proxies are slooow, this class provides significantly
 * faster equals/hashCode methods, that also correctly handle non binding attributes.
 *
 * @author Stuart Douglas
 */
public class QualifierInstance {

    public static final QualifierInstance ANY = new QualifierInstance(Any.class);

    private final Class<? extends Annotation> annotationClass;
    private final Map<AnnotatedMethod<?>, Object> values;
    private final int hashCode;


    public static Set<QualifierInstance> qualifiers(final BeanManagerImpl beanManager, Set<Annotation> annotations) {
        if(annotations.isEmpty()) {
            return Collections.emptySet();
        }
        final MetaAnnotationStore store = beanManager.getServices().get(MetaAnnotationStore.class);
        final Set<QualifierInstance> ret = new HashSet<QualifierInstance>();
        for(Annotation a : annotations) {
            ret.add(new QualifierInstance(a, store));
        }
        return immutableSet(ret);
    }

    public static Set<QualifierInstance> qualifiers(final BeanManagerImpl beanManager, Bean<?> bean) {
        if(bean instanceof RIBean) {
            return ((RIBean<?>) bean).getQualifierInstances();
        }
        return qualifiers(beanManager, bean.getQualifiers());
    }

    public QualifierInstance(final Annotation instance, final MetaAnnotationStore store) {
        this(instance, instance.annotationType(), store);
    }

    private QualifierInstance(final Annotation instance, Class<? extends Annotation> annotationClass, final MetaAnnotationStore store) {
        this(annotationClass, createValues(instance, annotationClass, store));
    }

    private QualifierInstance(final Class<? extends Annotation> annotationClass) {
        this(annotationClass, Collections.<AnnotatedMethod<?>, Object>emptyMap());
    }

    private QualifierInstance(Class<? extends Annotation> annotationClass, Map<AnnotatedMethod<?>, Object> values) {
        this.annotationClass = annotationClass;
        this.values = values;
        this.hashCode = 31 * annotationClass.hashCode() + values.hashCode();
    }

    private static Map<AnnotatedMethod<?>, Object> createValues(final Annotation instance, final Class<? extends Annotation> annotationClass, final MetaAnnotationStore store) {
        final Map<AnnotatedMethod<?>, Object> values = new HashMap<AnnotatedMethod<?>, Object>();
        final QualifierModel<? extends Annotation> model = store.getBindingTypeModel(annotationClass);
        for (final AnnotatedMethod<?> method : model.getAnnotatedAnnotation().getMethods()) {
            if(!model.getNonBindingMembers().contains(method)) {
                try {
                    values.put(method, RuntimeAnnotatedMembers.invokeMethod(method, instance));
                } catch (IllegalAccessException e) {
                    throw new WeldException(e);
                } catch (InvocationTargetException e) {
                    throw new WeldException(e);
                }
            }
        }
        return Collections.unmodifiableMap(values);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final QualifierInstance that = (QualifierInstance) o;

        if (!annotationClass.equals(that.annotationClass)) return false;
        if (!values.equals(that.values)) return false;

        return true;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "QualifierInstance{" +
                "annotationClass=" + annotationClass +
                ", values=" + values +
                ", hashCode=" + hashCode +
                '}';
    }
}
