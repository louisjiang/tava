package io.tava.okhttp;

import com.alibaba.fastjson.JSON;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.*;
import okhttp3.internal.Util;
import okio.ByteString;
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
import java.util.concurrent.TimeUnit;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-18 16:29:16
 */
@Service
public class OkHttpClientService implements CookieJar {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Set<Cookie>> hostToCookies = new HashMap<>();
    private final List<Cookie> empty = new ArrayList<>();
    private final List<String> excludeCookieUrls = new ArrayList<>();
    private final OkHttpClient okHttpClient;


    public OkHttpClientService() {
        X509TrustManager[] trustManagers = buildTrustManagers();
        SSLSocketFactory sslSocketFactory = buildSSLSocketFactory(trustManagers);

        assert sslSocketFactory != null;
        okHttpClient = new OkHttpClient.Builder().
                connectTimeout(5, TimeUnit.SECONDS).
                readTimeout(5, TimeUnit.SECONDS).
                callTimeout(5, TimeUnit.SECONDS).
                sslSocketFactory(sslSocketFactory, trustManagers[0]).
                connectionSpecs(Util.immutableListOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT)).cookieJar(this).build();
    }

    private SSLSocketFactory buildSSLSocketFactory(TrustManager[] trustAllCerts) {
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException cause) {
            this.logger.error("buildSSLSocketFactory", cause);
        }
        return null;
    }

    private X509TrustManager[] buildTrustManagers() {
        return new X509TrustManager[]{
                new X509TrustManager() {
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
                }
        };
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
        return post(url, json, null);
    }

    public Response post(String url, JSON json, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(ByteString.encodeUtf8(json.toJSONString()), JSON_MEDIA_TYPE);
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
            this.logger.error("request:{}", cause.getLocalizedMessage());
            return null;
        }
    }

    public Response request(Request request) {
        return request(request, 1);
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        if (excludeCookieUrls.contains(httpUrl.toString())) {
            return empty;
        }
        String host = httpUrl.host();
        synchronized (hostToCookies) {
            Set<Cookie> cookies = hostToCookies.get(host);
            if (cookies == null) {
                return empty;
            }
            return new ArrayList<>(cookies);
        }
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
        String host = httpUrl.host();
        synchronized (hostToCookies) {
            Set<Cookie> cookies = hostToCookies.computeIfAbsent(host, k -> new HashSet<>());
            cookies.addAll(list);
        }

    }
}
