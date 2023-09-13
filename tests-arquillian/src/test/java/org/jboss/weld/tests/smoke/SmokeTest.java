package org.jboss.weld.tests.smoke;

import jakarta.enterprise.inject.Instance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Smoke tests -- check anything unusual.
 *
 * @author Sam Corbet
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class SmokeTest {
    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SmokeTest.class))
                .addPackage(Crasher.class.getPackage());
    }

    @Test
    public void testInnerClass(Instance<Crasher> instance) {
        Crasher crasher = instance.get();
        Assert.assertNotNull(crasher);
    }

}
