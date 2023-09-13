package org.jboss.weld.tests.contexts.conversation.weld1262;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 *
 * @author tremes
 *
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class Weld1262Test {

    @ArquillianResource
    URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(Weld1262Test.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(Crossroad.class, Guide.class)
                .addAsWebResource(Weld1262Test.class.getPackage(), "crossroad.xhtml", "crossroad.xhtml")
                .addAsWebResource(Weld1262Test.class.getPackage(), "road.xhtml", "road.xhtml")
                .addAsWebInfResource(Weld1262Test.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(Weld1262Test.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

    }

    @Test
    public void testConversationPropagatedByNavigationHandler() throws Exception {

        HtmlPage main = startConversation();
        HtmlPage road = getFirstMatchingElement(main, HtmlSubmitInput.class, "guide").click();
        assertEquals("Guide is active", getFirstMatchingElement(road, HtmlSpan.class, "guideMessage").getTextContent());

    }

    @Test
    public void testConversationNotPropagatedByFacesRedirect() throws Exception {

        HtmlPage main = startConversation();
        HtmlPage road = getFirstMatchingElement(main, HtmlSubmitInput.class, "redirect").click();
        assertEquals("Guide is not active", getFirstMatchingElement(road, HtmlSpan.class, "guideMessage").getTextContent());
    }

    public HtmlPage startConversation() throws Exception {

        WebClient client = new WebClient();
        HtmlPage main = client.getPage(url.toString().concat("crossroad.jsf"));

        main = getFirstMatchingElement(main, HtmlSubmitInput.class, "begin").click();
        String cid = getFirstMatchingElement(main, HtmlSpan.class, "cid").getTextContent();
        assertTrue(Integer.valueOf(cid) > 0);
        return main;
    }

    protected <T extends HtmlElement> T getFirstMatchingElement(HtmlPage page, Class<T> elementClass, String id) {

        Set<T> inputs = getElements(page.getBody(), elementClass);
        for (T input : inputs) {
            if (input.getId().contains(id)) {
                return input;
            }
        }
        return null;
    }

    protected <T> Set<T> getElements(HtmlElement rootElement, Class<T> elementClass) {
        Set<T> result = new HashSet<T>();

        for (HtmlElement element : rootElement.getHtmlElementDescendants()) {
            result.addAll(getElements(element, elementClass));
        }

        if (elementClass.isInstance(rootElement)) {
            result.add(elementClass.cast(rootElement));
        }
        return result;

    }

}
