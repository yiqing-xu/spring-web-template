package com.xyq.tweb.util;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * <p>
 * 一个RestTemplate的链式请求调用
 * </p>
 *
 * @author xuyiqing
 * @since 2022/5/30
 */
public class RestHelper {

    private static final Logger log = Logger.getLogger(RestHelper.class.getName());
    private static RestTemplate restTemplate;
    static {
        try {
            restTemplate = SpringContextUtils.getBean(RestTemplate.class);
        } catch (NoSuchBeanDefinitionException e) {
            restTemplate = new RestTemplate();
        }
        addInterceptor(createHttpBasicIntereptor());
        restTemplate.setErrorHandler(createResponseErrorHandler());
    }

    /**
     * 静态内部类实现懒加载线程池
     */
    private static class Executor {
        private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                10,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger poolNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("RestTemplateHelper-Executor-" + poolNumber.getAndIncrement());
                        if (thread.isDaemon())
                            thread.setDaemon(false);
                        if (thread.getPriority() != Thread.NORM_PRIORITY)
                            thread.setPriority(Thread.NORM_PRIORITY);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    private static void addInterceptor(ClientHttpRequestInterceptor interceptor) {
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add(interceptor);
    }

    /**
     * @return httpbasic认证拦截
     */
    private static ClientHttpRequestInterceptor createHttpBasicIntereptor() {
        return (request, body, execution) -> {
            String userInfo = request.getURI().getUserInfo();
            if (userInfo != null && !userInfo.isEmpty()) {
                String[] split = userInfo.split(":");
                String username = split[0];
                String password = split[1];
                HttpHeaders headers = request.getHeaders();
                headers.setBasicAuth(username, password, StandardCharsets.UTF_8);
            }
            return execution.execute(request, body);
        };
    }

    /**
     * @return statuscode非200异常绕过, 直接返回
     */
    private static ResponseErrorHandler createResponseErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

            }
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder get() {
        return new Builder().method(HttpMethod.GET);
    }

    public static Builder get(String url) {
        return new Builder().method(HttpMethod.GET).url(url);
    }

    public static Builder post() {
        return new Builder().method(HttpMethod.POST);
    }

    public static Builder post(String url) {
        return new Builder().method(HttpMethod.POST).url(url);
    }

    public static Builder put() {
        return new Builder().method(HttpMethod.PUT);
    }

    public static Builder put(String url) {
        return new Builder().method(HttpMethod.PUT).url(url);
    }

    public static Builder delete() {
        return new Builder().method(HttpMethod.DELETE);
    }

    public static Builder delete(String url) {
        return new Builder().method(HttpMethod.DELETE).url(url);
    }

    public static class RequestContext {
        private HttpMethod httpMethod = HttpMethod.GET;  // http请求方法
        private String url;                              // 资源地址
        private MultiValueMap<String, String> query;     // query GET请求参数
        private MultiValueMap<String, Object> form;      // formdata请求参数
        private HttpHeaders headers = new HttpHeaders(); // 请求头
        private Object body;                             // 请求体; 实体类基于jackson序列化
        private HttpEntity<Object> httpEntity;           // 请求组装实体

        private <T> ResponseEntity<T> doGET(Class<T> tClass) {
            return restTemplate.exchange(url, HttpMethod.GET, httpEntity, tClass);
        }

        private <T> ResponseEntity<T> doPOST(Class<T> tClass) {
            return restTemplate.exchange(url, HttpMethod.POST, httpEntity, tClass);
        }

        private <T> ResponseEntity<T> doPUT(Class<T> tClass) {
            return restTemplate.exchange(url, HttpMethod.PUT, httpEntity, tClass);
        }

        private <T> ResponseEntity<T> doDELETE(Class<T> tClass) {
            return restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, tClass);
        }

        // 响应体反序列化成实体对象
        public <T> T object(Class<T> t) {
            return entity(t).getBody();
        }

        // 响应体反序列化成字符串
        public String string() {
            return entity(String.class).getBody();
        }

        // 响应体反序列化成字节数组
        public byte[] bytes() {
            return entity(byte[].class).getBody();
        }

        // 响应体成文件资源
        public Resource resource() {
            return entity(Resource.class).getBody();
        }

        // 响应体保存成文件
        public void file(File file) {
            Resource resource = resource();
            if (file.isDirectory()) {
                String filename = resource.getFilename();
                if (filename != null && !filename.isEmpty()) {  // 文件流提取
                    file = new File(file, filename);
                } else {
                    String[] urls = url.split("/");  // 静态资源提取
                    String staticFilename = urls[urls.length - 1];
                    file = new File(file, staticFilename);
                }
            }
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(resource.getInputStream());
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                byte[] bytes = new byte[1024];
                while (bufferedInputStream.read(bytes) > 0) {
                    bufferedOutputStream.write(bytes);
                }
                bufferedOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 返回整个响应体
        public <T> ResponseEntity<T> entity(Class<T> tClass) {
            log.info(String.format("method=%s; url=%s; query=%s; form=%s; body=%s; header=%s",
                    url, httpMethod, query, form, body, headers));
            return restTemplate.exchange(url, httpMethod, httpEntity, tClass);
        }

        // 异步调用, 回调处理
        public <T> void async(AsyncRunnable runnable, Class<T> t) {
            Runnable job = () -> {
                try {
                    ResponseEntity<T> entity = entity(t);
                    runnable.onSuccess(entity);
                } catch (Exception e) {
                    runnable.onFail(e);
                }
            };
            Executor.executor.submit(job);
        }

        // 异步调用, 回调处理
        public void async() {
            Runnable job = () -> {
                entity(String.class);
            };
            Executor.executor.submit(job);
        }

    }

    public static class Builder {

        private final RequestContext context = new RequestContext();

        public RequestContext build() {
            if (context.query != null) {
                StringJoiner stringJoiner = new StringJoiner("&");
                context.query.forEach((k, vs) -> vs.forEach(v -> stringJoiner.add(k + "=" + v)));
                context.url = context.url + "?" + stringJoiner;
            }
            if (context.form != null) {
                if (context.headers.getContentType() == null) {  // formdata
                    context.headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                }
                context.httpEntity = new HttpEntity<>(context.form, context.headers);
            } else if (context.body != null) {
                if (context.headers.getContentType() == null) {  // body参数默认采用json格式
                    context.headers.setContentType(MediaType.APPLICATION_JSON);
                }
                context.httpEntity = new HttpEntity<>(context.body, context.headers);
            } else {
                context.httpEntity = new HttpEntity<>(context.headers);
            }
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
            context.httpMethod = HttpMethod.valueOf(httpMethod.toUpperCase());
            return this;
        }

        public Builder url(String url) {
            context.url = url;
            return this;
        }

        public Builder query(String key, String value) {
            if (context.query == null) {
                context.query = new LinkedMultiValueMap<>();
            }
            context.query.add(key, value);
            return this;
        }

        public Builder query(String key, List<String> values) {
            if (context.query == null) {
                context.query = new LinkedMultiValueMap<>();
            }
            context.query.addAll(key, values);
            return this;
        }

        public Builder query(Map<String, String> queries) {
            for (Map.Entry<String, String> entry : queries.entrySet()) {
                query(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder query(MultiValueMap<String, String> queries) {
            if (context.query == null) {
                context.query = new LinkedMultiValueMap<>();
            }
            context.query.addAll(queries);
            return this;
        }

        public Builder removeQuery(String key) {
            context.query.remove(key);
            return this;
        }

        public Builder form(String key, String value) {
            if (context.form == null) {
                context.form = new LinkedMultiValueMap<>();
            }
            context.form.add(key, value);
            return this;
        }

        public Builder form(String key, List<String> values) {
            if (context.form == null) {
                context.form = new LinkedMultiValueMap<>();
            }
            context.form.addAll(key, values);
            return this;
        }

        public Builder form(Map<String, String> forms) {
            for (Map.Entry<String, String> entry : forms.entrySet()) {
                form(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder form(MultiValueMap<String, String> forms) {
            if (context.form == null) {
                context.form = new LinkedMultiValueMap<>();
            }
            for (Map.Entry<String, List<String>> entry : forms.entrySet()) {
                form(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder form(String key, File file) {
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            if (context.form == null) {
                context.form = new LinkedMultiValueMap<>();
            }
            context.form.add(key, fileSystemResource);
            return this;
        }

        public Builder form(String key, String filename, File file) {
            try (FileInputStream inputStream = new FileInputStream(file);) {
                form(key, filename, inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder form(String key, String filename, InputStream inputStream) {
            try {
                byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
                ByteArrayResource byteArrayResource = new ByteArrayResource(bytes) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                };
                if (context.form == null) {
                    context.form = new LinkedMultiValueMap<>();
                }
                context.form.add(key, byteArrayResource);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder removeForm(String key) {
            context.form.remove(key);
            return this;
        }

        public Builder body(String body) {
            context.body = body;
            return this;
        }

        public Builder body(Object body) {
            context.body = body;
            return this;
        }

        public Builder removeBody() {
            context.body = null;
            return this;
        }

        public Builder header(String key, String value) {
            context.headers.add(key, value);
            return this;
        }

        public Builder header(HttpHeaders headers) {
            context.headers.addAll(headers);
            return this;
        }

        public Builder header(HttpHeaders headers, Boolean set) {
            if (set != null && set) {
                context.headers = headers;
            } else {
                context.headers.addAll(headers);
            }
            return this;
        }

        public Builder removeHeader(String key) {
            context.headers.remove(key);
            return this;
        }

        public Builder contentType(MediaType mediaType) {
            context.headers.setContentType(mediaType);
            return this;
        }

        public Builder cookie(String key, String value) {
            context.headers.add("Cookie", key + "=" + value);
            return this;
        }

        public Builder basic(String username, String password) {
            context.headers.setBasicAuth(username, password, StandardCharsets.UTF_8);
            return this;
        }

        public MultiValueMap<String, String> query() {
            return context.query;
        }

        public MultiValueMap<String, Object> form() {
            return context.form;
        }

        public Object body() {
            return context.body;
        }

        public HttpHeaders header() {
            return context.headers;
        }

    }

    public interface AsyncRunnable {

        <T> void onSuccess(ResponseEntity<T> responseEntity);

        void onFail(Exception e);

    }

}
