package org.jboss.weld.tests.interceptors.bridgemethods.common;

/**
 *
 */
public interface SpecialService extends BaseService<String> {

    @Override
    void setSomething(String param);

    @Override
    String getSomething();
}
