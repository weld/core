package org.jboss.weld.interceptor.util.proxy;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public interface TargetInstanceProxy<T> {
    T getTargetInstance();

    Class<? extends T> getTargetClass();
}
