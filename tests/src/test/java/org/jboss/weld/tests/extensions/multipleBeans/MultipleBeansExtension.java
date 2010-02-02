package org.jboss.weld.tests.extensions.multipleBeans;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.jboss.weld.tests.util.annotated.TestAnnotatedTypeBuilder;

/**
 * Extension that registers addition types via the SPI
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class MultipleBeansExtension implements Extension
{

   public void addNewAnnotatedTypes(@Observes BeforeBeanDiscovery event) throws SecurityException, NoSuchFieldException, NoSuchMethodException
   {
      TestAnnotatedTypeBuilder<BlogFormatter> formatter = new TestAnnotatedTypeBuilder<BlogFormatter>(BlogFormatter.class);
      Field content = BlogFormatter.class.getField("content");
      formatter.addToField(content, new InjectLiteral());
      formatter.addToField(content, new AuthorLiteral("Bob"));
      Method format = BlogFormatter.class.getMethod("format");
      formatter.addToMethod(format, new ProducesLiteral());
      formatter.addToMethod(format, new FormattedBlogLiteral("Bob"));
      event.addAnnotatedType(formatter.create());

      TestAnnotatedTypeBuilder<BlogConsumer> consumer = new TestAnnotatedTypeBuilder<BlogConsumer>(BlogConsumer.class);
      consumer.addToClass(new ConsumerLiteral("Bob"));
      content = BlogConsumer.class.getField("blogContent");
      consumer.addToField(content, new InjectLiteral());
      consumer.addToField(content, new FormattedBlogLiteral("Bob"));
      event.addAnnotatedType(consumer.create());

      // two beans that are exactly the same
      // this is not very useful, however should still work
      TestAnnotatedTypeBuilder<UselessBean> uselessBuilder = new TestAnnotatedTypeBuilder<UselessBean>(UselessBean.class);
      event.addAnnotatedType(uselessBuilder.create());

   }

   private static class InjectLiteral extends AnnotationLiteral<Inject> implements Inject
   {

   }

   private static class ProducesLiteral extends AnnotationLiteral<Produces> implements Produces
   {

   }
}
