package org.jboss.weld.tests.ejb;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EJBCallTest {
	@Deployment
	public static JavaArchive createTestArchive() {
		return ShrinkWrap
				.create(JavaArchive.class, "test.jar")
				.addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
	}

	@Stateless
	public static class SomeService {
		public String someMethod() {
			return "test";
		}
	}

	@Inject
	SomeService someService;

	@Test
	public void testStatelessCall() {
		Assert.assertEquals("test", someService.someMethod());
	}
}
