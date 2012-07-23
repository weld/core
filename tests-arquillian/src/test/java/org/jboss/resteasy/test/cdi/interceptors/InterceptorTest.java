/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.resteasy.test.cdi.interceptors;

import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.swing.text.Utilities;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.cdi.interceptors.Interceptor2;
import org.jboss.resteasy.cdi.interceptors.Interceptor3;
import org.jboss.resteasy.cdi.interceptors.InterceptorResource;
import org.jboss.resteasy.cdi.interceptors.Interceptor0;
import org.jboss.resteasy.cdi.interceptors.Interceptor1;
import org.jboss.resteasy.cdi.interceptors.JaxRsActivator;
import org.jboss.resteasy.cdi.interceptors.TestBinding;
import org.jboss.resteasy.cdi.interceptors.VisitList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This is a collection of tests addressed to the interactions of 
 * Resteasy, CDI, EJB, and so forth in the context of a JEE Application Server.
 * 
 * It tests the injection of a variety of beans into Resteasy objects.
 * 
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version $Revision: 1.1 $
 *
 * Copyright May 8, 2012
 */
@RunWith(Arquillian.class)
public class InterceptorTest
{
	@Deployment
	public static Archive<?> createTestArchive()
	{
		WebArchive war = ShrinkWrap.create(WebArchive.class, "resteasy-cdi-ejb-test.war")
		    .addClasses(JaxRsActivator.class, Utilities.class)
            .addClasses(InterceptorResource.class, Interceptor0.class, Interceptor1.class)
            .addClasses(TestBinding.class, Interceptor2.class, Interceptor3.class)
            .addClasses(VisitList.class)
		    .addAsWebInfResource("interceptorBeans.xml", "beans.xml");
	   System.out.println(war.toString(true));
	   return war;
	}
	   
	/**
	 */
	@Test
	public void test() throws Exception
	{
	   System.out.println("starting testVerifyScopes()");
	   ClientRequest request = new ClientRequest("http://localhost:8080/resteasy-cdi-ejb-test/rest/test/");
	   ClientResponse<?> response = request.post();
	   System.out.println("Status: " + response.getStatus());
	   assertEquals(200, response.getStatus());
	   response.releaseConnection();
	}
}
