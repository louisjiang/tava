package io.tava.fastjson.builder;

import io.tava.fastjson.JSONArray;
import io.tava.util.builder.CollectionBuilder;

public final class JSONArrayBuilder implements CollectionBuilder<Object, JSONArray> {

    private final JSONArray jsonArray = new JSONArray();

    @Override
    public JSONArray build() {
        return jsonArray;
    }
}
