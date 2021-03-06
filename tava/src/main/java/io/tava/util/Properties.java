package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.Function2;
import io.tava.function.Predicate1;
import io.tava.function.Predicate2;
import io.tava.lang.Tuple2;
import io.tava.util.builder.MapBuilder;
import io.tava.util.builder.PropertiesBuilder;

public class Properties extends java.util.Properties implements Map<Object, Object> {

    @Override
    public <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder() {
        return (MapBuilder<K0, V0, M0>) new PropertiesBuilder();
    }

    @Override
    public Properties filter(Predicate2<Object, Object> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public Properties filter(Predicate1<java.util.Map.Entry<Object, Object>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public Properties filterNot(Predicate2<Object, Object> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public Properties filterNot(Predicate1<java.util.Map.Entry<Object, Object>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public Properties take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    public Properties takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    @Override
    public Properties takeWhile(Predicate2<Object, Object> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public Properties takeWhile(Predicate1<java.util.Map.Entry<Object, Object>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public Properties drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    public Properties dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    public Properties dropWhile(Predicate2<Object, Object> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public Properties dropWhile(Predicate1<java.util.Map.Entry<Object, Object>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public Properties slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    public Tuple2<Properties, Properties> span(Predicate2<Object, Object> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<Properties, Properties> span(Predicate1<java.util.Map.Entry<Object, Object>> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<Properties, Properties> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    public <K0> Map<K0, Properties> groupBy(Function2<Object, Object, K0> action) {
        return MapOps.groupBy(this, action);
    }

    @Override
    public <K0> Map<K0, Properties> groupBy(Function1<java.util.Map.Entry<Object, Object>, K0> action) {
        return MapOps.groupBy(this, action);
    }

    @Override
    public <K0, R> Map<K0, List<R>> groupMap(Function1<java.util.Map.Entry<Object, Object>, K0> action, Function1<java.util.Map.Entry<Object, Object>, R> mapAction) {
        return MapOps.groupMap(this, action, mapAction);
    }

}
