package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Option;
import io.tava.lang.Tuple;
import io.tava.lang.Tuple2;
import io.tava.util.builder.MapBuilder;


public final class MapOps {

    private MapOps() {
    }

    public static <K, V, M extends Map<K, V>> M filter(M map, Predicate2<K, V> action) {
        return filter(map, entry -> action.test(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>> M filter(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        MapBuilder<K, V, M> builder = map.builder();
        if (map.isEmpty()) {
            return builder.build();
        }
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            boolean test = action.test(entry);
            if (test) {
                builder.put(entry);
            }
        }
        return builder.build();
    }

    public static <K, V, M extends Map<K, V>> M filterNot(M map, Predicate2<K, V> action) {
        return filterNot(map, entry -> action.test(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>> M filterNot(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        if (map.isEmpty()) {
            return map.<K, V, M>builder().build();
        }
        return filter(map, entry -> !action.test(entry));
    }


    public static <K, V, M extends Map<K, V>> M take(M map, int n) {
        MapBuilder<K, V, M> builder = map.builder();
        if (n <= 0 || map.isEmpty()) {
            return builder.build();
        }

        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        int index = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (index++ == n) {
                break;
            }
            builder.put(entry);
        }
        return builder.build();
    }

    public static <K, V, M extends Map<K, V>> M takeRight(M map, int n) {
        MapBuilder<K, V, M> builder = map.builder();
        if (n <= 0 || map.isEmpty()) {
            return builder.build();
        }
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        int diff = map.size() - n;
        int index = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (index++ >= diff) {
                builder.put(entry);
            }
        }

        return builder.build();
    }

    public static <K, V, M extends Map<K, V>> M takeWhile(M map, Predicate2<K, V> action) {
        return takeWhile(map, entry -> action.test(entry.getKey(), entry.getValue()));

    }

    public static <K, V, M extends Map<K, V>> M takeWhile(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        MapBuilder<K, V, M> builder = map.builder();
        if (map.isEmpty()) {
            return builder.build();
        }
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (!action.test(entry)) {
                break;
            }
            builder.put(entry);
        }
        return builder.build();
    }

    public static <K, V, M extends Map<K, V>> M toMap(Set<java.util.Map.Entry<K, V>> entries, MapBuilder<K, V, M> builder) {
        return entries.foldLeft(builder, MapBuilder::put).build();
    }

    public static <K, V, M extends Map<K, V>> M drop(M map, int n) {
        MapBuilder<K, V, M> builder = map.builder();
        if (n >= map.size() || map.isEmpty()) {
            return builder.build();
        }
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        int index = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (index++ > n - 1) {
                builder.put(entry);
            }
        }

        return builder.build();
    }

    public static <K, V, M extends Map<K, V>> M dropRight(M map, int n) {
        MapBuilder<K, V, M> builder = map.builder();

        int size = map.size();
        if (n >= size || map.isEmpty()) {
            return builder.build();
        }

        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (n++ >= size) {
                break;
            }
            builder.put(entry);
        }

        return builder.build();
    }

    public static <K, V, M extends Map<K, V>> M dropWhile(M map, Predicate2<K, V> action) {
        return dropWhile(map, entry -> action.test(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>> M dropWhile(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        MapBuilder<K, V, M> builder = map.builder();
        if (map.isEmpty()) {
            return builder.build();
        }
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        boolean test = true;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (test) {
                test = action.test(entry);
            }
            if (!test) {
                builder.put(entry);
            }
        }

        return builder.build();
    }

    public static <K, V, M extends Map<K, V>> M slice(M map, int from, int until) {
        MapBuilder<K, V, M> builder = map.builder();
        int lo = Math.max(from, 0);
        if (until <= lo || map.isEmpty()) {
            return builder.build();
        }

        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        int index = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (index >= until) {
                break;
            }
            if (index >= lo) {
                builder.put(entry);
            }
            index++;
        }

        return take(drop(map, lo), until - lo);
    }

    public static <K, V, M extends Map<K, V>> Tuple2<? extends M, ? extends M> span(M map, Predicate2<K, V> action) {
        return span(map, entry -> action.test(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>> Tuple2<? extends M, ? extends M> span(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        MapBuilder<K, V, M> leftBuilder = map.builder();
        MapBuilder<K, V, M> rightBuilder = map.builder();
        if (map.isEmpty()) {
            return Tuple.of(leftBuilder.build(), rightBuilder.build());
        }

        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        boolean test = true;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (test) {
                test = action.test(entry);
            }
            if (test) {
                leftBuilder.put(entry);
            } else {
                rightBuilder.put(entry);
            }
        }

        return Tuple.of(leftBuilder.build(), rightBuilder.build());
    }

    public static <K, V, M extends Map<K, V>> Tuple2<? extends M, ? extends M> splitAt(M map, int n) {
        MapBuilder<K, V, M> leftBuilder = map.builder();
        MapBuilder<K, V, M> rightBuilder = map.builder();
        if (map.isEmpty()) {
            return Tuple.of(leftBuilder.build(), rightBuilder.build());
        }

        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        int index = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (index++ < n) {
                leftBuilder.put(entry);
            } else {
                rightBuilder.put(entry);
            }
        }
        return Tuple.of(leftBuilder.build(), rightBuilder.build());
    }

    public static <K, V, M extends Map<K, V>, K0, V0, M0 extends Map<K0, V0>> M0 mapWithIndex(M map, IndexedFunction2<K, V, java.util.Map.Entry<K0, V0>> action) {
        return mapWithIndex(map, (index, entry) -> action.apply(index, entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>, K0, V0, M0 extends Map<K0, V0>> M0 mapWithIndex(M map, IndexedFunction1<java.util.Map.Entry<K, V>, java.util.Map.Entry<K0, V0>> action) {
        MapBuilder<K0, V0, M0> builder = map.builder();
        if (map.isEmpty()) {
            return builder.build();
        }
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        int index = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            builder.put(action.apply(index++, entry));
        }
        return builder.build();
    }

    public static <K, V, M extends Map<K, V>, K0, V0, M0 extends Map<K0, V0>> M0 flatMap(M map, Function2<K, V, Map<K0, V0>> action) {
        return flatMap(map, entry -> action.apply(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>, K0, V0, M0 extends Map<K0, V0>> M0 flatMap(M map, Function1<java.util.Map.Entry<K, V>, Map<K0, V0>> action) {
        MapBuilder<K0, V0, M0> builder = map.builder();
        if (map.isEmpty()) {
            return builder.build();
        }
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            builder.putAll(action.apply(entry));
        }
        return builder.build();
    }

    public static <K, V, M extends Map<K, V>, K0, V0, M0 extends Map<K0, V0>> M0 map(M map, Function2<K, V, Tuple2<K0, V0>> action) {
        return map(map, entry -> action.apply(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>, K0, V0, M0 extends Map<K0, V0>> M0 map(M map, Function1<java.util.Map.Entry<K, V>, Tuple2<K0, V0>> action) {
        MapBuilder<K0, V0, M0> builder = map.builder();
        if (map.isEmpty()) {
            return builder.build();
        }
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            builder.put(action.apply(entry));
        }
        return builder.build();
    }

    public static <K, V, M extends Map<K, V>> Collection<Tuple2<java.util.Map.Entry<K, V>, Integer>> zipWithIndex(M map) {
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        List<Tuple2<java.util.Map.Entry<K, V>, Integer>> list = new ArrayList<>();
        int index = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            list.add(Tuple.of(entry, index++));
        }
        return list;
    }

    public static <K, V, M extends Map<K, V>> boolean forall(M map, Predicate2<K, V> action) {
        return forall(map, entry -> action.test(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>> boolean forall(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        boolean result = false;
        for (java.util.Map.Entry<K, V> entry : entries) {
            result = action.test(entry);
            if (!result) {
                break;
            }
        }
        return result;
    }

    public static <K, V, M extends Map<K, V>> boolean exists(M map, Predicate2<K, V> action) {
        return exists(map, entry -> action.test(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>> boolean exists(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (action.test(entry)) {
                return true;
            }
        }
        return false;
    }

    public static <K, V, M extends Map<K, V>> int count(M map, Predicate2<K, V> action) {
        return count(map, entry -> action.test(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>> int count(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        int result = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (action.test(entry)) {
                result += 1;
            }
        }
        return result;
    }

    public static <K, V, M extends Map<K, V>> Option<java.util.Map.Entry<K, V>> find(M map, Predicate2<K, V> action) {
        return find(map, entry -> action.test(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>> Option<java.util.Map.Entry<K, V>> find(M map, Predicate1<java.util.Map.Entry<K, V>> action) {
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (action.test(entry)) {
                return Option.some(entry);
            }
        }
        return Option.none();
    }

    public static <K, V, M extends Map<K, V>, R> R foldLeft(M map, R zero, Function2<R, java.util.Map.Entry<K, V>, R> action) {
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        R result = zero;
        for (java.util.Map.Entry<K, V> entry : entries) {
            result = action.apply(result, entry);
        }
        return result;
    }

    public static <K, V, M extends Map<K, V>, R> R foldLeftWithIndex(M map, R zero, IndexedFunction2<R, java.util.Map.Entry<K, V>, R> action) {
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        R result = zero;
        int index = 0;
        for (java.util.Map.Entry<K, V> entry : entries) {
            result = action.apply(index++, result, entry);
        }
        return result;
    }

    public static <K, V, M extends Map<K, V>, R> R foldRight(M map, R zero, Function2<R, java.util.Map.Entry<K, V>, R> action) {
        return map.reverse().foldRight(zero, action);
    }

    public static <K, V, M extends Map<K, V>, R> R foldRightWithIndex(M map, R zero, IndexedFunction2<R, java.util.Map.Entry<K, V>, R> action) {
        return map.reverse().foldRightWithIndex(zero, action);
    }

    public static <K, V, M extends Map<K, V>> java.util.Map.Entry<K, V> reduceLeft(M map, Function2<? super java.util.Map.Entry<K, V>, ? super java.util.Map.Entry<K, V>, ? extends java.util.Map.Entry<K, V>> action) {
        boolean first = true;
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        java.util.Map.Entry<K, V> entry0 = null;
        for (java.util.Map.Entry<K, V> entry : entries) {
            if (!first) {
                entry0 = action.apply(entry0, entry);
                continue;
            }
            entry0 = entry;
            first = false;
        }
        return entry0;
    }

    public static <K, V, M extends Map<K, V>, K0> Map<K0, M> groupBy(M map, Function2<K, V, K0> action) {
        return groupBy(map, entry -> action.apply(entry.getKey(), entry.getValue()));
    }

    public static <K, V, M extends Map<K, V>, K0> Map<K0, M> groupBy(M map, Function1<java.util.Map.Entry<K, V>, K0> action) {
        Map<K0, M> map0 = map.<K0, M, Map<K0, M>>builder().build();
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            K0 key = action.apply(entry);
            M innerMap = map0.get(key);
            if (innerMap == null) {
                innerMap = map.<K, V, M>builder().build();
                map0.put(key, innerMap);
            }
            innerMap.put(entry.getKey(), entry.getValue());
        }
        return map0;
    }

    public static <K, V, M extends Map<K, V>, K0, R> Map<K0, List<R>> groupMap(M map, Function1<java.util.Map.Entry<K, V>, K0> action, Function1<java.util.Map.Entry<K, V>, R> mapAction) {
        Map<K0, List<R>> map0 = map.<K0, List<R>, Map<K0, List<R>>>builder().build();
        java.util.Set<java.util.Map.Entry<K, V>> entries = map.entrySet();
        for (java.util.Map.Entry<K, V> entry : entries) {
            K0 k0 = action.apply(entry);
            List<R> innerList = map0.get(k0);
            if (innerList == null) {
                innerList = new ArrayList<>();
                map0.put(k0, innerList);
            }
            R r = mapAction.apply(entry);
            innerList.add(r);
        }
        return map0;
    }

}