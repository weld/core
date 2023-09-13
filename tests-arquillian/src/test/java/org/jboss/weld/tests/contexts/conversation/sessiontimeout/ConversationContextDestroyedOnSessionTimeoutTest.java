/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.conversation.sessiontimeout;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * This test only verifies that PreDestroy callback is called correctly on a conversation scoped bean when the HTTP session
 * times out.
 *
 * @author Martin Kouba
 * @author Matej Novotny
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class ConversationContextDestroyedOnSessionTimeoutTest {

    @ArquillianResource
    private URL contextPath;

    @Deployment(testable = false)
    public static WebArchive createTestArchive() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(ConversationContextDestroyedOnSessionTimeoutTest.class,
                                Utils.ARCHIVE_TYPE.WAR))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(Foo.class, TestServlet.class, ActionSequence.class, SessionListener.class);
    }

    @Test
    public void testConversationContextDestroyedCorrectly() throws FailingHttpStatusCodeException, MalformedURLException,
            IOException, InterruptedException {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        String cid = client.getPage(contextPath + "/init").getWebResponse().getContentAsString();
        ActionSequence sequence = new ActionSequence();
        sequence.add(Foo.class.getSimpleName() + "init");
        sequence.add(Foo.class.getSimpleName() + "ping");
        sequence.add(SessionListener.class.getSimpleName() + "destroyed");
        sequence.add(Foo.class.getSimpleName() + "destroy");
        // we need to wait over 1s for session to timeout and then attempt to send another request
        Thread.sleep(1200L);
        assertEquals(sequence.dataToCsv(), client.getPage(contextPath + "/test" + "?cid=" + cid).getWebResponse()
                .getContentAsString().trim());
    }

}
