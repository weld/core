package org.jboss.weld.tests.producer.weld1368;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.Typed;

/**
 * See http://issues.jboss.org/browse/WELD-1368
 * 
 * @author tremes
 * 
 */
public class SpecializedProducer {

	@Typed
	public static class TestBean1 {

	}

	public static class TestProducer1 {

		@Produces
		public TestBean1 testBean() {
			return new TestBean1();
		}
	}

	@Typed
	public static class TestBean2 extends TestBean1 {
	
	}

	public static class TestProducer2 extends TestProducer1 {

		public void anyMethod() {

		}

		@Produces
		@Specializes
		@Override
		public TestBean2 testBean() {
			return new TestBean2();
		}

	}

	@Typed
	public static class TestBean3 extends TestBean2 {
		
	}

	public static class TestProducer3 extends TestProducer2 {

		@Produces
		@Specializes
		@Override
		public TestBean3 testBean() {
			return new TestBean3();
		}

	}
}
