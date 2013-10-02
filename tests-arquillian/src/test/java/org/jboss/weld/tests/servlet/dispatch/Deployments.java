package org.jboss.weld.tests.servlet.dispatch;

import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployments {

    public static Archive<?> deployment(Class<?> servletClass, boolean includeWebXml) {

        WebArchive war1 = ShrinkWrap.create(WebArchive.class, "app1.war")
                .addClasses(servletClass, FirstServlet.class, FirstBean.class, FirstConversationScopedBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "app2.war")
                .addClasses(SecondServlet.class, SecondBean.class, SecondConversationScopedBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        if (includeWebXml) {
            war1.addAsWebInfResource(Deployments.class.getPackage(), "web.xml", "web.xml");
            war2.addAsWebInfResource(Deployments.class.getPackage(), "web.xml", "web.xml");
        }

        JavaArchive library = ShrinkWrap.create(JavaArchive.class)
                .addClass(TestBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(EnterpriseArchive.class)
                .addAsModule(Testable.archiveToTest(war1))
                .addAsModule(war2)
                .addAsLibrary(library);
    }
}
