package org.jboss.weld.tests.interceptors.weld760;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class DuplicateInterceptorTest {

    @Deployment
    public static Archive<?> deploy() {
        Archive jar = ShrinkWrap.create(JavaArchive.class)
                .addClasses(MySuperClass.class);

        Archive beanArchive = ShrinkWrap.create(BeanArchive.class)
                .intercept(MyInterceptor.class)
                .addClasses(MyObject.class, MyManagedBean.class, MyInterceptor.class, MyInterceptorBinding.class,
                        MyStereotype.class);

        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(DuplicateInterceptorTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addAsLibraries(jar, beanArchive);
    }

    @Test
    public void testDuplicateInterceptor(MyManagedBean myManagedBean) throws Exception {
        myManagedBean.perform();
    }

}
