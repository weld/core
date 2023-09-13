package org.jboss.weld.tests.servlet.dispatch;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.weld.tests.category.Integration;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests a combination of dispatch types (include/forward) and local/cross-context dispatch to verify that contexts are
 * activated/deactivated properly. Each
 * dispatch request is followed by a validation request in which the number of constructions/destructions is examined to verify
 * that an activated context is
 * always deactivated properly (no leaks occur).
 *
 * Unlike DispatchingTest, this test uses ConversationFilter for conversation activation.
 *
 * @author Jozef Hartinger
 * @author Ron Smeral
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class DispatchingWithConversationFilterTest extends AbstractDispatchingTestCase {

    @Deployment(testable = false)
    public static Archive<?> getDeployment() {
        return Deployments.deployment(MainServlet.class, true, DispatchingWithConversationFilterTest.class);
    }
}
