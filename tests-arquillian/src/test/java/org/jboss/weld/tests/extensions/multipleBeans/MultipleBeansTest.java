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
package org.jboss.weld.tests.extensions.multipleBeans;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.test.util.annotated.TestAnnotatedTypeBuilder;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Tests that it is possible to add multiple beans with the same java class type
 * through the SPI
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class MultipleBeansTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(MultipleBeansTest.class))
                .addPackage(MultipleBeansTest.class.getPackage())
                .addPackage(TestAnnotatedTypeBuilder.class.getPackage())
                .addClass(Utils.class)
                .addAsServiceProvider(Extension.class, MultipleBeansExtension.class);
    }

    @Inject
    private BeanManagerImpl beanManager;

    @Test
    // WELD-406
    public void testFormatterRegistered() {
        // test that we have added two beans with the same qualifiers
        Assert.assertEquals(2, Utils.getBeans(beanManager, BlogFormatter.class).size());
        // test that the beans which have different producer methods produce
        // different values
        Assert.assertEquals("+Bob's content+", Utils.getReference(beanManager, String.class, new FormattedBlogLiteral("Bob")));
        Assert.assertEquals("+Barry's content+",
                Utils.getReference(beanManager, String.class, new FormattedBlogLiteral("Barry")));
    }

    @Test
    // WELD-406
    public void testBlogConsumed() {
        // test that the two different BlogConsumers have been registered
        // correctly
        BlogConsumer consumer = Utils.getReference(beanManager, BlogConsumer.class, new ConsumerLiteral("Barry"));
        Assert.assertEquals("+Barry's content+", consumer.blogContent);
        consumer = Utils.getReference(beanManager, BlogConsumer.class, new ConsumerLiteral("Bob"));
        Assert.assertEquals("+Bob's content+", consumer.blogContent);
    }

    /**
     * Apparently it is not possible to add two beans that are exactly the same.
     * Even though this is not very useful it should still be possible.
     */
    @Test
    // WELD-406
    public void testTwoBeansExactlyTheSame() {
        Assert.assertEquals(2, beanManager.getBeans(UselessBean.class).size());
    }

}
