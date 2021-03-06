package io.tava.fastjson;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-03 13:42:01
 */
public class Adapter {

    public static JSONObject jsonObject(com.alibaba.fastjson.JSONObject jsonObject) {
        return new JSONObject(jsonObject);
    }

    public static JSONArray jsonArray(com.alibaba.fastjson.JSONArray jsonArray) {
        return new JSONArray(jsonArray);
    }

}
