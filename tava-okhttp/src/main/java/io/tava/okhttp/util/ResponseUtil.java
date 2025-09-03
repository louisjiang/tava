package io.tava.okhttp.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.tava.lang.Option;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-11-24 13:33
 */
public class ResponseUtil {

    public static Option<JSONObject> toJSONObject(Response response) {
        return toString(response).map(ResponseUtil::parseObject);
    }

    private static JSONObject parseObject(String value) {
        try {
            return JSON.parseObject(value);
        } catch (Exception cause) {
            return null;
        }
    }

    public static Option<JSONArray> toJSONArray(Response response) {
        return toString(response).map(ResponseUtil::parseArray);
    }

    private static JSONArray parseArray(String value) {
        try {
            return JSON.parseArray(value);
        } catch (Exception cause) {
            return null;
        }
    }

    public static Option<String> toString(Response response) {
        if (response == null || response.code() != 200 && response.code() != 201) {
            close(response);
            return Option.none();
        }
        try {
            return Option.option(Objects.requireNonNull(response.body()).string());
        } catch (IOException e) {
            return Option.none();
        } finally {
            close(response);
        }

    }

    public static void close(Response response) {
        if (response == null) {
            return;
        }
        response.close();
    }

}
