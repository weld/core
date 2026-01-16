package org.jboss.weld.bean.proxy;

import java.security.ProtectionDomain;

import org.jboss.classfilewriter.ClassFactory;
import org.jboss.weld.bean.proxy.util.WeldDefaultProxyServices;

/**
 * A dummy implementation which has only one purpose - to avoid instantiating {@code DefaultClassFactory.INSTANCE}.
 * The sole method in this class is never used as we define classes using different means that further vary
 * between in-container (such as WildFly) and SE setups.
 * <p>
 * See {@link WeldDefaultProxyServices#defineClass(Class, String, byte[], int, int)} for details on how we define
 * classes.
 */
public class DummyClassFactoryImpl implements ClassFactory {

    private DummyClassFactoryImpl() {
    }

    // final so that there's only one instance that's being referenced from anywhere
    public static final DummyClassFactoryImpl INSTANCE = new DummyClassFactoryImpl();

    @Override
    public Class<?> defineClass(ClassLoader loader, String name, byte[] b, int off, int len, ProtectionDomain protectionDomain)
            throws ClassFormatError {
        throw new UnsupportedOperationException("DummyClasFactoryImpl should not be used to define classes");
    }
}
