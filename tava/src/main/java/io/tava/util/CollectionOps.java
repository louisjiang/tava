package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Option;
import io.tava.lang.Tuple2;
import io.tava.util.builder.CollectionBuilder;

public final class CollectionOps {

    private CollectionOps() {
    }

    public static <E, C extends Collection<E>> C filter(C collection, Predicate1<E> action) {
        CollectionBuilder<E, C> builder = collection.builder();
        if (collection.isEmpty()) {
            return builder.build();
        }
        for (E e : collection) {
            if (action.test(e)) {
                builder.add(e);
            }
        }

        return builder.build();
    }

    public static <E, C extends Collection<E>> C filterNot(C collection, Predicate1<E> action) {
        return filter(collection, e -> !action.test(e));
    }

    public static <E, C extends Collection<E>> C take(C collection, int n) {
        CollectionBuilder<E, C> builder = collection.builder();
        if (n <= 0 || collection.isEmpty()) {
            return builder.build();
        }

        int index = 0;
        for (E e : collection) {
            if (index++ == n) {
                break;
            }
            builder.add(e);
        }
        return builder.build();
    }

    public static <E, C extends Collection<E>> C takeRight(C collection, int n) {
        CollectionBuilder<E, C> builder = collection.builder();
        if (n <= 0 || collection.isEmpty()) {
            return builder.build();
        }

        int diff = collection.size() - n;
        int index = 0;
        for (E e : collection) {
            if (index++ >= diff) {
                builder.add(e);
            }
        }
        return builder.build();
    }

    public static <E, C extends Collection<E>> C takeWhile(C collection, Predicate1<E> action) {
        CollectionBuilder<E, C> builder = collection.builder();
        for (E e : collection) {
            if (!action.test(e)) {
                break;
            }
            builder.add(e);
        }
        return builder.build();
    }

    public static <E, C extends Collection<E>> C drop(C collection, int n) {
        CollectionBuilder<E, C> builder = collection.builder();
        if (n >= collection.size() || collection.isEmpty()) {
            return builder.build();
        }

        int index = 0;
        for (E e : collection) {
            if (index++ > n - 1) {
                builder.add(e);
            }
        }
        return builder.build();
    }


    public static <E, C extends Collection<E>> C dropRight(C collection, int n) {
        CollectionBuilder<E, C> builder = collection.builder();
        int size = collection.size();
        if (n >= size || collection.isEmpty()) {
            return builder.build();
        }

        for (E e : collection) {
            if (n++ >= size) {
                break;
            }
            builder.add(e);
        }
        return builder.build();
    }

    public static <E, C extends Collection<E>> C dropWhile(C collection, Predicate1<E> action) {
        CollectionBuilder<E, C> builder = collection.builder();
        if (collection.isEmpty()) {
            return builder.build();
        }

        boolean test = true;
        for (E e : collection) {
            if (test) {
                test = action.test(e);
            }
            if (!test) {
                builder.add(e);
            }
        }

        return builder.build();
    }

    public static <E, C extends Collection<E>> C slice(C collection, int from, int until) {
        int lo = Math.max(from, 0);
        CollectionBuilder<E, C> builder = collection.builder();
        if (until <= lo || collection.isEmpty()) {
            return builder.build();
        }
        int index = 0;
        for (E e : collection) {
            if (index >= until) {
                break;
            }
            if (index >= lo) {
                builder.add(e);
            }
            index++;
        }

        return builder.build();
    }

    public static <K, V, E, C extends Collection<E>> Map<K, V> toMap(C collection, Function1<E, Tuple2<K, V>> action) {
        Map<K, V> map = new HashMap<>();
        for (E e : collection) {
            Tuple2<K, V> tuple2 = action.apply(e);
            map.put(tuple2.getValue1(), tuple2.getValue2());
        }
        return map;
    }

    public static <E, C extends Collection<E>, R, RC extends Collection<R>> RC map(C collection, Function1<E, R> action) {
        CollectionBuilder<R, RC> builder = collection.builder();
        for (E e : collection) {
            R r = action.apply(e);
            builder.add(r);
        }
        return builder.build();
    }

    public static <E, C extends Collection<E>, R, RC extends Collection<R>> RC mapWithIndex(C collection, IndexedFunction1<E, R> action) {
        CollectionBuilder<R, RC> builder = collection.builder();
        int index = 0;
        for (E e : collection) {
            R r = action.apply(index++, e);
            builder.add(r);
        }
        return builder.build();
    }


    public static <E, C extends Collection<E>, R, RC extends Collection<R>> RC flatMap(C collection, Function1<E, Collection<R>> action) {
        CollectionBuilder<R, RC> builder = collection.builder();
        for (E e : collection) {
            Collection<R> c = action.apply(e);
            builder.add(c);
        }
        return builder.build();
    }

    public static <E, C extends Collection<E>, R extends Collection<Tuple2<E, Integer>>> R zipWithIndex(C collection) {
        return mapWithIndex(collection, (index, e) -> new Tuple2<>(e, index));
    }

    public static <E, C extends Collection<E>> boolean forall(C collection, Predicate1<E> action) {
        boolean result = false;
        for (E e : collection) {
            result = action.test(e);
            if (!result) {
                break;
            }
        }
        return result;
    }

    public static <E, C extends Collection<E>> boolean exists(C collection, Predicate1<E> action) {
        for (E e : collection) {
            if (action.test(e)) {
                return true;
            }
        }
        return false;
    }

    public static <E, C extends Collection<E>> int count(C collection, Predicate1<E> action) {
        int result = 0;
        for (E e : collection) {
            if (action.test(e)) {
                result += 1;
            }
        }
        return result;
    }

    public static <E, C extends Collection<E>> Option<E> find(C collection, Predicate1<E> action) {
        for (E e : collection) {
            if (action.test(e)) {
                return Option.some(e);
            }
        }
        return Option.none();
    }


    public static <E, C extends Collection<E>> Tuple2<C, C> span(C collection, Predicate1<E> action) {
        CollectionBuilder<E, C> leftBuilder = collection.builder();
        CollectionBuilder<E, C> rightBuilder = collection.builder();
        if (collection.size() > 0) {
            boolean test = true;
            for (E e : collection) {
                if (test) {
                    test = action.test(e);
                }
                if (test) {
                    leftBuilder.add(e);
                } else {
                    rightBuilder.add(e);
                }
            }
        }

        return new Tuple2<>(leftBuilder.build(), rightBuilder.build());
    }

    public static <E, C extends Collection<E>> Tuple2<C, C> splitAt(C collection, int n) {

        CollectionBuilder<E, C> leftBuilder = collection.builder();
        CollectionBuilder<E, C> rightBuilder = collection.builder();

        if (collection.isEmpty()) {
            return new Tuple2<>(leftBuilder.build(), rightBuilder.build());
        }

        int index = 0;
        for (E e : collection) {
            if (index++ < n) {
                leftBuilder.add(e);
            } else {
                rightBuilder.add(e);
            }
        }

        return new Tuple2<>(leftBuilder.build(), rightBuilder.build());
    }

    public static <E, C extends Collection<E>, R> R foldLeft(C collection, R zero, Function2<R, E, R> action) {
        R result = zero;
        for (E e : collection) {
            result = action.apply(result, e);
        }
        return result;
    }

    public static <E, C extends Collection<E>, R> R foldLeftWithIndex(C collection, R zero, IndexedFunction2<R, E, R> action) {
        R result = zero;
        int index = 0;
        for (E e : collection) {
            result = action.apply(index++, result, e);
        }
        return result;
    }

    public static <E, C extends Collection<E>> E reduceLeft(C collection, Function2<? super E, ? super E, ? extends E> action) {
        boolean first = true;
        E item = null;
        for (E e : collection) {
            if (!first) {
                item = action.apply(item, e);
                continue;
            }
            item = e;
            first = false;
        }
        return item;
    }

    public static <E, C extends Collection<E>, K0> Map<K0, C> groupBy(C collection, Function1<E, K0> action) {
        Map<K0, C> map = new HashMap<>();
        for (E e : collection) {
            K0 key = action.apply(e);
            C innerCollection = map.get(key);
            if (innerCollection == null) {
                innerCollection = collection.<E, C>builder().build();
                map.put(key, innerCollection);
            }
            innerCollection.add(e);
        }
        return map;
    }

    public static <E, C extends Collection<E>, K0, R, RC extends Collection<R>> Map<K0, RC> groupMap(C collection, Function1<E, K0> action, Function1<E, R> mapAction) {
        Map<K0, RC> map = new HashMap<>();
        for (E e : collection) {
            K0 key = action.apply(e);
            RC innerCollection = map.get(key);
            if (innerCollection == null) {
                innerCollection = collection.<R, RC>builder().build();
                map.put(key, innerCollection);
            }
            R r = mapAction.apply(e);
            innerCollection.add(r);
        }
        return map;
    }

    public static <E, L extends List<E>> L reverse(L list) {
        CollectionBuilder<E, L> builder = list.builder();
        for (int i = list.size() - 1; i >= 0; i--) {
            E e = list.get(i);
            builder.add(e);
        }
        return builder.build();
    }


}
