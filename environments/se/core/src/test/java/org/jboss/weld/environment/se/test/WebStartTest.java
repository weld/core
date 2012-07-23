package org.jboss.weld.environment.se.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.se.discovery.url.WeldSEResourceLoader;
import org.jboss.weld.environment.se.discovery.url.WeldSEUrlDeployment;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.junit.Test;

/**
 * Tests that Java Web Start to remote JARs are resolved to their local cached
 * copy (WELD-1040).
 * 
 * @author Alexandre Gattiker
 */
public class WebStartTest {

	/**
	 * Mock implementation of com.sun.jnlp.JNLPClassLoader#getJarFile(URL)
	 */
	public static final class MockJNLPClassLoader extends ClassLoader {
		public JarFile getJarFile(URL url) {
			try {
				File jarFile = File.createTempFile("jarfile", ".jar");
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
						jarFile));
				out.putNextEntry(new ZipEntry("MockClass.class"));
				out.close();
				return new JarFile(jarFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}
	}

	@Test
	public void testJavaWebStartClassLoaderURL() {
		ResourceLoader resourceLoader = new WeldSEResourceLoader() {
			@Override
			public Collection<URL> getResources(String name) {
				try {
					return Collections.singleton(new URL(
							"http://example.com/dummy"));
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}

		};

		Thread.currentThread().setContextClassLoader(new MockJNLPClassLoader());
		Bootstrap bootstrap = new WeldBootstrap();
		WeldSEUrlDeployment depl = new WeldSEUrlDeployment(resourceLoader,
				bootstrap);

		assertEquals(Collections.singleton("MockClass"), new HashSet<String>(
				depl.getBeanDeploymentArchives().get(0).getBeanClasses()));
	}
}
