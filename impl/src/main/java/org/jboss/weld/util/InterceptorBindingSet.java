package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class InterceptorBindingSet extends AbstractSet<Annotation> {

    private BeanManagerImpl beanManager;
    private Set<Annotation> set = new HashSet<Annotation>();

    public InterceptorBindingSet(BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public boolean add(Annotation annotation) {
        if (contains(annotation)) {
            return false;
        }
        return set.add(annotation);
    }

    @Override
    public Iterator<Annotation> iterator() {
        return set.iterator();
    }

    @Override
    public int size() {
        return set.size();
    }

    public boolean contains(Object o) {
        if (o instanceof Annotation) {
            Annotation annotation = (Annotation) o;

            MetaAnnotationStore metaAnnotationStore = beanManager.getServices().get(MetaAnnotationStore.class);
            InterceptorBindingModel<? extends Annotation> interceptorBindingModel = metaAnnotationStore
                    .getInterceptorBindingModel(annotation.annotationType());

            for (Annotation containedAnnotation : set) {
                if (interceptorBindingModel.isEqual(annotation, containedAnnotation)) {
                    return true;
                }
            }
            return false;
        } else {
            return super.contains(o);
        }
    }

}
