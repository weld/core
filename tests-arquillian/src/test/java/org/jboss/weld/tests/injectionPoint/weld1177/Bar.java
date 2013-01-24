package org.jboss.weld.tests.injectionPoint.weld1177;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class Bar {

    @Inject
    private InjectionPoint injectionPoint;


    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

    public Member getInjectionPointMember() {
        return injectionPoint.getMember();
    }

    public Type getInjectionPointType() {
        return injectionPoint.getType();
    }
}
