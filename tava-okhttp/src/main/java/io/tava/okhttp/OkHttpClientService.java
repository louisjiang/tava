package io.tava.okhttp;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.tava.lang.Either;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class OkHttpClientService extends ProxySelector implements CookieJar, X509TrustManager, io.tava.util.Util {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Set<Cookie>> hostToCookies = new ConcurrentHashMap<>();
    private final List<Cookie> empty = new ArrayList<>();
    private final List<String> excludeCookieUrls = new ArrayList<>();
    private final OkHttpClient okHttpClient;
    private final List<Proxy> proxies = new ArrayList<>();
    private final List<String> proxyHosts = new ArrayList<>();
    private final boolean disableCookies;

    public OkHttpClientService() {
        this(true, false, 5, 5, 5, 5, 5, 256, 128, 256, 5);
    }

    public OkHttpClientService(long connectTimeout, long readTimeout, long writeTimeout, long callTimeout, long pingInterval, int maxRequests, int maxRequestsPerHost, int maxIdleConnections, int keepAliveDuration) {
        this(true, false, connectTimeout, readTimeout, writeTimeout, callTimeout, pingInterval, maxRequests, maxRequestsPerHost, maxIdleConnections, keepAliveDuration);
    }

    public OkHttpClientService(boolean disableCookies, boolean useHttp1_1, long connectTimeout, long readTimeout, long writeTimeout, long callTimeout, long pingInterval, int maxRequests, int maxRequestsPerHost, int maxIdleConnections, int keepAliveDuration) {
        this.disableCookies = disableCookies;
        SSLSocketFactory sslSocketFactory = buildSSLSocketFactory();
        if (sslSocketFactory == null) {
            throw new NullPointerException("sslSocketFactory is null");
        }
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);

        ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (useHttp1_1) {
            builder.protocols(List.of(Protocol.HTTP_1_1));
        }
        this.okHttpClient = builder.connectTimeout(connectTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS).writeTimeout(writeTimeout, TimeUnit.SECONDS).callTimeout(callTimeout, TimeUnit.SECONDS).pingInterval(pingInterval, TimeUnit.SECONDS).sslSocketFactory(sslSocketFactory, this).connectionPool(connectionPool).dispatcher(dispatcher).connectionSpecs(Arrays.asList(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT)).proxySelector(this).cookieJar(this).build();
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

    public Either<Response, Exception> get(String url) {
        return get(url, null);
    }

    public Either<Response, Exception> get(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().get().url(url);
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(builder::addHeader);
        }
        return request(builder.build());
    }

    public Either<Response, Exception> put(String url, Map<String, String> forms) {
        return put(url, forms, null);
    }

    public Either<Response, Exception> put(String url, Map<String, String> forms, Map<String, String> headers) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (forms != null && !forms.isEmpty()) {
            forms.forEach(formBodyBuilder::add);
        }
        return put(url, formBodyBuilder.build(), headers);
    }

    public Either<Response, Exception> put(String url, JSONObject json) {
        return put(url, json, JSON_MEDIA_TYPE);
    }

    public Either<Response, Exception> put(String url, JSONObject json, MediaType mediaType) {
        return put(url, json, mediaType, null);
    }

    public Either<Response, Exception> put(String url, JSONObject json, Map<String, String> headers) {
        return put(url, json, null, headers);
    }

    public Either<Response, Exception> put(String url, JSONObject json, MediaType mediaType, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(json.toString(), mediaType);
        return put(url, requestBody, headers);
    }

    public Either<Response, Exception> put(String url, JSONArray json) {
        return put(url, json, JSON_MEDIA_TYPE);
    }

    public Either<Response, Exception> put(String url, JSONArray json, MediaType mediaType) {
        return put(url, json, mediaType, null);
    }

    public Either<Response, Exception> put(String url, JSONArray json, Map<String, String> headers) {
        return put(url, json, null, headers);
    }

    public Either<Response, Exception> put(String url, JSONArray json, MediaType mediaType, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(json.toString(), mediaType);
        return put(url, requestBody, headers);
    }

    public Either<Response, Exception> put(String url, RequestBody requestBody) {
        return put(url, requestBody, null);
    }

    public Either<Response, Exception> put(String url, RequestBody requestBody, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).put(requestBody);
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(builder::addHeader);
        }
        return request(builder.build());
    }

    public Either<Response, Exception> post(String url, Map<String, String> forms) {
        return post(url, forms, null);
    }


    public Either<Response, Exception> post(String url, Map<String, String> forms, Map<String, String> headers) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (forms != null && !forms.isEmpty()) {
            forms.forEach(formBodyBuilder::add);
        }
        return post(url, formBodyBuilder.build(), headers);
    }

    public Either<Response, Exception> post(String url, JSONObject json) {
        return post(url, json, JSON_MEDIA_TYPE);
    }


    public Either<Response, Exception> post(String url, JSONObject json, Map<String, String> headers) {
        return post(url, json, JSON_MEDIA_TYPE, headers);
    }

    public Either<Response, Exception> post(String url, JSONObject json, MediaType mediaType) {
        return post(url, json, mediaType, null);
    }

    public Either<Response, Exception> post(String url, JSONObject json, MediaType mediaType, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(json.toString(), mediaType);
        return post(url, requestBody, headers);
    }

    public Either<Response, Exception> post(String url, JSONArray json) {
        return post(url, json, JSON_MEDIA_TYPE);
    }

    public Either<Response, Exception> post(String url, JSONArray json, Map<String, String> headers) {
        return post(url, json, JSON_MEDIA_TYPE, headers);
    }

    public Either<Response, Exception> post(String url, JSONArray json, MediaType mediaType) {
        return post(url, json, mediaType, null);
    }

    public Either<Response, Exception> post(String url, JSONArray json, MediaType mediaType, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(json.toString(), mediaType);
        return post(url, requestBody, headers);
    }

    public Either<Response, Exception> post(String url, RequestBody requestBody) {
        return post(url, requestBody, null);
    }


    public Either<Response, Exception> post(String url, RequestBody requestBody, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(builder::addHeader);
        }
        return request(builder.build());
    }

    public Either<Response, Exception> request(Request request) {
        try {
            Call call = okHttpClient.newCall(request);
            return Either.left(call.execute());
        } catch (Exception cause) {
            this.logger.error("[{}],[{}]", request.url(), cause.getLocalizedMessage());
            return Either.right(cause);
        }
    }

    public WebSocket webSocket(String url, WebSocketListener webSocketListener) {
        Request request = new Request.Builder().get().url(url).build();
        return this.okHttpClient.newWebSocket(request, webSocketListener);
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        if (disableCookies) {
            return empty;
        }
        String url = httpUrl.toString();
        for (String excludeCookieUrl : this.excludeCookieUrls) {
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
        if (disableCookies) {
            return;
        }
        String url = httpUrl.toString();
        for (String excludeCookieUrl : this.excludeCookieUrls) {
            if (url.startsWith(excludeCookieUrl)) {
                return;
            }
        }

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
