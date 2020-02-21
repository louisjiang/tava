package io.tava.fastjson;

import io.tava.fastjson.builder.JSONObjectBuilder;
import io.tava.function.*;
import io.tava.lang.Tuple2;
import io.tava.util.Map;
import io.tava.util.MapOps;
import io.tava.util.builder.MapBuilder;

public class JSONObject extends com.alibaba.fastjson.JSONObject implements Map<String, Object> {

    public JSONObject() {
        super();
    }

    public JSONObject(java.util.Map<String, Object> map) {
        super(map);
    }

    public JSONObject(boolean ordered) {
        super(ordered);
    }

    public JSONObject(int initialCapacity) {
        super(initialCapacity);
    }

    public JSONObject(int initialCapacity, boolean ordered) {
        super(initialCapacity, ordered);
    }

    @Override
    public <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder() {
        return (MapBuilder<K0, V0, M0>) new JSONObjectBuilder();
    }

    @Override
    public JSONObject filter(Predicate2<String, Object> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public JSONObject filter(Predicate1<Entry<String, Object>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public JSONObject filterNot(Predicate2<String, Object> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public JSONObject filterNot(Predicate1<Entry<String, Object>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public JSONObject take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    public JSONObject takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    @Override
    public JSONObject takeWhile(Predicate2<String, Object> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public JSONObject takeWhile(Predicate1<Entry<String, Object>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public JSONObject drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    public JSONObject dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    public JSONObject dropWhile(Predicate2<String, Object> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public JSONObject dropWhile(Predicate1<Entry<String, Object>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public JSONObject slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    public Tuple2<? extends JSONObject, ? extends JSONObject> span(Predicate2<String, Object> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends JSONObject, ? extends JSONObject> span(Predicate1<Entry<String, Object>> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends JSONObject, ? extends JSONObject> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    public <K0, V0> Map<K0, V0> map(Function2<String, Object, Tuple2<K0, V0>> action) {
        return null;
    }

    @Override
    public <K0, V0> Map<K0, V0> mapWithIndex(IndexedFunction2<String, Object, Entry<K0, V0>> action) {
        return null;
    }

    @Override
    public <K0, V0> Map<K0, V0> flatMap(Function1<Entry<String, Object>, Map<K0, V0>> action) {
        return null;
    }

    @Override
    public <K0, V0> Map<K0, V0> flatMap(Function2<String, Object, Map<K0, V0>> action) {
        return null;
    }

}
