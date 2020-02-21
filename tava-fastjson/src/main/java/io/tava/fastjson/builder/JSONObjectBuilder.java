package io.tava.fastjson.builder;

import io.tava.fastjson.JSONObject;
import io.tava.util.builder.MapBuilder;

public final class JSONObjectBuilder implements MapBuilder<String, Object, JSONObject> {

    private final JSONObject jsonObject = new JSONObject();

    @Override
    public JSONObject build() {
        return jsonObject;
    }

}
