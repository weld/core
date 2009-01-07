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
import org.jboss.webbeans.test.ejb.invalid.Greyhound;
import org.jboss.webbeans.test.ejb.invalid.Husky;
import org.jboss.webbeans.test.ejb.invalid.IrishTerrier;
import org.jboss.webbeans.test.ejb.invalid.Pekingese;
import org.jboss.webbeans.test.ejb.invalid.Pug;
import org.jboss.webbeans.test.ejb.invalid.Saluki;
import org.jboss.webbeans.test.ejb.valid.Labrador;
import org.jboss.webbeans.test.ejb.valid.Laika;
import org.jboss.webbeans.test.ejb.valid.Pitbull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Sections
 * 
 * 3.3. Enterprise Web Beans
 * 3.3.1. Which EJBs are enterprise Web Beans?
 * 3.3.2. API types of an enterprise Web Bean
 * 3.3.3. Declaring an enterprise Web Bean using annotations
 * 3.3.4. Declaring an enterprise Web Bean using XML
 * 
 * @author Nicklas Karlsson
 */
@SpecVersion("20081206")
@SuppressWarnings("unused")
public class EnterpriseBeanDeclarationTest extends AbstractTest
{
   
   @BeforeMethod
   public void setupEjbDescriptors()
   {
      addToEjbCache(Pug.class);
      addToEjbCache(Giraffe.class);
      addToEjbCache(Husky.class);
      addToEjbCache(Pitbull.class);
      addToEjbCache(IrishTerrier.class);
      addToEjbCache(Laika.class);
      addToEjbCache(Leopard.class);
      addToEjbCache(Labrador.class);
      addToEjbCache(Greyhound.class);
      addToEjbCache(Dachshund.class);
      addToEjbCache(Bullmastiff.class);
      addToEjbCache(Pekingese.class);
      addToEjbCache(Boxer.class);
      addToEjbCache(Beagle.class);
      addToEjbCache(Saluki.class);
   }

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
      EnterpriseBean<Giraffe> giraffe = EnterpriseBean.of(Giraffe.class, manager);
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
      EnterpriseBean<Beagle> beagle = EnterpriseBean.of(Beagle.class, manager);
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
      EnterpriseBean<Boxer> boxer = EnterpriseBean.of(Boxer.class, manager);
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
      EnterpriseBean<Bullmastiff> boxer = EnterpriseBean.of(Bullmastiff.class, manager);
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
      EnterpriseBean<Dachshund> dachshund = EnterpriseBean.of(Dachshund.class, manager);
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
      EnterpriseBean.of(Labrador.class, manager);
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
      EnterpriseBean<Greyhound> greyhound = EnterpriseBean.of(Greyhound.class, manager);
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
      EnterpriseBean<Husky> husky = EnterpriseBean.of(Husky.class, manager);
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
      EnterpriseBean<IrishTerrier> irishTerrier = EnterpriseBean.of(IrishTerrier.class, manager);
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
      EnterpriseBean<Laika> laika = EnterpriseBean.of(Laika.class, manager);
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
      EnterpriseBean<Pug> pug = EnterpriseBean.of(Pug.class, manager);
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
      EnterpriseBean<Pekingese> pekingese = EnterpriseBean.of(Pekingese.class, manager);
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
      EnterpriseBean<Laika> laika = EnterpriseBean.of(Laika.class, manager);
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
      EnterpriseBean<Leopard> Leopard = EnterpriseBean.of(Leopard.class, manager);
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
      EnterpriseBean<Pitbull> pitbull = EnterpriseBean.of(Pitbull.class, manager);
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
