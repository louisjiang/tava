package io.tava.fastjson;


import io.tava.fastjson.builder.JSONArrayBuilder;
import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;
import io.tava.util.Collection;
import io.tava.util.CollectionOps;
import io.tava.util.List;
import io.tava.util.Map;
import io.tava.util.builder.CollectionBuilder;

public class JSONArray extends com.alibaba.fastjson.JSONArray implements List<Object> {

    public JSONArray() {
        super();
    }

    public JSONArray(java.util.List<Object> list) {
        super(list);
    }

    public JSONArray(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new JSONArrayBuilder();
    }

    @Override
    public JSONArray filter(Predicate1<Object> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public JSONArray filterNot(Predicate1<Object> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public JSONArray take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public JSONArray takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public JSONArray takeWhile(Predicate1<Object> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public JSONArray drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public JSONArray dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public JSONArray dropWhile(Predicate1<Object> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public JSONArray slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> List<R> map(Function1<Object, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> List<R> mapWithIndex(IndexedFunction1<Object, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> List<R> flatMap(Function1<Object, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public List<Tuple2<Object, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    public Tuple2<JSONArray, JSONArray> span(Predicate1<Object> action) {
        return CollectionOps.span(this, action);
    }

    public Tuple2<JSONArray, JSONArray> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    public JSONArray reverse() {
        return CollectionOps.reverse(this);
    }

    @Override
    public <K> Map<K, JSONArray> groupBy(Function1<Object, K> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    public <K0, R> Map<K0, ? extends List<R>> groupMap(Function1<Object, K0> action, Function1<Object, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    public JSONArray diff(Collection<Object> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    public JSONArray intersect(Collection<Object> that) {
        return CollectionOps.intersect(this, that);
    }
}
