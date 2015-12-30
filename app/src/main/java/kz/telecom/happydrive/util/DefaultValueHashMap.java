package kz.telecom.happydrive.util;

import java.util.HashMap;

/**
 * Created by shgalym on 11/29/15.
 */
public class DefaultValueHashMap<K, V> extends HashMap<K, V> {
    private final V defaultValue;

    public DefaultValueHashMap(V defaultValue) {
        super();
        this.defaultValue = defaultValue;
    }

    @Override
    public V get(Object key) {
        return containsKey(key) ? super.get(key) : defaultValue;
    }
}
