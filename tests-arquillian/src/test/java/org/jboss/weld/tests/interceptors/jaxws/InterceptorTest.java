package org.jboss.weld.tests.interceptors.jaxws;

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

@RunWith(Arquillian.class)
public class InterceptorTest {
    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "echo_service.war")
                .addPackages(true, "org.jboss.weld.tests.interceptors.jaxws")
                .addAsWebInfResource(InterceptorTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(new ByteArrayAsset("<beans><interceptors><class>org.jboss.weld.tests.interceptors.jaxws.TestInterceptorImpl</class></interceptors></beans>".getBytes()), ArchivePaths.create("beans.xml"));
    }

    @Test
    public void echoTest() throws Exception {
        EchoService echoClient = createEchoServiceClient();

        Assert.assertEquals("Hello Paul", echoClient.sayHello("Paul"));
        Assert.assertNotNull("Interceptor not initialised", TestInterceptorImpl.counter);
        Assert.assertTrue("Interceptor not invoked", TestInterceptorImpl.counter > 0);
    }

    private EchoService createEchoServiceClient() {
        try {

            URL wsdlLocation = new URL("http://localhost:8080/echo_service/EchoService?wsdl");
            QName serviceName = new QName("http://org.jboss.weld/tests/interceptors/jaxws", "EchoServiceService");
            QName portName = new QName("http://org.jboss.weld/tests/interceptors/jaxws", "EchoServicePort");

            Service service = Service.create(wsdlLocation, serviceName);
            return service.getPort(portName, EchoService.class);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
