package org.jboss.weld.tests.beanManager.getReference.synthBean;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Singleton;

public class MyExtension implements Extension {

    public static boolean childCreated;
    public static boolean childDestroyed;
    public static boolean parentCreated;
    public static boolean parentDestroyed;

    final void registerBeans(@Observes final AfterBeanDiscovery event, final BeanManager bm) {
        event.addBean()
                .addTransitiveTypeClosure(Child.class)
                .scope(Dependent.class)
                .createWith((CreationalContext<Child> cc) -> createChild(cc))
                .destroyWith((child, cc) -> destroyChild(cc));
        event.addBean()
                .addTransitiveTypeClosure(Parent.class)
                .scope(Singleton.class)
                .createWith((CreationalContext<Parent> cc) -> createParent(bm, cc))
                .destroyWith((parent, cc) -> destroyParent(cc));
    }

    private Child createChild(final CreationalContext<Child> cc) {
        final Child c = new Child();
        this.childCreated = true;
        return c;
    }

    private void destroyChild(CreationalContext<Child> cc) {
        this.childDestroyed = true;
        cc.release();
    }

    private Parent createParent(final BeanManager bm, final CreationalContext<Parent> cc) {
        final Parent p = new Parent((Child) bm.getReference(bm.resolve(bm.getBeans(Child.class)), Child.class, cc));
        this.parentCreated = true;
        return p;
    }

    private void destroyParent(final CreationalContext<Parent> cc) {
        this.parentDestroyed = true;
        cc.release();
    }
}
