package io.tava.okhttp.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.tava.lang.Either;
import io.tava.lang.Option;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-11-24 13:33
 */
public class ResponseUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);

    public static Either<Throwable, Option<JSONObject>> toJSONObject(Either<Response, Exception> either) {
        if (either.isRight()) {
            return Either.right(Option.none());
        }
        return toJSONObject(either.left());
    }

    public static Either<Throwable, Option<JSONObject>> toJSONObject(Response response) {
        Either<Throwable, Option<String>> either = toString(response);
        if (either.isLeft()) {
            return Either.left(either.left());
        }
        Option<String> option = either.right();
        if (option.isEmpty()) {
            return Either.right(Option.none());
        }
        return parseObject(option.get());
    }

    private static Either<Throwable, Option<JSONObject>> parseObject(String value) {
        try {
            return Either.right(Option.option(JSON.parseObject(value)));
        } catch (Exception cause) {
            return Either.left(cause);
        }
    }

    public static Either<Throwable, Option<JSONArray>> toJSONArray(Either<Response, Exception> either) {
        if (either.isRight()) {
            return Either.right(Option.none());
        }
        return toJSONArray(either.left());
    }

    public static Either<Throwable, Option<JSONArray>> toJSONArray(Response response) {
        Either<Throwable, Option<String>> either = toString(response);
        if (either.isLeft()) {
            return Either.left(either.left());
        }
        Option<String> option = either.right();
        if (option.isEmpty()) {
            return Either.right(Option.none());
        }
        return parseArray(option.get());
    }

    private static Either<Throwable, Option<JSONArray>> parseArray(String value) {
        try {
            return Either.right(Option.option(JSON.parseArray(value)));
        } catch (Exception cause) {
            return Either.right(Option.none());
        }
    }

    public static Either<Throwable, Option<String>> toString(Response response) {
        if (response == null || response.code() != 200 && response.code() != 201) {
            close(response);
            return Either.right(Option.none());
        }
        try {
            return Either.right(Option.option(Objects.requireNonNull(response.body()).string()));
        } catch (IOException cause) {
            return Either.left(cause);
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
