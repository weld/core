package org.jboss.weld.tests.metadata.scanning;

import static org.jboss.weld.tests.metadata.scanning.Utils.createBeansXml;
import static org.jboss.weld.tests.metadata.scanning.Utils.escapePackageName;

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
import org.jboss.weld.tests.metadata.scanning.jboss.Garply;
import org.jboss.weld.tests.metadata.scanning.jboss.corp.Flob;
import org.jboss.weld.tests.metadata.scanning.jboss.corp.Fred;
import org.jboss.weld.tests.metadata.scanning.redhat.Plugh;
import org.jboss.weld.tests.metadata.scanning.redhat.Waldo;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MultipleDeepPackagePatternIncludeExcludeTest {

    public static final Asset BEANS_XML = createBeansXml(
            "<weld:scan>" +
                    "<weld:exclude pattern=\"" + escapePackageName(Baz.class.getPackage()) + "(\\.\\w*)*\"/>" +
                    "<weld:include pattern=\"" + escapePackageName(Foo.class.getPackage()) + "(\\.\\w*)*\"/>" +
                    "<weld:include pattern=\"" + escapePackageName(Grault.class.getPackage()) + "(\\.\\w*)*\"/>" +
                    "</weld:scan>");

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class).addClass(Utils.class)
                .addClasses(Bar.class, Foo.class, Baz.class, Qux.class, Corge.class, Garply.class, Grault.class)
                .addClasses(Wibble.class, Wobble.class, Wubble.class, Flob.class, Fred.class, Plugh.class, Waldo.class)
                .addAsManifestResource(BEANS_XML, "beans.xml");
    }

    @Test
    public void test(BeanManager beanManager) {

        assert beanManager.getBeans(Foo.class).size() == 1;
        assert beanManager.getBeans(Bar.class).size() == 1;
        assert beanManager.getBeans(Corge.class).size() == 1;
        assert beanManager.getBeans(Waldo.class).size() == 1;
        assert beanManager.getBeans(Plugh.class).size() == 1;

        assert beanManager.getBeans(Grault.class).size() == 1;
        assert beanManager.getBeans(Wibble.class).size() == 1;
        assert beanManager.getBeans(Wobble.class).size() == 1;
        assert beanManager.getBeans(Wubble.class).size() == 1;

        assert beanManager.getBeans(Baz.class).size() == 0;
        assert beanManager.getBeans(Garply.class).size() == 0;
        assert beanManager.getBeans(Flob.class).size() == 0;
        assert beanManager.getBeans(Fred.class).size() == 0;
    }

}
