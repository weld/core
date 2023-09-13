package org.jboss.weld.tests.metadata.scanning;

import static org.jboss.weld.tests.metadata.scanning.Utils.createBeansXml;
import static org.jboss.weld.tests.metadata.scanning.Utils.escapeClassName;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.tests.metadata.Qux;
import org.jboss.weld.tests.metadata.scanning.jboss.Baz;
import org.jboss.weld.tests.metadata.scanning.jboss.Garply;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MultiplePatternIncludeExcludeTest {

    public static final Asset BEANS_XML = createBeansXml(
            "<weld:scan>" +
                    "<weld:exclude pattern=\"" + escapeClassName(Bar.class) + "\"/>" +
                    "<weld:include pattern=\"" + escapeClassName(Foo.class) + "\"/>" +
                    "<weld:include pattern=\"" + escapeClassName(Baz.class) + "\"/>" +
                    "<weld:exclude pattern=\"" + escapeClassName(Qux.class) + "\"/>" +
                    "<weld:exclude pattern=\"" + escapeClassName(Corge.class) + "\"/>" +
                    "<weld:include pattern=\"" + escapeClassName(Garply.class) + "\"/>" +
                    "</weld:scan>");

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class).addClass(Utils.class)
                .addClasses(Bar.class, Foo.class, Baz.class, Qux.class, Corge.class, Garply.class)
                .addAsManifestResource(BEANS_XML, "beans.xml");
    }

    @Test
    public void test(BeanManager beanManager) {
        assert beanManager.getBeans(Qux.class).size() == 0;
        assert beanManager.getBeans(Foo.class).size() == 1;
        assert beanManager.getBeans(Baz.class).size() == 1;
        assert beanManager.getBeans(Bar.class).size() == 0;
        assert beanManager.getBeans(Corge.class).size() == 0;
        assert beanManager.getBeans(Garply.class).size() == 1;
    }

}
