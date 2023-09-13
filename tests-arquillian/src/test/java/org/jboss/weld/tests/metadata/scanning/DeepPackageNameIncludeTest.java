package org.jboss.weld.tests.metadata.scanning;

import static org.jboss.weld.tests.metadata.scanning.Utils.createBeansXml;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.tests.metadata.Qux;
import org.jboss.weld.tests.metadata.scanning.jboss.Baz;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DeepPackageNameIncludeTest {

    public static final Asset BEANS_XML = createBeansXml(
            "<weld:scan>" +
                    "<weld:include name=\"" + Bar.class.getPackage().getName() + ".**\"/>" +
                    "</weld:scan>");

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class).addClass(Utils.class)
                .addClasses(Bar.class, Foo.class, Baz.class, Qux.class)
                .addAsManifestResource(BEANS_XML, "beans.xml");
    }

    @Test
    public void test(BeanManager beanManager) {
        assert beanManager.getBeans(Qux.class).size() == 0;
        assert beanManager.getBeans(Baz.class).size() == 1;
        assert beanManager.getBeans(Foo.class).size() == 1;
        assert beanManager.getBeans(Bar.class).size() == 1;
    }

}
