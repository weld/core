package org.jboss.webbeans.test;

import javax.webbeans.DefinitionException;

import org.testng.annotations.Test;

@SpecVersion("20081222")
public class NewTest extends AbstractTest
{

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
   public void testNewBeanIsDependentScoped()
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanIsOfStandardDeploymentType()
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanIsHasOnlyNewBinding()
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoWebBeanName()
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasImplementationClassOfInjectionPointType()
   {
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasSameConstructorAsWrappedBean()
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasSameInitializerMethodsAsWrappedBean()
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasSameInjectedFieldsAsWrappedBean()
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
   @Test(groups = { "stub", "new" })
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
   @Test(groups = { "stub", "new" })
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
   @Test(groups = { "stub", "new" })
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoDisposalMethods()
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
   @Test(groups = { "stub", "new" })
   @SpecAssertion(section = "3.9")
   public void testNewBeanHasNoDecorators()
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
   public void testNewAnnotationMayBeAppliedToField()
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
