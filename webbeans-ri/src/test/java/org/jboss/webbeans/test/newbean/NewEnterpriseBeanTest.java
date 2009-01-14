package org.jboss.webbeans.test.newbean;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.Standard;

import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.binding.NewBinding;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.mock.MockWebBeanDiscovery;
import org.jboss.webbeans.test.newbean.invalid.NewAndOtherBindingType;
import org.jboss.webbeans.test.newbean.valid.AnnotatedConstructorParameter;
import org.jboss.webbeans.test.newbean.valid.AnnotatedField;
import org.jboss.webbeans.test.newbean.valid.AnnotatedInitializerParameter;
import org.jboss.webbeans.test.newbean.valid.AnnotatedProducerParameter;
import org.jboss.webbeans.test.newbean.valid.WrappedEnterpriseBean;
import org.jboss.webbeans.test.newbean.valid.WrappedSimpleBean;
import org.jboss.webbeans.util.Proxies.TypeInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SpecVersion("20081222")
public class NewEnterpriseBeanTest extends AbstractTest
{
   private EnterpriseBean<WrappedEnterpriseBean> wrappedEnterpriseBean;
   private NewEnterpriseBean<WrappedEnterpriseBean> newEnterpriseBean;
   
   @BeforeMethod
   public void initNewBean() {
      addToEjbCache(WrappedEnterpriseBean.class);
      wrappedEnterpriseBean = EnterpriseBean.of(WrappedEnterpriseBean.class, manager);
      manager.addBean(wrappedEnterpriseBean);
      newEnterpriseBean = NewEnterpriseBean.of(WrappedEnterpriseBean.class, manager);
      manager.addBean(newEnterpriseBean);
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
      assert Dependent.class.equals(newEnterpriseBean.getScopeType());
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
      assert Standard.class.equals(newEnterpriseBean.getDeploymentType());
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
      assert newEnterpriseBean.getBindings().size() == 1;
      assert newEnterpriseBean.getBindings().iterator().next().annotationType().equals(new NewBinding().annotationType());
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
      assert newEnterpriseBean.getName() == null;
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
   @Test(groups = { "new", "broken" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
      assert newEnterpriseBean.getType().equals(WrappedSimpleBean.class);
   }

   /**
    * If the parameter type satisfies the definition of a simple Web Bean
    * implementation class, Section 3.2.1, “Which Java classes are simple Web
    * Beans?”, then the Web Bean is a simple Web Bean. If the parameter type
    * satisfies the definition of an enterprise Web Bean implementation class,
    * Section 3.3.2, “Which EJBs are enterprise Web Beans?”, then the Web Bean
    * is an enterprise Web Bean.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanIsEnterpriseWebBeanIfParameterTypeIsEnterpriseWebBean()
   {
      assert wrappedEnterpriseBean.getType().equals(newEnterpriseBean.getType());
      assert manager.getEjbDescriptorCache().containsKey(newEnterpriseBean.getType());
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
      // N/A for EJB
      assert true;
//      assert wrappedEnterpriseBean.getConstructor().equals(newEnterpriseBean.getConstructor());
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
      assert newEnterpriseBean.getInitializerMethods().equals(wrappedEnterpriseBean.getInitializerMethods());
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
      Set<AnnotatedItem<?, ?>> wrappedBeanInjectionPoints = wrappedEnterpriseBean.getAnnotatedInjectionPoints();
      Set<AnnotatedItem<?, ?>> newBeanInjectionPoints = newEnterpriseBean.getAnnotatedInjectionPoints();
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
   @Test(groups = {"new", "stub" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoObservers()
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
   @Test(groups = { "new", "stub" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoProducerFields()
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
   @Test(groups = { "new", "stub" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoProducerMethods()
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
   public void testNewBeanHasNoDisposalMethods()
   {
      Class<?> type = TypeInfo.ofTypes(newEnterpriseBean.getTypes()).getSuperClass();
      assert manager.resolveDisposalMethods(type, newEnterpriseBean.getBindings().toArray(new Annotation[0])).isEmpty();
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
      Annotation[] bindingTypes = newEnterpriseBean.getBindings().toArray(new Annotation[0]);
      assert manager.resolveDecorators(newEnterpriseBean.getTypes(), bindingTypes).isEmpty();
   }

   /**
    * The @New annotation or <New> element may be applied to any field of a Web
    * Bean implementation class or to any parameter of a producer method,
    * initializer method, disposal method or Web Bean constructor where the type
    * of the field or parameter is a concrete Java type which satisfies the
    * requirements of a simple Web Bean implementation class or enterprise Web
    * Bean implementation class.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationMayBeAppliedToField()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(AnnotatedField.class, WrappedEnterpriseBean.class));
      webBeansBootstrap.boot();
      assert manager.resolveByType(WrappedEnterpriseBean.class, new NewBinding()).size() == 1;
   }

   /**
    * The @New annotation or <New> element may be applied to any field of a Web
    * Bean implementation class or to any parameter of a producer method,
    * initializer method, disposal method or Web Bean constructor where the type
    * of the field or parameter is a concrete Java type which satisfies the
    * requirements of a simple Web Bean implementation class or enterprise Web
    * Bean implementation class.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationMayBeAppliedToProducerMethodParameter()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(AnnotatedProducerParameter.class, WrappedEnterpriseBean.class));
      webBeansBootstrap.boot();
      assert manager.resolveByType(WrappedEnterpriseBean.class, new NewBinding()).size() == 1;
   }

   /**
    * The @New annotation or <New> element may be applied to any field of a Web
    * Bean implementation class or to any parameter of a producer method,
    * initializer method, disposal method or Web Bean constructor where the type
    * of the field or parameter is a concrete Java type which satisfies the
    * requirements of a simple Web Bean implementation class or enterprise Web
    * Bean implementation class.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationMayBeAppliedToInitializerMethodParameter()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(AnnotatedInitializerParameter.class, WrappedEnterpriseBean.class));
      webBeansBootstrap.boot();
      assert manager.resolveByType(WrappedEnterpriseBean.class, new NewBinding()).size() == 1;
   }

   /**
    * The @New annotation or <New> element may be applied to any field of a Web
    * Bean implementation class or to any parameter of a producer method,
    * initializer method, disposal method or Web Bean constructor where the type
    * of the field or parameter is a concrete Java type which satisfies the
    * requirements of a simple Web Bean implementation class or enterprise Web
    * Bean implementation class.
    */
   @Test(groups = { "new" })
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationMayBeAppliedToConstructorMethodParameter()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(AnnotatedConstructorParameter.class, WrappedEnterpriseBean.class));
      webBeansBootstrap.boot();
      assert manager.resolveByType(WrappedEnterpriseBean.class, new NewBinding()).size() == 1;
   }

   /**
    * If the @New binding type appears in conjunction with some other binding
    * type, or is specified for a field or parameter of a type which does not
    * satisfy the definition of a simple Web Bean implementation class or
    * enterprise Web Bean implementation class, a DefinitionException is thrown
    * by the container at deployment time.
    */
   @Test(groups = { "new" , "broken"}, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.9")
   public void testNewAnnotationCannotAppearInConjunctionWithOtherBindingType()
   {
      webBeansBootstrap.setWebBeanDiscovery(new MockWebBeanDiscovery(NewAndOtherBindingType.class));
      webBeansBootstrap.boot();      
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
