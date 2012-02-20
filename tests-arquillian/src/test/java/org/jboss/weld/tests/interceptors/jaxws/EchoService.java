package org.jboss.weld.tests.interceptors.jaxws;


import javax.jws.WebMethod;

public interface EchoService
{
    @WebMethod
    public String sayHello(String arg0);

}
