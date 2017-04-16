package org.jboss.weld.tests.ejb.duplicatenames;

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore("WFLY-3329")
@RunWith(Arquillian.class)
public class DuplicateEJBNamesInterceptionTest {

    private static final String BEANS_INTERCEPTOR = "<beans><interceptors><class>" + LogInterceptor.class.getName() + "</class></interceptors></beans>";

    @Deployment(testable = true)
    public static EnterpriseArchive deploy() {

        JavaArchive jar1 = ShrinkWrap.create(JavaArchive.class, "first.jar")
                .addAsManifestResource(new StringAsset(BEANS_INTERCEPTOR), "beans.xml")
                .addClasses(org.jboss.weld.tests.ejb.duplicatenames.first.MyEjbImpl.class);

        JavaArchive jar2 = ShrinkWrap.create(JavaArchive.class, "second.jar")
                .addAsManifestResource(new StringAsset(BEANS_INTERCEPTOR), "beans.xml")
                .addClasses(org.jboss.weld.tests.ejb.duplicatenames.second.MyEjbImpl.class);

        JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "lib.jar")
                .addClasses(LogInterceptor.class, Logged.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        WebArchive test = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(DuplicateEJBNamesInterceptionTest.class);

        return ShrinkWrap.create(EnterpriseArchive.class)
                .addAsModules(jar1, jar2)
                .addAsModule(Testable.archiveToTest(test))
                .addAsLibraries(lib);
    }

    @Inject
    private org.jboss.weld.tests.ejb.duplicatenames.first.MyEjbImpl first;

    @Inject
    private org.jboss.weld.tests.ejb.duplicatenames.second.MyEjbImpl second;

    @Test
    public void test() {
        first.call();
        second.call();
        Assert.assertEquals(2, LogInterceptor.QUEUE.size());
        Assert.assertEquals(org.jboss.weld.tests.ejb.duplicatenames.first.MyEjbImpl.MESSAGE, LogInterceptor.QUEUE.get(0));
        Assert.assertEquals(org.jboss.weld.tests.ejb.duplicatenames.second.MyEjbImpl.MESSAGE, LogInterceptor.QUEUE.get(1));
    }

}
