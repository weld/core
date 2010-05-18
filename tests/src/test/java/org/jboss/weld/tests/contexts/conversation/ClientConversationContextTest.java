/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.tests.contexts.conversation;

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import org.jboss.testharness.impl.packaging.Artifact;
import org.jboss.testharness.impl.packaging.Classes;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.jboss.testharness.impl.packaging.Resource;
import org.jboss.testharness.impl.packaging.Resources;
import org.jboss.testharness.impl.packaging.war.WebXml;
import org.jboss.weld.test.AbstractWeldTest;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @author Nicklas Karlsson
 * @author Dan Allen
 */
@Artifact(addCurrentPackage = false)
@Classes( { ConversationTestPhaseListener.class, Cloud.class })
@IntegrationTest(runLocally = true)
@Resources( { 
   @Resource(destination = "cloud.jspx", source = "cloud.jsf"),
   @Resource(destination="/WEB-INF/faces-config.xml", source="faces-config.xml")
})
@WebXml("web.xml")
public class ClientConversationContextTest extends AbstractWeldTest
{

   public static final String CID_REQUEST_PARAMETER_NAME = "cid";

   public static final String CID_HEADER_NAME = "org.jboss.jsr299.tck.cid";

   public static final String LONG_RUNNING_HEADER_NAME = "org.jboss.jsr299.tck.longRunning";

   @Test(groups = { "contexts" })
   public void testConversationPropagationToNonExistentConversationLeadsException() throws Exception
   {
      WebClient client = new WebClient();
      client.setThrowExceptionOnFailingStatusCode(false);
      Page page = client.getPage(getPath("/cloud.jsf", "org.jboss.jsr299"));
      assert page.getWebResponse().getStatusCode() == 500;
   }

   protected Boolean isLongRunning(Page page)
   {
      return Boolean.valueOf(page.getWebResponse().getResponseHeaderValue(LONG_RUNNING_HEADER_NAME));
   }

   protected String getPath(String viewId, String cid)
   {
      return getContextPath() + viewId + "?" + CID_REQUEST_PARAMETER_NAME + "=" + cid;
   }

   protected String getCid(Page page)
   {
      return page.getWebResponse().getResponseHeaderValue(CID_HEADER_NAME);
   }

}