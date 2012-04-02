package org.jboss.weld.resolution;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.introspector.WeldMethod;
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

    private final Class<? extends Annotation> annotationClass;
    private final Map<WeldMethod<?,?>, Object> values;
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
        return ret;
    }

    public static Set<QualifierInstance> qualifiers(final BeanManagerImpl beanManager, Bean<?> bean) {
        if(bean instanceof RIBean) {
            return ((RIBean) bean).getQualifierInstances();
        }
        return qualifiers(beanManager, bean.getQualifiers());
    }

    public QualifierInstance(final Annotation instance, final MetaAnnotationStore store) {
        annotationClass = instance.annotationType();
        final QualifierModel<? extends Annotation> model = store.getBindingTypeModel(annotationClass);
        final Map<WeldMethod<?,?>, Object> values = new HashMap<WeldMethod<?, ?>, Object>();
        for(final WeldMethod<?, ?> method : model.getAnnotatedAnnotation().getMembers()) {
            if(!model.getNonBindingMembers().contains(method)) {
                try {
                    values.put(method, method.invoke(instance));
                } catch (IllegalAccessException e) {
                    throw new WeldException(e);
                } catch (InvocationTargetException e) {
                    throw new WeldException(e);
                }
            }
        }
        this.values = Collections.unmodifiableMap(values);
        int result = annotationClass.hashCode();
        result = 31 * result + values.hashCode();
        hashCode = result;
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
