package org.jboss.weld.resolution;

import static org.jboss.weld.util.collections.WeldCollections.immutableMap;
import static org.jboss.weld.util.collections.WeldCollections.immutableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.QualifierModel;
import org.jboss.weld.security.SetAccessibleAction;
import org.jboss.weld.util.collections.ArraySet;

import com.google.common.base.Objects;

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

    public static QualifierInstance of(Annotation annotation, MetaAnnotationStore store) {
        return new QualifierInstance(annotation, store);
    }

    public static Set<QualifierInstance> qualifiers(final BeanManagerImpl beanManager, Set<Annotation> annotations) {
        return qualifiers(beanManager.getServices().get(MetaAnnotationStore.class), annotations);
    }
    public static Set<QualifierInstance> qualifiers(MetaAnnotationStore metaAnnotationStore, Set<Annotation> annotations) {
        if (annotations.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<QualifierInstance> ret = new ArraySet<QualifierInstance>();
        for(Annotation a : annotations) {
            ret.add(new QualifierInstance(a, metaAnnotationStore));
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
        this.hashCode = Objects.hashCode(annotationClass, values);
    }

    private static Map<AnnotatedMethod<?>, Object> createValues(final Annotation instance, final Class<? extends Annotation> annotationClass, final MetaAnnotationStore store) {
        final Map<AnnotatedMethod<?>, Object> values = new HashMap<AnnotatedMethod<?>, Object>();
        final QualifierModel<? extends Annotation> model = store.getBindingTypeModel(annotationClass);
        for (final AnnotatedMethod<?> method : model.getAnnotatedAnnotation().getMethods()) {
            if(!model.getNonBindingMembers().contains(method)) {
                try {
                    if(System.getSecurityManager() != null) {
                        AccessController.doPrivileged(SetAccessibleAction.of(method.getJavaMember()));
                    } else {
                        method.getJavaMember().setAccessible(true);
                    }
                    values.put(method, method.getJavaMember().invoke(instance));
                } catch (IllegalAccessException e) {
                    throw new WeldException(e);
                } catch (InvocationTargetException e) {
                    throw new WeldException(e);
                }
            }
        }
        return immutableMap(values);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final QualifierInstance that = (QualifierInstance) o;

        if (!annotationClass.equals(that.annotationClass)) {
            return false;
        }
        if (!values.equals(that.values)) {
            return false;
        }

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
