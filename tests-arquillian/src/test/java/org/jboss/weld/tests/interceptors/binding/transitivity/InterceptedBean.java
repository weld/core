package org.jboss.weld.tests.interceptors.binding.transitivity;

import jakarta.enterprise.context.Dependent;

/**
 * @author Marko Luksa
 */
@Dependent
public class InterceptedBean {

    @Synchronized
    public void synchronizedMethod() {
    }

    @Secure
    public void secure() {
    }

    @Transactional
    public void transactional() {
    }

    @UltraSynchronized
    public void ultraSynchronized() {
    }

    @UltraSecure
    public void ultraSecure() {
    }

    @UltraTransactional
    public void ultraTransactional() {
    }
}
