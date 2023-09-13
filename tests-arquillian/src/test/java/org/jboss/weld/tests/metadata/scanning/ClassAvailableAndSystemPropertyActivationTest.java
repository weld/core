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
import org.jboss.weld.tests.metadata.scanning.acme.Wibble;
import org.jboss.weld.tests.metadata.scanning.acme.corp.Wubble;
import org.jboss.weld.tests.metadata.scanning.jboss.Baz;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ClassAvailableAndSystemPropertyActivationTest {

    public static final Asset BEANS_XML = createBeansXml(
            "<weld:scan>"
                    + "<weld:include name=\"" + Bar.class.getName() + "\">"
                    + "<weld:if-class-available name=\"" + Qux.class.getName() + "\" />"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_1 + "\" />"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_2 + "\" value=\"" + SET_PROPERTY_2_VALUE + "\" />"
                    + "</weld:include>"
                    + "<weld:include name=\"" + Foo.class.getName() + "\">"
                    + "<weld:if-class-available name=\"com.acme.SomeMadeUpClass\" />"
                    + "<weld:if-system-property name=\"" + UNSET_PROPERTY_1 + "\" />"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_2 + "\" value=\"" + SET_PROPERTY_2_VALUE + "\" />"
                    + "</weld:include>"
                    + "<weld:include name=\"" + Corge.class.getName() + "\">"
                    + "<weld:if-class-available name=\"com.acme.SomeMadeUpClass\" />"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_1 + "\" />"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_2 + "\" value=\"" + SET_PROPERTY_2_VALUE + "\" />"
                    + "</weld:include>"
                    + "<weld:include name=\"" + Wibble.class.getName() + "\">"
                    + "<weld:if-class-available name=\"" + Qux.class.getName() + "\" />"
                    + "<weld:if-system-property name=\"!" + UNSET_PROPERTY_1 + "\" />"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_2 + "\" value=\"" + SET_PROPERTY_2_VALUE + "\" />"
                    + "</weld:include>"
                    + "<weld:include name=\"" + Wubble.class.getName() + "\">"
                    + "<weld:if-class-available name=\"" + Qux.class.getName() + "\" />"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_1 + "\" />"
                    + "<weld:if-system-property name=\"" + SET_PROPERTY_2 + "\" value=\"!" + SET_PROPERTY_2_VALUE + "\" />"
                    + "</weld:include>"
                    + "</weld:scan>");

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(Utils.class)
                .addClasses(Bar.class, Foo.class, Baz.class, Qux.class, Corge.class, Wibble.class, Wubble.class)
                .addClasses(SystemPropertyExtension.class)
                .addAsManifestResource(BEANS_XML, "beans.xml")
                .addAsServiceProvider(Extension.class, SystemPropertyExtension.class);
    }

    @Test
    public void test(BeanManager beanManager) {
        assertEquals(1, beanManager.getBeans(Bar.class).size());
        assertEquals(0, beanManager.getBeans(Foo.class).size());
        assertEquals(0, beanManager.getBeans(Corge.class).size());
        assertEquals(1, beanManager.getBeans(Wibble.class).size());
        assertEquals(0, beanManager.getBeans(Wubble.class).size());
        assertEquals(0, beanManager.getBeans(Qux.class).size());
        assertEquals(0, beanManager.getBeans(Baz.class).size());
    }
}
