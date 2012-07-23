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
package org.jboss.resteasy.cdi.interceptors;

import java.util.ArrayList;

import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version $Revision: 1.1 $
 *
 * Copyright May 7, 2012
 */
@Path("/")
@RequestScoped
@Interceptors ({Interceptor0.class})
@TestBinding(placement="CLASS")
public class InterceptorResource
{  
   @POST
   @Path("test")
   @Produces(MediaType.TEXT_PLAIN)
   @Interceptors ({Interceptor1.class})
   @TestBinding(placement="METHOD")
   public Response test()
   {
      ArrayList<Object> visitList = VisitList.getList();
      for (int i = 0; i < visitList.size(); i++)
      {
         System.out.println(visitList.get(i).toString());
      }
      boolean status = true;
//      if (!(visitList.get(0) instanceof Interceptor0))
//      {
//         System.out.println("Interceptor0 missing");
//         status = false;
//      }
//      if (!(visitList.get(1) instanceof Interceptor2))
//      {
//         System.out.println("Interceptor2 missing");
//         status = false;
//      }
//      if (!(visitList.get(2) instanceof Interceptor1))
//      {
//         System.out.println("Interceptor1 missing");
//         status = false;
//      }
//      if (!(visitList.get(3) instanceof Interceptor3))
//      {
//         System.out.println("Interceptor3 missing");
//         status = false;
//      }
      return status ? Response.ok().build() : Response.serverError().build();
   }
}
