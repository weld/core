package org.jboss.weld.tests.interceptors.bindingconflict;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class BindingConflictTest {
    @Deployment
    @ShouldThrowException(Exception.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .intercept(MyInterceptor.class)
                .addPackage(BindingConflictTest.class.getPackage());
    }

    @Test
    public void testDeployment() throws Exception {
    }
}
