package org.jboss.webbeans.test.newbean;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Standard;

import org.jboss.webbeans.bean.BeanFactory;
import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.binding.NewBinding;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;
import org.jboss.webbeans.test.newbean.valid.Sample;
import org.jboss.webbeans.test.newbean.valid.WrappedBean;
import org.jboss.webbeans.util.Proxies.TypeInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SpecVersion("20081222")
public class NewBeanTest extends AbstractTest
{
   private SimpleBean<WrappedBean> wrappedBean;
   private NewSimpleBean<WrappedBean> newBean;
   
   @BeforeMethod
   public void initNewBean() {
      wrappedBean = BeanFactory.createSimpleBean(WrappedBean.class, manager);
      manager.addBean(wrappedBean);
      newBean = BeanFactory.createNewSimpleBean(WrappedBean.class, manager);
      manager.addBean(newBean);
   }
   
   /**
    * When the built-in binding type @New is applied to an injection point, a
    * Web Bean is implicitly defined with: • scope @Dependent, • deployment type
    * 
    * @Standard, • @New as the only binding annotation, • no Web Bean name, • no
    *            stereotypes, and such that • the implementation class is the
    *            declared type of the injection point.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanIsDependentScoped()
   {
      assert Dependent.class.equals(newBean.getScopeType());
   }

   /**
    * When the built-in binding type @New is applied to an injection point, a
    * Web Bean is implicitly defined with: • scope @Dependent, • deployment type
    * 
    * @Standard, • @New as the only binding annotation, • no Web Bean name, • no
    *            stereotypes, and such that • the implementation class is the
    *            declared type of the injection point.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanIsOfStandardDeploymentType()
   {
      assert Standard.class.equals(newBean.getDeploymentType());
   }

   /**
    * When the built-in binding type @New is applied to an injection point, a
    * Web Bean is implicitly defined with: • scope @Dependent, • deployment type
    * 
    * @Standard, • @New as the only binding annotation, • no Web Bean name, • no
    *            stereotypes, and such that • the implementation class is the
    *            declared type of the injection point.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanIsHasOnlyNewBinding()
   {
      assert newBean.getBindingTypes().size() == 1;
      assert newBean.getBindingTypes().iterator().next().annotationType().equals(new NewBinding().annotationType());
   }

   /**
    * When the built-in binding type @New is applied to an injection point, a
    * Web Bean is implicitly defined with: • scope @Dependent, • deployment type
    * 
    * @Standard, • @New as the only binding annotation, • no Web Bean name, • no
    *            stereotypes, and such that • the implementation class is the
    *            declared type of the injection point.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoWebBeanName()
   {
      assert newBean.getName() == null;
   }

   /**
    * When the built-in binding type @New is applied to an injection point, a
    * Web Bean is implicitly defined with: • scope @Dependent, • deployment type
    * 
    * @Standard, • @New as the only binding annotation, • no Web Bean name, • no
    *            stereotypes, and such that • the implementation class is the
    *            declared type of the injection point.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoStereotypes()
   {
      assert false;
   }

   /**
    * When the built-in binding type @New is applied to an injection point, a
    * Web Bean is implicitly defined with: • scope @Dependent, • deployment type
    * 
    * @Standard, • @New as the only binding annotation, • no Web Bean name, • no
    *            stereotypes, and such that • the implementation class is the
    *            declared type of the injection point.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      assert newBean.getType().equals(WrappedBean.class);
   }

   /**
    * If the parameter type satisfies the definition of a simple Web Bean
    * implementation class, Section 3.2.1, “Which Java classes are simple Web
    * Beans?”, then the Web Bean is a simple Web Bean. If the parameter type
    * satisfies the definition of an enterprise Web Bean implementation class,
    * Section 3.3.2, “Which EJBs are enterprise Web Beans?”, then the Web Bean
    * is an enterprise Web Bean.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanIsSimpleWebBeanIfParameterTypeIsSimpleWebBean()
   {
      // TODO: has to be?
      assert false;
   }

   /**
    * If the parameter type satisfies the definition of a simple Web Bean
    * implementation class, Section 3.2.1, “Which Java classes are simple Web
    * Beans?”, then the Web Bean is a simple Web Bean. If the parameter type
    * satisfies the definition of an enterprise Web Bean implementation class,
    * Section 3.3.2, “Which EJBs are enterprise Web Beans?”, then the Web Bean
    * is an enterprise Web Bean.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanIsEnterpriseWebBeanIfParameterTypeIsEnterpriseWebBean()
   {
      // TODO: has to be?
      assert false;
   }

   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasSameConstructorAsWrappedBean()
   {
      assert wrappedBean.getConstructor().equals(newBean.getConstructor());
   }

   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasSameInitializerMethodsAsWrappedBean()
   {
      assert newBean.getInitializerMethods().equals(wrappedBean.getInitializerMethods());
   }

   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasSameInjectedFieldsAsWrappedBean()
   {
      Set<AnnotatedItem<?, ?>> wrappedBeanInjectionPoints = wrappedBean.getInjectionPoints();
      Set<AnnotatedItem<?, ?>> newBeanInjectionPoints = newBean.getInjectionPoints();
      assert wrappedBeanInjectionPoints.equals(newBeanInjectionPoints);
   }
   
   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = {"new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoObservers()
   {
      assert newBean.getObserverMethods().isEmpty();
   }

   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoProducerFields()
   {
      assert newBean.getProducerFields().isEmpty();
   }

   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoProducerMethods()
   {
      assert newBean.getProducerMethods().isEmpty();
   }

   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoDisposalMethods()
   {
      Class<?> type = TypeInfo.ofTypes(newBean.getTypes()).getSuperClass();
      assert manager.resolveDisposalMethods(type, newBean.getBindingTypes().toArray(new Annotation[0])).isEmpty();
   }

   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasSameInterceptorMethodsAsWrappedBean()
   {
      assert false;
   }

   /**
    * Furthermore, this Web Bean: • has the same Web Bean constructor,
    * initializer methods and injected fields as a Web Bean defined using
    * annotations— that is, it has any Web Bean constructor, initializer method
    * or injected field declared by annotations that appear on the
    * implementation class, • has no observer methods, producer methods or
    * fields or disposal methods, • has the same interceptors as a Web Bean
    * defined using annotations—that is, it has all the interceptor binding
    * types declared by annotations that appear on the implementation class, and
    * • has no decorators.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoDecorators()
   {
      Annotation[] bindingTypes = newBean.getBindingTypes().toArray(new Annotation[0]);
      assert manager.resolveDecorators(newBean.getTypes(), bindingTypes).isEmpty();
   }

   /**
    * The @New annotation or <New> element may be applied to any field of a Web
    * Bean implementation class or to any parameter of a producer method,
    * initializer method, disposal method or Web Bean constructor where the type
    * of the field or parameter is a concrete Java type which satisfies the
    * requirements of a simple Web Bean implementation class or enterprise Web
    * Bean implementation class.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationMayBeAppliedToField()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(Sample.class));
      webBeansBootstrap.boot();
      assert manager.resolveByType(WrappedBean.class, new NewBinding()).size() == 1;
   }

   /**
    * The @New annotation or <New> element may be applied to any field of a Web
    * Bean implementation class or to any parameter of a producer method,
    * initializer method, disposal method or Web Bean constructor where the type
    * of the field or parameter is a concrete Java type which satisfies the
    * requirements of a simple Web Bean implementation class or enterprise Web
    * Bean implementation class.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationMayBeAppliedToProducerMethodParameter()
   {
      assert false;
   }

   /**
    * The @New annotation or <New> element may be applied to any field of a Web
    * Bean implementation class or to any parameter of a producer method,
    * initializer method, disposal method or Web Bean constructor where the type
    * of the field or parameter is a concrete Java type which satisfies the
    * requirements of a simple Web Bean implementation class or enterprise Web
    * Bean implementation class.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationMayBeAppliedToInitializerMethodParameter()
   {
      assert false;
   }

   /**
    * The @New annotation or <New> element may be applied to any field of a Web
    * Bean implementation class or to any parameter of a producer method,
    * initializer method, disposal method or Web Bean constructor where the type
    * of the field or parameter is a concrete Java type which satisfies the
    * requirements of a simple Web Bean implementation class or enterprise Web
    * Bean implementation class.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationMayBeAppliedToConstructorMethodParameter()
   {
      assert false;
   }

   /**
    * If the @New binding type appears in conjunction with some other binding
    * type, or is specified for a field or parameter of a type which does not
    * satisfy the definition of a simple Web Bean implementation class or
    * enterprise Web Bean implementation class, a DefinitionException is thrown
    * by the container at deployment time.
    */
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationCannotAppearInConjunctionWithOtherBindingType()
   {
      assert false;
   }

   /**
    * If the @New binding type appears in conjunction with some other binding
    * type, or is specified for a field or parameter of a type which does not
    * satisfy the definition of a simple Web Bean implementation class or
    * enterprise Web Bean implementation class, a DefinitionException is thrown
    * by the container at deployment time.
    */
   @Test(groups = { "stub", "new" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationCannotBeAppliedToNonWebBeanImplementationClass()
   {
      assert false;
   }

   /**
    * No Web Bean defined using annotations or XML may explicitly declare @New
    * as a binding type
    */
   @Test(groups = { "stub", "new" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationCannotBeExplicitlyDeclared()
   {
      assert false;
   }
   
   
}
