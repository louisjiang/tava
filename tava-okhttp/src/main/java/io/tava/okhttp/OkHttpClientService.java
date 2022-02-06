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
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-18 16:29:16
 */
@Service
public class OkHttpClientService extends ProxySelector implements CookieJar, X509TrustManager, io.tava.util.Util {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Set<Cookie>> hostToCookies = new ConcurrentHashMap<>();
    private final List<Cookie> empty = new ArrayList<>();
    private final List<String> excludeCookieUrls = new ArrayList<>();
    private final OkHttpClient okHttpClient;
    private final List<Proxy> proxies = new ArrayList<>();
    private final List<String> proxyHosts = new ArrayList<>();

    public OkHttpClientService() {
        this(5, 5, 5, 5, 5, 512);
    }

    public OkHttpClientService(long connectTimeout, long readTimeout, long writeTimeout, long callTimeout, long pingInterval, int maxIdleConnections) {
        SSLSocketFactory sslSocketFactory = buildSSLSocketFactory();
        if (sslSocketFactory == null) {
            throw new NullPointerException("sslSocketFactory is null");
        }
        this.okHttpClient = new OkHttpClient.Builder().
                connectTimeout(connectTimeout, TimeUnit.SECONDS).
                readTimeout(readTimeout, TimeUnit.SECONDS).
                writeTimeout(writeTimeout, TimeUnit.SECONDS).
                callTimeout(callTimeout, TimeUnit.SECONDS).
                pingInterval(pingInterval, TimeUnit.SECONDS).
                sslSocketFactory(sslSocketFactory, this).
                connectionPool(new ConnectionPool(maxIdleConnections, 5, TimeUnit.MINUTES)).
                connectionSpecs(Util.immutableListOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT)).
                proxySelector(this).
                cookieJar(this).
                build();
    }

    private SSLSocketFactory buildSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{this}, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException cause) {
            this.logger.error("buildSSLSocketFactory", cause);
        }
        return null;
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
        return post(url, forms, 1);
    }

    public Response post(String url, Map<String, String> forms, int retry) {
        return post(url, forms, null, retry);
    }

    public Response post(String url, Map<String, String> forms, Map<String, String> headers) {
        return post(url, forms, headers, 1);
    }

    public Response post(String url, Map<String, String> forms, Map<String, String> headers, int retry) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (forms != null && forms.size() > 0) {
            forms.forEach(formBodyBuilder::add);
        }
        return post(url, formBodyBuilder.build(), headers, retry);
    }

    public Response post(String url, JSON json) {
        return post(url, json, 1);
    }

    public Response post(String url, JSON json, int retry) {
        return post(url, json, JSON_MEDIA_TYPE, retry);
    }

    public Response post(String url, JSON json, Map<String, String> headers) {
        return post(url, json, headers, 1);
    }

    public Response post(String url, JSON json, Map<String, String> headers, int retry) {
        return post(url, json, JSON_MEDIA_TYPE, headers, retry);
    }

    public Response post(String url, JSON json, MediaType mediaType) {
        return post(url, json, mediaType, 1);
    }

    public Response post(String url, JSON json, MediaType mediaType, int retry) {
        return post(url, json, mediaType, null, retry);
    }

    public Response post(String url, JSON json, MediaType mediaType, Map<String, String> headers) {
        return post(url, json, mediaType, headers, 1);
    }

    public Response post(String url, JSON json, MediaType mediaType, Map<String, String> headers, int retry) {
        RequestBody requestBody = RequestBody.create(json.toJSONString(), mediaType);
        return post(url, requestBody, headers, retry);
    }

    public Response post(String url, RequestBody requestBody) {
        return post(url, requestBody, 1);
    }

    public Response post(String url, RequestBody requestBody, int retry) {
        return post(url, requestBody, null, retry);
    }

    public Response post(String url, RequestBody requestBody, Map<String, String> headers) {
        return post(url, requestBody, headers, 1);
    }

    public Response post(String url, RequestBody requestBody, Map<String, String> headers, int retry) {
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        if (headers != null && headers.size() > 0) {
            headers.forEach(builder::addHeader);
        }
        return request(builder.build(), retry);
    }

    public Response request(Request request, int retry) {
        RetryPolicy<Response> retryPolicy = new RetryPolicy<Response>().withMaxRetries(retry);
        try {
            return Failsafe.with(retryPolicy).get(() -> {
                Call call = okHttpClient.newCall(request);
                return call.execute();
            });
        } catch (Exception cause) {
            this.logger.error("{}:[{}]", cause.getMessage(), request.url());
            return null;
        }
    }

    public Response request(Request request) {
        return request(request, 1);
    }

    public WebSocket webSocket(String url, WebSocketListener webSocketListener) {
        Request request = new Request.Builder().get().url(url).build();
        return this.okHttpClient.newWebSocket(request, webSocketListener);
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

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }


    @Override
    public List<Proxy> select(URI uri) {
        if (isNull(this.proxies) || !proxyHosts.contains(uri.getHost())) {
            return null;
        }
        return proxies;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

    }


    public List<Proxy> getProxies() {
        return proxies;
    }

    public void setProxies(List<Proxy> proxies) {
        this.proxies.addAll(proxies);
    }

    public void addProxy(Proxy proxy) {
        this.proxies.add(proxy);
    }

    public void addProxyHost(String host) {
        this.proxyHosts.add(host);
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

}
