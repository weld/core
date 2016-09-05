package org.jboss.weld.tests.servlet.crosscontext;

import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class Deployments {

    public static Archive<?> deployment(Class<?> servletClass) {

        WebArchive war1 = ShrinkWrap.create(WebArchive.class, "app1.war")
                .addClass(servletClass)
                .addAsWebInfResource(new StringAsset(""), "beans.xml");

        WebArchive war2 = ShrinkWrap.create(WebArchive.class, "app2.war")
                .addClass(IncludedServlet.class)
                .addAsWebInfResource(new StringAsset(""), "beans.xml");

        return ShrinkWrap.create(EnterpriseArchive.class)
                .addAsModule(Testable.archiveToTest(war1))
                .addAsModule(war2);
    }
}
