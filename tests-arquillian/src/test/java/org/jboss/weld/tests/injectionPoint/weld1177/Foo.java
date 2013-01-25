package org.jboss.weld.tests.injectionPoint.weld1177;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Stateless
public class Foo {

//    @Inject
    private InjectionPoint injectionPoint;

    @Inject
    private Bar bar;

    @Inject
    public void setInjectionPoint(InjectionPoint injectionPoint) {
        this.injectionPoint = injectionPoint;
    }

    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

    public void doSomething() {
        System.out.println("System.identityHashCode(this) = " + System.identityHashCode(this));
    }

    public Member getInjectionPointMember() {
        return injectionPoint.getMember();
    }

    public Type getInjectionPointType() {
        return injectionPoint.getType();
    }

    public Member getBarInjectionPointMember() {
        return bar.getInjectionPointMember();
    }
}
