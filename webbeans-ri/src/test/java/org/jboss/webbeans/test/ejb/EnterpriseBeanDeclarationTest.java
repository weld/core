package org.jboss.webbeans.test.ejb;

import javax.webbeans.DefinitionException;
import javax.webbeans.DeploymentException;

import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.test.AbstractTest;
import org.jboss.webbeans.test.SpecAssertion;
import org.jboss.webbeans.test.SpecVersion;
import org.jboss.webbeans.test.beans.Giraffe;
import org.jboss.webbeans.test.beans.Leopard;
import org.jboss.webbeans.test.ejb.invalid.Beagle;
import org.jboss.webbeans.test.ejb.invalid.Boxer;
import org.jboss.webbeans.test.ejb.invalid.Bullmastiff;
import org.jboss.webbeans.test.ejb.invalid.Dachshund;
import org.jboss.webbeans.test.ejb.invalid.GreatDane;
import org.jboss.webbeans.test.ejb.invalid.Greyhound;
import org.jboss.webbeans.test.ejb.invalid.Husky;
import org.jboss.webbeans.test.ejb.invalid.IrishTerrier;
import org.jboss.webbeans.test.ejb.invalid.Pekingese;
import org.jboss.webbeans.test.ejb.invalid.Pug;
import org.jboss.webbeans.test.ejb.valid.Laika;
import org.jboss.webbeans.test.ejb.valid.Pitbull;
import org.jboss.webbeans.util.BeanFactory;
import org.testng.annotations.Test;

@SpecVersion("20081206")
@SuppressWarnings("unused")
public class EnterpriseBeanDeclarationTest extends AbstractTest
{

   /**
    * An EJB stateless session bean must belong to the @Dependent pseudo-scope.
    * If an enterprise Web Bean specifies an illegal scope, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time
    */
   @Test(groups = { "enterpriseBeans" })
   @SpecAssertion(section = "3.3")
   public void testStatelessWithDependentScopeOK()
   {
      EnterpriseBean<Giraffe> giraffe = BeanFactory.createEnterpriseBean(Giraffe.class);
   }

   /**
    * An EJB stateless session bean must belong to the @Dependent pseudo-scope.
    * If an enterprise Web Bean specifies an illegal scope, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time
    */
   @Test(groups = { "enterpriseBeans" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithRequestScopeFails()
   {
      EnterpriseBean<Beagle> beagle = BeanFactory.createEnterpriseBean(Beagle.class);
   }

   /**
    * An EJB stateless session bean must belong to the @Dependent pseudo-scope.
    * If an enterprise Web Bean specifies an illegal scope, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time
    */

   @Test(groups = { "enterpriseBeans" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithConversationScopeFails()
   {
      EnterpriseBean<Boxer> boxer = BeanFactory.createEnterpriseBean(Boxer.class);
   }

   /**
    * An EJB stateless session bean must belong to the @Dependent pseudo-scope.
    * If an enterprise Web Bean specifies an illegal scope, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time
    */
   @Test(groups = { "enterpriseBeans" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithSessionScopeFails()
   {
      EnterpriseBean<Bullmastiff> boxer = BeanFactory.createEnterpriseBean(Bullmastiff.class);
   }

   /**
    * An EJB stateless session bean must belong to the @Dependent pseudo-scope.
    * If an enterprise Web Bean specifies an illegal scope, a
    * DefinitionException is thrown by the Web Bean manager at initialization
    * time
    */
   @Test(groups = { "enterpriseBeans" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testStatelessWithApplicationScopeFails()
   {
      EnterpriseBean<Dachshund> dachshund = BeanFactory.createEnterpriseBean(Dachshund.class);
   }

   /**
    * An EJB singleton bean must belong to either the @ApplicationScoped scope
    * or to the @Dependent pseudo-scope. If an enterprise Web Bean specifies an
    * illegal scope, a DefinitionException is thrown by the Web Bean manager at
    * initialization time
    */
   @Test(groups = { "enterpriseBeans" })
   @SpecAssertion(section = "3.3")
   public void testSingletonWithDependentScopeOK()
   {
      EnterpriseBean<GreatDane> greatDane = BeanFactory.createEnterpriseBean(GreatDane.class);
   }

   /**
    * An EJB singleton bean must belong to either the @ApplicationScoped scope
    * or to the @Dependent pseudo-scope. If an enterprise Web Bean specifies an
    * illegal scope, a DefinitionException is thrown by the Web Bean manager at
    * initialization time
    */
   @Test(groups = { "enterpriseBeans" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithRequestScopeFails()
   {
      EnterpriseBean<Greyhound> greyhound = BeanFactory.createEnterpriseBean(Greyhound.class);
   }

   /**
    * An EJB singleton bean must belong to either the @ApplicationScoped scope
    * or to the @Dependent pseudo-scope. If an enterprise Web Bean specifies an
    * illegal scope, a DefinitionException is thrown by the Web Bean manager at
    * initialization time
    */
   @Test(groups = { "enterpriseBeans" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithConversationScopeFails()
   {
      EnterpriseBean<Husky> husky = BeanFactory.createEnterpriseBean(Husky.class);
   }

   /**
    * An EJB singleton bean must belong to either the @ApplicationScoped scope
    * or to the @Dependent pseudo-scope. If an enterprise Web Bean specifies an
    * illegal scope, a DefinitionException is thrown by the Web Bean manager at
    * initialization time
    */
   @Test(groups = { "enterpriseBeans" }, expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testSingletonWithSessionScopeFails()
   {
      EnterpriseBean<IrishTerrier> irishTerrier = BeanFactory.createEnterpriseBean(IrishTerrier.class);
   }

   /**
    * An EJB singleton bean must belong to either the @ApplicationScoped scope
    * or to the @Dependent pseudo-scope. If an enterprise Web Bean specifies an
    * illegal scope, a DefinitionException is thrown by the Web Bean manager at
    * initialization time
    */
   @Test(groups = { "enterpriseBeans" })
   @SpecAssertion(section = "3.3")
   public void testSingletonWithApplicationScopeOK()
   {
      EnterpriseBean<Laika> laika = BeanFactory.createEnterpriseBean(Laika.class);
   }

   /**
    * However, in any deployment, there may be at most one most specialized
    * enabled enterprise Web Bean for any particular EJB enterprise bean.
    * Therefore, for each distinct EJB name in a module, there is at most one
    * Web Bean that may be called at runtime. If there is more than one most
    * specialized enabled enterprise Web Bean for a particular EJB enterprise
    * bean, a DeploymentException is thrown by the Web Bean manager at
    * initialization time.
    */
   @Test(groups = { "enterpriseBeans", "stub" }, expectedExceptions = DeploymentException.class)
   @SpecAssertion(section = "3.3")
   public void testOnlyOneEnabledSpecializedEnterpriseBeanForImplmentation()
   {
      assert false;
   }

   /**
    * If the implementation class of an enterprise Web Bean is annotated @Interceptor
    * or @Decorator, a DefinitionException is thrown by the Web Bean manager at
    * initialization time.
    */
   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanInterceptorFails()
   {
      EnterpriseBean<Pug> pug = BeanFactory.createEnterpriseBean(Pug.class);
   }

   /**
    * If the implementation class of an enterprise Web Bean is annotated @Interceptor
    * or @Decorator, a DefinitionException is thrown by the Web Bean manager at
    * initialization time.
    */
   @Test(expectedExceptions = DefinitionException.class)
   @SpecAssertion(section = "3.3")
   public void testEnterpriseBeanDecoratorFails()
   {
      EnterpriseBean<Pekingese> pekingese = BeanFactory.createEnterpriseBean(Pekingese.class);
   }

   /**
    * Only one Web Bean per implementation class may be defined using
    * annotations.
    */
   @Test(expectedExceptions = DefinitionException.class, groups = { "enterpriseBeans", "stub" })
   @SpecAssertion(section = "3.3")
   public void testMultipleAnnotationDefinedEnterpriseBeansWithSameImplementationClassFails()
   {
      // TODO: testable?
      assert false;
   }

   /**
    * Note that multiple enterprise Web Beans may share the same implementation
    * class. This occurs when Web Beans are defined using XML
    */
   @Test(groups = { "webbeansxml", "enterpriseBeans", "stub" })
   @SpecAssertion(section = "3.3")
   public void testMultipleXMLDefinedEnterpriseBeansWithSameImplementationClassOK()
   {
      assert false;
   }

   /**
    * All session beans exposing an EJB 3.x client view and declared via an EJB
    * component defining annotation on the EJB bean class are Web Beans, and
    * thus no special declaration is required.
    */
   @Test(groups = { "enterpriseBeans", "stub" })
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEnterpriseBean()
   {
      // TODO: dupe?
      assert false;
   }

   /**
    * Additional enterprise Web Beans for these EJBs may be defined using XML,
    * by specifying the bean class in web-beans.xml.
    */
   @Test(groups = { "enterpriseBeans", "webbeansxml", "stub" })
   @SpecAssertion(section = "3.3.1")
   public void testAnnotatedEnterpriseBeanComplementedWithXML()
   {
      // TODO dupe?
      assert false;
   }

   /**
    * All session beans exposing an EJB 3.x client view and declared in
    * ejb-jar.xml are also Web Beans.
    */
   @Test(groups = { "enterpriseBeans", "ejbjarxml", "stub" })
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEnterpriseBean()
   {
      // TODO dupe?
      assert false;
   }

   /**
    * Additional enterprise Web Beans for these EJBs may be defined using XML,
    * by specifying the bean class and EJB name in web-beans.xml
    */
   @Test(groups = { "enterpriseBeans", "ejbjarxml", "webbeansxml", "stub" })
   @SpecAssertion(section = "3.3.1")
   public void testEJBJARDefinedEnterpriseBeanComplementedWithXML()
   {
      // TODO dupe?
      assert false;
   }

   /**
    * The set of API types for an enterprise Web Bean contains all local
    * interfaces of the bean that do not have wildcard type parameters or type
    * variables and their superinterfaces
    */
   @Test(groups = { "enterpriseBeans", "stub" })
   @SpecAssertion(section = "3.3.2")
   public void testAPITypesAreLocalInterfacesWithoutWildcardTypesOrTypeVariablesWithSuperInterfaces()
   {
      assert false;
   }

   /**
    * If the EJB bean has a bean class local view and the bean class is not a
    * parameterized type, the set of API types contains the bean class and all
    * superclasses
    */
   @Test(groups = { "enterpriseBeans", "stub" })
   @SpecAssertion(section = "3.3.2")
   public void testEnterpriseBeanWithLocalViewAndParameterizedTypeIncludesBeanClassAndSuperclassesInAPITypes()
   {
      assert false;
   }

   /**
    * In addition, java.lang.Object is an API type of every enterprise Web Bean.
    */
   @Test(groups = "enterpriseBeans")
   @SpecAssertion(section = "3.3.2")
   public void testObjectIsInAPITypes()
   {
      EnterpriseBean<Laika> laika = BeanFactory.createEnterpriseBean(Laika.class);
      assert laika.getTypes().contains(Object.class);
   }

   /**
    * Remote interfaces are not included in the set of API types.
    */
   @Test(groups = { "enterpriseBeans", "stub" })
   @SpecAssertion(section = "3.3.2")
   public void testRemoteInterfacesAreNotInAPITypes()
   {
      assert false;
   }

   /**
    * Enterprise Web Beans may be declared in web-beans.xml using the bean class
    * name (for EJBs defined using a component- defining annotation) or bean
    * class and EJB name (for EJBs defined in ejb-jar.xml). The ejbName
    * attribute declares the EJB name of an EJB defined in ejb-jar.xml
    */
   @Test(groups = { "enterpriseBeans", "webbeansxml", "ejbjarxml", "stub" })
   @SpecAssertion(section = "3.3")
   public void testXMLFilesEJBNameUsage()
   {
      assert false;
   }

   /**
    * Enterprise Web Beans may not be message-driven beans. If an enterprise Web
    * Bean declared in XML is a message-driven bean, a DefinitionException is
    * thrown by the Web Bean manager at initialization time.
    */
   @Test(expectedExceptions = DefinitionException.class, groups = "enterpriseBeans")
   @SpecAssertion(section = "3.3")
   public void testMessageDrivenBeansNotOK()
   {
      EnterpriseBean<Leopard> Leopard = BeanFactory.createEnterpriseBean(Leopard.class);
   }

   /**
    * The default name for an enterprise Web Bean is the unqualified class name
    * of the Web Bean implementation class, after converting the first character
    * to lower case.
    */
   @Test(groups = "enterpriseBeans")
   @SpecAssertion(section = "3.3.7")
   public void testDefaultName()
   {
      EnterpriseBean<Pitbull> pitbull = BeanFactory.createEnterpriseBean(Pitbull.class);
      assert pitbull.getName().equals("pitbull");
   }
   
   /**
    * An enterprise bean proxy implements all local interfaces of the EJB.
    */
   @Test(groups = { "specialization", "enterpriseBeans", "clientProxy", "stub" })
   @SpecAssertion(section = "3.3.8")
   public void testEnterpriseBeanProxyImplementsAllLocalInterfaces()
   {
      assert false;
   }   

}
