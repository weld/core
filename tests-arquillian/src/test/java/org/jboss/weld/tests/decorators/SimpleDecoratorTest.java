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
package org.jboss.weld.tests.decorators;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
@RunWith(Arquillian.class)
public class SimpleDecoratorTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SimpleDecoratorTest.class))
                .decorate(SimpleDecorator1.class, SimpleDecorator2.class)
                .addPackage(SimpleDecoratorTest.class.getPackage());
    }

    @Test
    public void testSimpleDecorator(SimpleBean simpleBean) {
        resetDecorators();
        simpleBean.resetInvokedFlag();
        Assert.assertEquals(1, simpleBean.echo1(1));
        assertDecoratorsInvoked(true, false, false, false);
        Assert.assertTrue(simpleBean.isInvoked());

        resetDecorators();
        simpleBean.resetInvokedFlag();
        Assert.assertEquals(2, simpleBean.echo2(2));
        assertDecoratorsInvoked(false, false, true, false);
        Assert.assertTrue(simpleBean.isInvoked());

        resetDecorators();
        simpleBean.resetInvokedFlag();
        Assert.assertEquals(3, simpleBean.echo3(3));
        assertDecoratorsInvoked(false, true, false, true);

        Assert.assertTrue(simpleBean.isInvoked());

        resetDecorators();
        simpleBean.resetInvokedFlag();
        Assert.assertEquals(4, simpleBean.echo4(4));
        assertDecoratorsInvoked(false, false, false, false);

        Assert.assertTrue(simpleBean.isInvoked());
    }

    private void resetDecorators() {
        SimpleDecorator1.reset();
        SimpleDecorator2.reset();
    }

    private void assertDecoratorsInvoked(boolean decorator1Echo1, boolean decorator1Echo3, boolean decorator2Echo2,
            boolean decorator2Echo3) {
        Assert.assertEquals(decorator1Echo1, SimpleDecorator1.echo1);
        Assert.assertEquals(decorator1Echo3, SimpleDecorator1.echo3);
        Assert.assertEquals(decorator2Echo2, SimpleDecorator2.echo2);
        Assert.assertEquals(decorator2Echo3, SimpleDecorator2.echo3);
    }
}
