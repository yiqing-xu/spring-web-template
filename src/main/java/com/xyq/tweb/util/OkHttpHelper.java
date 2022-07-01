package com.xyq.tweb.util;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <p>
 *
 * </p>
 *
 * @author 徐益庆
 * @since 2021/02/30
 */
public class OkHttpHelper {

    private static final Logger logger = Logger.getLogger(OkHttpHelper.class.getName());
    private static OkHttpClient okHttpClient;
    static {
        final TrustManager trustManager = buildTrustManager();
        final Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(5);
        dispatcher.setMaxRequestsPerHost(5);
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(buildHttpBasicInterceptor())
                .dispatcher(dispatcher)
                .connectTimeout(Duration.ofSeconds(20))
                .writeTimeout(Duration.ofSeconds(20))
                .readTimeout(Duration.ofSeconds(20))
                .callTimeout(Duration.ofSeconds(20))
                .retryOnConnectionFailure(true)
                .sslSocketFactory(createSSLSocketFactory(trustManager), (X509TrustManager) trustManager)
                .hostnameVerifier((String s, SSLSession sslSession) -> true)
                .build();
    }

    public static void setClient(OkHttpClient okHttpClient) {
        synchronized (OkHttpHelper.class) {
            OkHttpHelper.okHttpClient = okHttpClient;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder get() {
        return new Builder().get();
    }

    public static Builder get(String url) {
        return new Builder().get().url(url);
    }

    public static Builder post() {
        return new Builder().post();
    }

    public static Builder post(String url) {
        return new Builder().post().url(url);
    }

    public static Builder put() {
        return new Builder().put();
    }

    public static Builder put(String url) {
        return new Builder().put().url(url);
    }

    public static Builder delete() {
        return new Builder().delete();
    }

    public static Builder delete(String url) {
        return new Builder().delete().url(url);
    }

    public static Builder method(String method) {
        final HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        return new Builder().method(httpMethod);
    }

    public static Builder method(HttpMethod method) {
        return new Builder().method(method);
    }

    public static class RequestContext {
        private HttpMethod httpMethod = HttpMethod.GET;
        private HttpUrl.Builder httpUrl;
        private Request.Builder requestBuilder;
        private MultipartBody.Builder form;
        private String body;

        public RequestContext() {
        }

        public RequestContext(Request.Builder requestBuilder) {
            this.requestBuilder = requestBuilder;
        }

        public Response sync() {
            logger.info(String.format(
                    "HttpMethod=%s; URL=%s; QUERY=%s; BODY=%s",
                    httpMethod,
                    httpUrl,
                    httpUrl.build().query(),
                    body
            ));
            Response response = null;
            try {
                response = okHttpClient.newCall(requestBuilder.build()).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        public String string() {
            try (Response response = sync(); ResponseBody body = response.body()) {
                if (body != null) {
                    return body.string();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return null;
        }

        public byte[] bytes() {
            try (Response response = sync(); ResponseBody body = response.body();) {
                if (body != null) {
                    return body.bytes();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public InputStream inputStream() {
            Response response = sync();
            ResponseBody body = response.body();
            if (body != null) {
                return body.byteStream();
            }
            return null;
        }

        public void async() {
            okHttpClient.newCall(requestBuilder.build()).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    response.close();
                }
            });
        }

        public void async(Callback callBack) {
            okHttpClient.newCall(requestBuilder.build()).enqueue(callBack);
        }

    }

    public static class Builder {

        private final RequestContext context = new RequestContext(new Request.Builder());
        private static final MediaType APPLICATION_JSON = MediaType.parse("application/json");

        public RequestContext build() {
            context.requestBuilder.url(context.httpUrl.build());
            RequestBody requestBody = null;
            if (!context.httpMethod.equals(HttpMethod.GET)) {
                if (context.body != null) {
                    requestBody = RequestBody.create(context.body, APPLICATION_JSON);
                } else if (context.form != null) {
                    requestBody = context.form.setType(MultipartBody.FORM).build();
                } else {
                    requestBody = okhttp3.internal.Util.EMPTY_REQUEST;
                }
            }
            context.requestBuilder.method(context.httpMethod.toString(), requestBody);
            return context;
        }

        public RequestContext execute() {
            return build();
        }

        public Builder method(HttpMethod httpMethod) {
            context.httpMethod = httpMethod;
            return this;
        }

        public Builder method(String httpMethod) {
            context.httpMethod = HttpMethod.valueOf(httpMethod.toUpperCase());;
            return this;
        }

        public Builder url(String url) {
            if (context.httpUrl == null) {
                HttpUrl parse = HttpUrl.parse(url);
                if (parse == null) return this;
                context.httpUrl = parse.newBuilder();
            } else {
                context.httpUrl.build().newBuilder(url);
            }
            return this;
        }

        public Builder basic(String username, String password) {
            context.httpUrl.username(username).password(password);
            return this;
        }

        public Builder cookie(String key, String value) {
            context.requestBuilder.addHeader("Cookie", key + "=" + value);
            return this;
        }
        
        public Builder header(String key, String value) {
            context.requestBuilder.addHeader(key, value);
            return this;
        }

        public Builder query(String key, String value) {
            if (context.httpUrl == null) {
                context.httpUrl = new HttpUrl.Builder();
            }
            context.httpUrl.addQueryParameter(key, value);
            return this;
        }

        public Builder query(String key, String... value) {
            for (String val : value) {
                query(key, val);
            }
            return this;
        }

        public Builder query(String key, List<String> values) {
            for (String value : values) {
                query(key, value);
            }
            return this;
        }

        public Builder query(Map<String, String> query) {
            query.forEach(this::query);
            return this;
        }
        
        public Builder form(String key, File file) {
            byte[] bytes = readBytes(file);
            context.form.addFormDataPart(key, file.getName(), RequestBody.create(bytes));
            return this;
        }

        public Builder form(String key, File... files) {
            for (File file : files) {
                form(key, file);
            }
            return this;
        }

        public Builder form(String key, InputStream inputStream, String filename) {
            byte[] bytes = readBytes(inputStream);
            context.form.addFormDataPart(key, filename, RequestBody.create(bytes));
            return this;
        }

        public Builder form(String key, String value) {
            if (context.form == null) {
                context.form = new MultipartBody.Builder();
            }
            context.form.addFormDataPart(key, value);
            return this;
        }

        public Builder form(Map<String, String> form) {
            form.forEach(this::form);
            return this;
        }

        public Builder form(String key, List<String> values) {
            for (String value : values) {
                form(key, value);
            }
            return this;
        }

        public Builder form(String key, String... values) {
            List<String> stringList = Arrays.asList(values);
            return form(key, stringList);
        }

        public Builder body(String body) {
            context.body = body;
            return this;
        }

        public Builder body(byte[] body) {
            context.body = new String(body);
            return this;
        }

        public Builder get() {
            context.httpMethod = HttpMethod.GET;
            return this;
        }

        public Builder post() {
            context.httpMethod = HttpMethod.POST;
            return this;
        }

        public Builder put() {
            context.httpMethod = HttpMethod.PUT;
            return this;
        }

        public Builder delete() {
            context.httpMethod = HttpMethod.DELETE;
            return this;
        }

    }

    private static byte[] readBytes(InputStream inputStream) {
        byte[] outbytes = null;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[1024];
            while (bufferedInputStream.read(bytes) > 0) {
                byteArrayOutputStream.write(bytes);
            }
            outbytes = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outbytes;
    }
    
    private static byte[] readBytes(File file) {
        byte[] outbytes = null;
        try (FileInputStream inputStream = new FileInputStream(file);) {
            outbytes = readBytes(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outbytes;
    }

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    private static SSLSocketFactory createSSLSocketFactory(TrustManager trustAllCerts) {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{trustAllCerts}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ssfFactory;
    }

    private static TrustManager buildTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        };
    }

    private static Interceptor buildHttpBasicInterceptor() {
        return chain -> {
            Request request = chain.request();
            HttpUrl url = request.url();
            String username = url.username();
            if (username.isEmpty()) {
                return chain.proceed(request);
            }
            String basic = Credentials.basic(url.username(), url.password());
            Request authorization = request.newBuilder().header("Authorization", basic).build();
            return chain.proceed(authorization);
        };
    }

    public static void proxy(String proxyHost, Integer proxyPort, String proxyUser, String proxyPass) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        Authenticator proxyAuthenticator = (route, response) -> {
            String credential = Credentials.basic(proxyUser, proxyPass);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };
        okHttpClient = okHttpClient.newBuilder().proxy(proxy).proxyAuthenticator(proxyAuthenticator).build();
    }

}
