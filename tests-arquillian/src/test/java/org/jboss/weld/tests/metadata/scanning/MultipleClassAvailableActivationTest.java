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
import org.jboss.weld.tests.metadata.scanning.acme.Grault;
import org.jboss.weld.tests.metadata.scanning.acme.Wibble;
import org.jboss.weld.tests.metadata.scanning.acme.corp.Wobble;
import org.jboss.weld.tests.metadata.scanning.acme.corp.Wubble;
import org.jboss.weld.tests.metadata.scanning.jboss.Baz;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MultipleClassAvailableActivationTest {

    public static final Asset BEANS_XML = createBeansXml(
            "<weld:scan>" +
                    "<weld:include name=\"" + Bar.class.getName() + "\">" +
                    "<weld:if-class-available name=\"" + Qux.class.getName() + "\" />" +
                    "<weld:if-class-available name=\"" + Wobble.class.getName() + "\" />" +
                    "<weld:if-class-available name=\"" + Grault.class.getName() + "\" />" +
                    "</weld:include>" +
                    "<weld:include name=\"" + Foo.class.getName() + "\">" +
                    "<weld:if-class-available name=\"com.acme.SomeMadeUpClass\" />" +
                    "<weld:if-class-available name=\"com.acme.SomeOtherMadeUpClass\" />" +
                    "<weld:if-class-available name=\"com.acme.YetAnotherMadeUpClass\" />" +
                    "</weld:include>" +
                    "<weld:include name=\"" + Corge.class.getName() + "\">" +
                    "<weld:if-class-available name=\"com.acme.SomeMadeUpClass\" />" +
                    "<weld:if-class-available name=\"" + Qux.class.getName() + "\" />" +
                    "<weld:if-class-available name=\"" + Corge.class.getName() + "\" />" +
                    "</weld:include>" +
                    "<weld:include name=\"" + Wibble.class.getName() + "\">" +
                    "<weld:if-class-available name=\"!com.acme.SomeMadeUpClass\" />" +
                    "<weld:if-class-available name=\"" + Qux.class.getName() + "\" />" +
                    "<weld:if-class-available name=\"" + Corge.class.getName() + "\" />" +
                    "</weld:include>" +
                    "<weld:include name=\"" + Wubble.class.getName() + "\">" +
                    "<weld:if-class-available name=\"com.acme.SomeMadeUpClass\" />" +
                    "<weld:if-class-available name=\"!" + Qux.class.getName() + "\" />" +
                    "<weld:if-class-available name=\"" + Corge.class.getName() + "\" />" +
                    "</weld:include>" +
                    "</weld:scan>");

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class).addClass(Utils.class)
                .addClasses(Bar.class, Foo.class, Baz.class, Qux.class, Corge.class, Wibble.class, Wubble.class)
                .addClasses(Wobble.class, Grault.class)
                .addAsManifestResource(BEANS_XML, "beans.xml");
    }

    @Test
    public void test(BeanManager beanManager) {
        assert beanManager.getBeans(Bar.class).size() == 1;
        assert beanManager.getBeans(Corge.class).size() == 0;
        assert beanManager.getBeans(Qux.class).size() == 0;
        assert beanManager.getBeans(Foo.class).size() == 0;
        assert beanManager.getBeans(Baz.class).size() == 0;
        assert beanManager.getBeans(Wibble.class).size() == 1;
        assert beanManager.getBeans(Wubble.class).size() == 0;

    }

}
