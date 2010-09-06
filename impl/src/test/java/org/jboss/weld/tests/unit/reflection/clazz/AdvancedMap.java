package org.jboss.weld.tests.unit.reflection.clazz;

import java.util.Map;

public interface AdvancedMap<K, V> extends Map<K, V>
{
   
   public ReallyAdvancedMap<K, V> getReallyAdvancedMap();

}
