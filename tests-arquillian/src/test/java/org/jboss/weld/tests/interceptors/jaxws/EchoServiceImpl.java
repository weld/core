package org.jboss.weld.tests.interceptors.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService(serviceName = "EchoServiceService", portName = "EchoServicePort",
        name = "EchoServiceImpl", targetNamespace = "http://org.jboss.weld/tests/interceptors/jaxws")
public class EchoServiceImpl implements EchoService
{
    @WebMethod
    @TestInterceptor
    public String sayHello(String msg)
    {
        return "Hello " + msg;
    }

}