package org.jboss.weld.tests.unit.metadata;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.ClassAvailableActivation;
import org.jboss.weld.bootstrap.spi.Filter;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.SystemPropertyActivation;
import org.jboss.weld.metadata.BeansXmlImpl;
import org.jboss.weld.metadata.BeansXmlMergeUtil;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.metadata.ScanningImpl;
import org.junit.Test;

public class BeansXmlMergeUtilTest {
    private class FilterStub implements Filter {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public Collection<Metadata<SystemPropertyActivation>> getSystemPropertyActivations() {
            return null;
        }

        @Override
        public Collection<Metadata<ClassAvailableActivation>> getClassAvailableActivations() {
            return null;
        }
    }

    @Test
    public void testMergeEmptyBeansXML() {
        final List<BeansXml> beansXMLFiles = Arrays.asList(BeansXml.EMPTY_BEANS_XML, BeansXml.EMPTY_BEANS_XML);
        final BeansXml mergedBeansXml = new BeansXmlMergeUtil().merge(beansXMLFiles);
        assertNotNull(mergedBeansXml);
        assertNotNull(mergedBeansXml.getEnabledAlternativeClasses());
        assertNotNull(mergedBeansXml.getEnabledAlternativeStereotypes());
        assertNotNull(mergedBeansXml.getEnabledDecorators());
        assertNotNull(mergedBeansXml.getEnabledInterceptors());
        assertNotNull(mergedBeansXml.getScanning());
        assertNotNull(mergedBeansXml.getScanning().getExcludes());
        assertNotNull(mergedBeansXml.getScanning().getIncludes());
    }

    @Test
    public void testMergeAll() {
        final BeansXml beansXml1 = createBeansXmlTestInstance();
        final BeansXml beansXml2 = createBeansXmlTestInstance();

        final List<BeansXml> beansXMLFiles = Arrays.asList(beansXml1, beansXml2, BeansXml.EMPTY_BEANS_XML);
        final BeansXml mergedBeansXml = new BeansXmlMergeUtil().merge(beansXMLFiles);

        assertNotNull(mergedBeansXml);
        assertNotNull(mergedBeansXml.getEnabledAlternativeClasses());
        assertNotNull(mergedBeansXml.getEnabledAlternativeStereotypes());
        assertNotNull(mergedBeansXml.getEnabledDecorators());
        assertNotNull(mergedBeansXml.getEnabledInterceptors());
        assertNotNull(mergedBeansXml.getScanning());
        assertNotNull(mergedBeansXml.getScanning().getExcludes());
        assertNotNull(mergedBeansXml.getScanning().getIncludes());

        assertEquals(2, mergedBeansXml.getEnabledAlternativeClasses().size());
        assertEquals(2, mergedBeansXml.getEnabledAlternativeStereotypes().size());
        assertEquals(2, mergedBeansXml.getEnabledDecorators().size());
        assertEquals(2, mergedBeansXml.getEnabledInterceptors().size());
        assertEquals(2, mergedBeansXml.getScanning().getExcludes().size());
        assertEquals(2, mergedBeansXml.getScanning().getIncludes().size());
    }

    @Test
    public void testMergeWithoutDuplicates() {
        final BeansXml beansXml1 = createBeansXmlTestInstance();
        final BeansXml beansXml2 = createBeansXmlTestInstance();

        final List<BeansXml> beansXMLFiles = Arrays.asList(beansXml1, beansXml2, BeansXml.EMPTY_BEANS_XML);
        final BeansXml mergedBeansXml = new BeansXmlMergeUtil().merge(beansXMLFiles, true);

        assertNotNull(mergedBeansXml);
        assertNotNull(mergedBeansXml.getEnabledAlternativeClasses());
        assertNotNull(mergedBeansXml.getEnabledAlternativeStereotypes());
        assertNotNull(mergedBeansXml.getEnabledDecorators());
        assertNotNull(mergedBeansXml.getEnabledInterceptors());
        assertNotNull(mergedBeansXml.getScanning());
        assertNotNull(mergedBeansXml.getScanning().getExcludes());
        assertNotNull(mergedBeansXml.getScanning().getIncludes());

        assertEquals(1, mergedBeansXml.getEnabledAlternativeClasses().size());
        assertEquals(1, mergedBeansXml.getEnabledAlternativeStereotypes().size());
        assertEquals(1, mergedBeansXml.getEnabledDecorators().size());
        assertEquals(1, mergedBeansXml.getEnabledInterceptors().size());
    }

    public BeansXml createBeansXmlTestInstance() {
        final MetadataImpl<String> alternative = new MetadataImpl<String>("some.package.SomeAlternative", "C:\temp");
        final MetadataImpl<String> stereoType = new MetadataImpl<String>("some.package.SomeStereotype", "C:\temp");
        final MetadataImpl<String> decorator = new MetadataImpl<String>("some.package.SomeDecorator", "C:\temp");
        final MetadataImpl<String> interceptor = new MetadataImpl<String>("some.package.SomeInterceptor", "C:\temp");
        final Metadata<Filter> include = new MetadataImpl<Filter>(new FilterStub(), "C:\temp");
        final Metadata<Filter> exclude = new MetadataImpl<Filter>(new FilterStub(), "C:\temp");

        final List<Metadata<String>> alternativesClasses = new ArrayList<Metadata<String>>();
        final List<Metadata<String>> alternativeStereotypes = new ArrayList<Metadata<String>>();
        final List<Metadata<String>> decorators = new ArrayList<Metadata<String>>();
        final List<Metadata<String>> interceptors = new ArrayList<Metadata<String>>();
        final List<Metadata<Filter>> includes = new ArrayList<Metadata<Filter>>();
        final List<Metadata<Filter>> excludes = new ArrayList<Metadata<Filter>>();

        alternativesClasses.add(alternative);
        alternativeStereotypes.add(stereoType);
        decorators.add(decorator);
        interceptors.add(interceptor);
        includes.add(include);
        excludes.add(exclude);

        return new BeansXmlImpl(alternativesClasses, alternativeStereotypes, decorators, interceptors, new ScanningImpl(includes, excludes), null,
                BeanDiscoveryMode.ALL, "1.1");
    }
}
