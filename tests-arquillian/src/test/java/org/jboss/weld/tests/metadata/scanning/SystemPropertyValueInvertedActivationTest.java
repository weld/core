package org.jboss.weld.tests.metadata.scanning;

import static org.jboss.weld.tests.metadata.scanning.SystemPropertyExtension.*;
import static org.jboss.weld.tests.metadata.scanning.Utils.createBeansXml;
import static org.junit.Assert.assertEquals;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

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
public class SystemPropertyValueInvertedActivationTest {

    public static final Asset BEANS_XML = createBeansXml(
            "<weld:scan>"
                    + "<weld:include name=\"" + Bar.class.getName() + "\">"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_1 + "\" value=\"!" + SET_PROPERTY_1_VALUE + "\" />"
                    + "</weld:include>"
                    + "<weld:include name=\"" + Foo.class.getName() + "\">"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_2 + "\" value=\"!" + SET_PROPERTY_2_OTHER_VALUE
                    + "\" />"
                    + "</weld:include>"
                    + "</weld:scan>");

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class).addClass(Utils.class)
                .addClasses(Bar.class, Foo.class, Baz.class, Qux.class)
                .addClass(Utils.class).addClasses(SystemPropertyExtension.class)
                .addAsManifestResource(BEANS_XML, "beans.xml")
                .addAsServiceProvider(Extension.class, SystemPropertyExtension.class);
    }

    @Test
    public void test(BeanManager beanManager) {
        assertEquals(0, beanManager.getBeans(Bar.class).size());
        assertEquals(1, beanManager.getBeans(Foo.class).size());
        assertEquals(0, beanManager.getBeans(Qux.class).size());
        assertEquals(0, beanManager.getBeans(Baz.class).size());
    }
}
