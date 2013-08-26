package org.jboss.weld.environment.se.test.provider.custom;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;

import org.jboss.weld.Weld;
import org.jboss.weld.bean.builtin.BeanManagerProxy;

@Vetoed
public class MyCDI extends Weld implements CDIProvider {

    public static final MyCDI INSTANCE = new MyCDI();
    
    public static boolean isGetBMCalled = false;
    public static boolean isGetCDICalled = false;

    private MyCDI() {
    }
    
    @Override
    public BeanManagerProxy getBeanManager() {
        isGetBMCalled = true;
        return super.getBeanManager();
    }

    @Override
    public CDI<Object> getCDI() {
        isGetCDICalled = true;
        return INSTANCE;
    }

    public static void reset() {
        isGetBMCalled = false;
        isGetCDICalled = false;
    }
}
