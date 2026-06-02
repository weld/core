package org.jboss.weld.tests.instance.wildcard.covariant;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that {@code Instance<? extends X>} injection points are valid and
 * functional. Instance is naturally covariant so an upper-bounded wildcard is
 * a legitimate use case.
 *
 * @see <a href="https://github.com/jakartaee/cdi/issues/888">CDI #888</a>
 */
@RunWith(Arquillian.class)
public class InstanceCovariantWildcardTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InstanceCovariantWildcardTest.class))
                .addClasses(BeanWithCovariantInstance.class, Widget.class);
    }

    @Test
    public void testCovariantInstanceWildcardDeploys(BeanWithCovariantInstance bean) {
        assertTrue("Instance<? extends Widget> should be resolvable", bean.isResolvable());
        Widget widget = bean.get();
        assertNotNull("Instance.get() should return a Widget", widget);
    }
}
