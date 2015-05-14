package org.jboss.weld.context.beanstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.serialization.spi.BeanIdentifier;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public abstract class AbstractNamingScheme implements NamingScheme {

    class DeprefixerFunction implements Function<String, BeanIdentifier> {

        public BeanIdentifier apply(String from) {
            return AbstractNamingScheme.this.deprefix(from);
        }

    }

    class PrefixerFunction implements Function<BeanIdentifier, String> {

        public String apply(BeanIdentifier from) {
            return AbstractNamingScheme.this.prefix(from);
        }

    }

    private final String delimiter;
    private final DeprefixerFunction deprefixerFunction;
    private final PrefixerFunction prefixerFunction;

    /**
     * Create a new Prefixer.
     *
     * @param prefix    The prefix
     * @param delimiter The delimiter to use between the prefix and the
     *                  identifier.
     */
    public AbstractNamingScheme(String delimiter) {
        this.delimiter = delimiter;
        this.deprefixerFunction = new DeprefixerFunction();
        this.prefixerFunction = new PrefixerFunction();
    }

    public boolean accept(String id) {
        String prefix = getPrefix();
        return id.startsWith(prefix) && id.startsWith(delimiter, prefix.length());
    }

    public BeanIdentifier deprefix(String id) {
        return new StringBeanIdentifier(id.substring(getPrefix().length() + delimiter.length()));
    }

    public String prefix(BeanIdentifier id) {
        return getPrefix() + delimiter + id.asString();
    }

    public Collection<String> filterIds(Iterator<String> iterator) {
        if (!iterator.hasNext()) {
            return Collections.emptyList();
        }
        List<String> filtered = new ArrayList<String>();
        while (iterator.hasNext()) {
            String id = iterator.next();
            if (accept(id)) {
                filtered.add(id);
            }
        }
        return filtered;
    }

    public Collection<BeanIdentifier> deprefix(Collection<String> ids) {
        return new ArrayList<BeanIdentifier>(Collections2.transform(ids, deprefixerFunction));
    }

    public Collection<String> prefix(Collection<BeanIdentifier> ids) {
        return new ArrayList<String>(Collections2.transform(ids, prefixerFunction));
    }

    protected abstract String getPrefix();

    protected String getDelimiter() {
        return delimiter;
    }

}
