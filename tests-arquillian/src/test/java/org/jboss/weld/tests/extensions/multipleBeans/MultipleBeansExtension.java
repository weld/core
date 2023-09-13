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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

import org.jboss.weld.test.util.annotated.TestAnnotatedTypeBuilder;

/**
 * Extension that registers addition types via the SPI
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 */
public class MultipleBeansExtension implements Extension {

    private boolean addedBlogFormatterSeen = false;

    public void addNewAnnotatedTypes(@Observes BeforeBeanDiscovery event)
            throws SecurityException, NoSuchFieldException, NoSuchMethodException {
        TestAnnotatedTypeBuilder<BlogFormatter> formatter = new TestAnnotatedTypeBuilder<BlogFormatter>(BlogFormatter.class);
        Field content = BlogFormatter.class.getField("content");
        formatter.addToField(content, new InjectLiteral());
        formatter.addToField(content, new AuthorLiteral("Bob"));
        Method format = BlogFormatter.class.getMethod("format");
        formatter.addToMethod(format, new ProducesLiteral());
        formatter.addToMethod(format, new FormattedBlogLiteral("Bob"));
        event.addAnnotatedType(formatter.create(), BlogFormatter.class.getSimpleName());

        TestAnnotatedTypeBuilder<BlogConsumer> consumer = new TestAnnotatedTypeBuilder<BlogConsumer>(BlogConsumer.class);
        consumer.addToClass(new ConsumerLiteral("Bob"));
        content = BlogConsumer.class.getField("blogContent");
        consumer.addToField(content, new InjectLiteral());
        consumer.addToField(content, new FormattedBlogLiteral("Bob"));
        event.addAnnotatedType(consumer.create(), BlogConsumer.class.getSimpleName());

        // two beans that are exactly the same
        // this is not very useful, however should still work
        TestAnnotatedTypeBuilder<UselessBean> uselessBuilder = new TestAnnotatedTypeBuilder<UselessBean>(UselessBean.class);
        event.addAnnotatedType(uselessBuilder.create(), UselessBean.class.getSimpleName());

    }

    public void observeProcessBlogFormatter(@Observes ProcessAnnotatedType<BlogFormatter> event) {
        AnnotatedType<BlogFormatter> type = event.getAnnotatedType();
        for (AnnotatedField<? super BlogFormatter> f : type.getFields()) {
            if (f.getJavaMember().getName().equals("content")) {
                if (f.isAnnotationPresent(Author.class)) {
                    if (f.getAnnotation(Author.class).name().equals("Bob")) {
                        addedBlogFormatterSeen = true;
                    }
                }
            }
        }
    }

    public boolean isAddedBlogFormatterSeen() {
        return addedBlogFormatterSeen;
    }

    private static class InjectLiteral extends AnnotationLiteral<Inject> implements Inject {

    }

    private static class ProducesLiteral extends AnnotationLiteral<Produces> implements Produces {

    }
}
