package org.jboss.weld.tests.interceptors.visibility;

import static org.junit.Assert.assertTrue;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.interceptors.visibility.unreachable.AbstractPanel;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class UnreachableInterceptedInterfaceTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(UnreachableInterceptedInterfaceTest.class))
                .addPackage(UnreachableInterceptedInterfaceTest.class.getPackage())
                .addPackage(AbstractPanel.class.getPackage());
    }

    @Inject
    MyPanel panel;

    @Test
    public void testInterceptorInvoked() {
        panel.drawPanel();
        assertTrue(PanelInterceptor.called);
    }

}