package org.jboss.weld.tests.weld1192;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class Weld1192Test {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(Weld1192Test.class))
                .addPackage(Weld1192Test.class.getPackage());
    }

    @Inject
    BeanManager manager;

    @Test
    public void testTypedBeanDeployment() {
        // tests that typed bean can be deployed
        Set<Bean<?>> beans = manager.getBeans(StringFoo.class);
        assertTrue(beans.size() == 1);
    }

}
