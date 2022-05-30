package com.xyq.tweb.util;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


public class RestTemplateUtils {

    private static RestTemplate restTemplate;

    static {
        try {
            restTemplate = SpringContextUtils.getBean(RestTemplate.class);
        } catch (NoSuchBeanDefinitionException e) {
            restTemplate = new RestTemplate();
        }
        restTemplate.setInterceptors(Collections.singletonList(createHttpBasicIntereptor()));
    }

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
        private HttpMethod httpMethod = HttpMethod.GET;
        private String url;
        private MultiValueMap<String, String> query;
        private MultiValueMap<String, Object> form;
        private final HttpHeaders headers = new HttpHeaders();
        private Object body;
        private HttpEntity<Object> httpEntity;

        private <T> ResponseEntity<T> doGET(Class<T> tClass) {
            return restTemplate.exchange(this.url, HttpMethod.GET, httpEntity, tClass);
        }

        private <T> ResponseEntity<T> doPOST(Class<T> tClass) {
            return restTemplate.postForEntity(this.url, httpEntity, tClass);
        }

        private <T> ResponseEntity<T> doPUT(Class<T> tClass) {
            return restTemplate.exchange(url, HttpMethod.PUT, httpEntity, tClass);
        }

        private <T> ResponseEntity<T> doDelete(Class<T> tClass) {
            return restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, tClass);
        }

        public <T> T object(Class<T> t) {
            return this.entity(t).getBody();
        }

        public String string() {
            return this.entity(String.class).getBody();
        }

        public byte[] bytes() {
            return this.entity(byte[].class).getBody();
        }

        public Resource resource() {
            return this.entity(Resource.class).getBody();
        }

        public <T> ResponseEntity<T> entity(Class<T> tClass) {
            if (httpMethod.equals(HttpMethod.GET)) {
                return doGET(tClass);
            } else if (httpMethod.equals(HttpMethod.POST)) {
                return doPOST(tClass);
            } else if (httpMethod.equals(HttpMethod.PUT)) {
                return doPUT(tClass);
            } else if (httpMethod.equals(HttpMethod.DELETE)) {
                return doDelete(tClass);
            } else {
                return doGET(tClass);
            }
        }

    }

    public static class Builder {

        private final RequestContext context = new RequestContext();

        public RequestContext build() {
            if (context.query != null) {
                StringJoiner stringJoiner = new StringJoiner("&");
                for (Map.Entry<String, List<String>> entry : context.query.entrySet()) {
                    for (String value : entry.getValue()) {
                        stringJoiner.add(entry.getKey() + "=" + value);
                    }
                }
                context.url = context.url + "?" + stringJoiner;
            }
            if (context.form != null) {
                context.headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                context.httpEntity = new HttpEntity<>(context.form, context.headers);
            } else if (context.body != null) {
                context.headers.setContentType(MediaType.APPLICATION_JSON);
                context.httpEntity = new HttpEntity<>(context.body, context.headers);
            } else {
                context.httpEntity = new HttpEntity<>(context.headers);
            }
            return context;
        }

        public Builder method(HttpMethod httpMethod) {
            context.httpMethod = httpMethod;
            return this;
        }

        public Builder url(String url) {
            context.url = url;
            return this;
        }

        public Builder addQuery(String key, String value) {
            if (context.query == null) {
                context.query = new LinkedMultiValueMap<>();
            }
            context.query.add(key, value);
            return this;
        }

        public Builder addQuery(String key, List<String> values) {
            if (context.query == null) {
                context.query = new LinkedMultiValueMap<>();
            }
            context.query.addAll(key, values);
            return this;
        }

        public Builder addQuery(Map<String, String> queries) {
            for (Map.Entry<String, String> entry : queries.entrySet()) {
                this.addQuery(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder addForm(String key, String value) {
            if (context.form == null) {
                context.form = new LinkedMultiValueMap<>();
            }
            context.form.add(key, value);
            return this;
        }

        public Builder addForm(String key, List<String> values) {
            if (context.form == null) {
                context.form = new LinkedMultiValueMap<>();
            }
            context.form.addAll(key, values);
            return this;
        }

        public Builder addForm(Map<String, String> forms) {
            for (Map.Entry<String, String> entry : forms.entrySet()) {
                this.addForm(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder addFile(String key, File file) {
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            if (context.form == null) {
                context.form = new LinkedMultiValueMap<>();
            }
            context.form.add(key, fileSystemResource);
            return this;
        }

        public Builder addFile(String key, List<File> files) {
            for (File file : files) {
                this.addFile(key, file);
            }
            return this;
        }

        public Builder addFile(String key, String filename, File file) {
            try (FileInputStream inputStream = new FileInputStream(file);) {
                this.addFile(key, filename, inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder addFile(String key, String filename, InputStream inputStream) {
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
            } finally {
                try {
                    inputStream.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
            return this;
        }

        public Builder addBody(String body) {
            context.body = body;
            return this;
        }

        public Builder addBody(Object body) {
            context.body = body;
            return this;
        }

        public Builder addHeader(String key, String value) {
            context.headers.add(key, value);
            return this;
        }

        public Builder addCookie(String key, String value) {
            context.headers.add("Cookie", key + "=" + value);
            return this;
        }

        public Builder setBasicAuth(String username, String password) {
            context.headers.setBasicAuth(username, password, StandardCharsets.UTF_8);
            return this;
        }

    }

}
