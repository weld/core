package org.jboss.weld.tests.interceptors.binding.transitivity;

/**
 * @author Marko Luksa
 */
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
