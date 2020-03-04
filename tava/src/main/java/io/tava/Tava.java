package io.tava;

import io.tava.util.Adapter;
import io.tava.util.List;
import io.tava.util.Map;
import io.tava.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-04 16:58:31
 */
public class Tava {

    public static <E> List<E> list(java.util.List<E> list) {
        return Adapter.list(list);
    }

    public static <E> Set<E> set(java.util.Set<E> set) {
        return Adapter.set(set);
    }

    public static <K, V> Map<K, V> map(java.util.Map<K, V> map) {
        return Adapter.map(map);
    }

}
