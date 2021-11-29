package io.tava.okhttp;

import com.alibaba.fastjson.JSON;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.*;
import okhttp3.internal.Util;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-18 16:29:16
 */
@Service
public class OkHttpClientService implements CookieJar {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Set<Cookie>> hostToCookies = new ConcurrentHashMap<>();
    private final List<Cookie> empty = new ArrayList<>();
    private final List<String> excludeCookieUrls = new ArrayList<>();
    private final OkHttpClient okHttpClient;

    public OkHttpClientService() {
        this(5, 5, 5, 5, 512);
    }

    public OkHttpClientService(long connectTimeout, long readTimeout, long writeTimeout, long callTimeout, int maxIdleConnections) {
        X509TrustManager trustManager = buildTrustManager();
        SSLSocketFactory sslSocketFactory = buildSSLSocketFactory(trustManager);
        if (sslSocketFactory == null) {
            throw new NullPointerException("sslSocketFactory is null");
        }
        this.okHttpClient = new OkHttpClient.Builder().
                connectTimeout(connectTimeout, TimeUnit.SECONDS).
                readTimeout(readTimeout, TimeUnit.SECONDS).
                writeTimeout(writeTimeout, TimeUnit.SECONDS).
                callTimeout(callTimeout, TimeUnit.SECONDS).
                sslSocketFactory(sslSocketFactory, trustManager).
                connectionPool(new ConnectionPool(maxIdleConnections, 5, TimeUnit.MINUTES)).
                connectionSpecs(Util.immutableListOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT)).cookieJar(this).build();
    }

    private SSLSocketFactory buildSSLSocketFactory(X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException cause) {
            this.logger.error("buildSSLSocketFactory", cause);
        }
        return null;
    }

    private X509TrustManager buildTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        };
    }

    public void clearCookies() {
        this.hostToCookies.clear();
    }

    public void addExcludeCookieUrl(String url) {
        this.excludeCookieUrls.add(url);
    }

    public Response get(String url) {
        return get(url, 1);
    }

    public Response get(String url, int retry) {
        return get(url, null, retry);
    }

    public Response get(String url, Map<String, String> headers) {
        return get(url, headers, 1);
    }

    public Response get(String url, Map<String, String> headers, int retry) {
        Request.Builder builder = new Request.Builder().get().url(url);
        if (headers != null && headers.size() > 0) {
            headers.forEach(builder::addHeader);
        }
        return request(builder.build(), retry);
    }

    public Response put(String url, Map<String, String> forms) {
        return put(url, forms, null);
    }

    public Response put(String url, Map<String, String> forms, Map<String, String> headers) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (forms != null && forms.size() > 0) {
            forms.forEach(formBodyBuilder::add);
        }
        return put(url, formBodyBuilder.build(), headers);
    }

    public Response put(String url, JSON json) {
        return put(url, json, JSON_MEDIA_TYPE);
    }

    public Response put(String url, JSON json, MediaType mediaType) {
        return put(url, json, mediaType, null);
    }

    public Response put(String url, JSON json, Map<String, String> headers) {
        return put(url, json, null, headers);
    }

    public Response put(String url, JSON json, MediaType mediaType, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(json.toJSONString(), mediaType);
        return put(url, requestBody, headers);
    }

    public Response put(String url, RequestBody requestBody) {
        return put(url, requestBody, null);
    }

    public Response put(String url, RequestBody requestBody, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).put(requestBody);
        if (headers != null && headers.size() > 0) {
            headers.forEach(builder::addHeader);
        }
        return request(builder.build());
    }


    public Response post(String url, Map<String, String> forms) {
        return post(url, forms, null);
    }

    public Response post(String url, Map<String, String> forms, Map<String, String> headers) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (forms != null && forms.size() > 0) {
            forms.forEach(formBodyBuilder::add);
        }
        return post(url, formBodyBuilder.build(), headers);
    }

    public Response post(String url, JSON json) {
        return post(url, json, JSON_MEDIA_TYPE);
    }

    public Response post(String url, JSON json, Map<String, String> headers) {
        return post(url, json, JSON_MEDIA_TYPE, headers);
    }

    public Response post(String url, JSON json, MediaType mediaType) {
        return post(url, json, mediaType, null);
    }

    public Response post(String url, JSON json, MediaType mediaType, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(json.toJSONString(), mediaType);
        return post(url, requestBody, headers);
    }

    public Response post(String url, RequestBody requestBody) {
        return post(url, requestBody, null);
    }

    public Response post(String url, RequestBody requestBody, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        if (headers != null && headers.size() > 0) {
            headers.forEach(builder::addHeader);
        }
        return request(builder.build());
    }

    public Response request(Request request, int retry) {
        RetryPolicy<Response> retryPolicy = new RetryPolicy<Response>().withMaxRetries(retry);
        try {
            return Failsafe.with(retryPolicy).get(() -> {
                Call call = okHttpClient.newCall(request);
                return call.execute();
            });
        } catch (Exception cause) {
            this.logger.error("request error:[{}]", request.url(), cause);
            return null;
        }
    }

    public Response request(Request request) {
        return request(request, 1);
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        String url = httpUrl.toString();
        for (String excludeCookieUrl : excludeCookieUrls) {
            if (url.startsWith(excludeCookieUrl)) {
                return empty;
            }
        }
        String host = httpUrl.host();
        Set<Cookie> cookies = hostToCookies.get(host);
        if (cookies == null) {
            return empty;
        }
        return new ArrayList<>(cookies);
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
        hostToCookies.computeIfAbsent(httpUrl.host(), k -> new HashSet<>()).addAll(list);
    }
}
