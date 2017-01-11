package org.jboss.weld.contexts.beanstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.serialization.spi.BeanIdentifier;

public abstract class AbstractNamingScheme implements NamingScheme {

    private final String delimiter;

    /**
     *
     * @param delimiter The delimiter to use between the prefix and the identifier.
     */
    public AbstractNamingScheme(String delimiter) {
        this.delimiter = delimiter;
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
        return ids.stream().map(this::deprefix).collect(Collectors.toList());
    }

    public Collection<String> prefix(Collection<BeanIdentifier> ids) {
        return ids.stream().map(this::prefix).collect(Collectors.toList());
    }

    protected abstract String getPrefix();

    protected String getDelimiter() {
        return delimiter;
    }

}
