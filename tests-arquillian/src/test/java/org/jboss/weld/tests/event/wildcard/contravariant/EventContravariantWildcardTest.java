package org.jboss.weld.tests.event.wildcard.contravariant;

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
 * Verifies that {@code Event<? super X>} injection points are valid and functional.
 * {@code Event} is naturally contravariant — you fire subtypes into it — so a
 * lower-bounded wildcard is a legitimate use case.
 * <p>
 * This reproduces the scenario reported by Gavin King where Jakarta Data injects
 * {@code Event<? super LifecycleEvent<?>>}.
 *
 * @see <a href="https://github.com/jakartaee/cdi/issues/888">CDI #888</a>
 */
@RunWith(Arquillian.class)
public class EventContravariantWildcardTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EventContravariantWildcardTest.class))
                .addClasses(BeanWithContravariantEvent.class, LifecycleEvent.class, LifecycleEventObserver.class,
                        BeanWithSimpleContravariantEvent.class, Widget.class, WidgetObserver.class);
    }

    @Test
    public void testParameterizedContravariantEventWildcard(BeanWithContravariantEvent bean,
            LifecycleEventObserver observer) {
        bean.fireEvent(new LifecycleEvent<>("test"));
        assertTrue("LifecycleEvent should have been observed", observer.isObserved());
    }

    @Test
    public void testSimpleContravariantEventWildcard(BeanWithSimpleContravariantEvent bean,
            WidgetObserver observer) {
        bean.fireWidget(new Widget("test"));
        assertTrue("Widget event should have been observed", observer.isObserved());
    }
}
