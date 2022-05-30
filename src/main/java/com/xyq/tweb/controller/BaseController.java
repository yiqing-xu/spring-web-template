package com.xyq.tweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyq.tweb.domain.web.Result;
import com.xyq.tweb.util.RestTemplateUtils;
import com.xyq.tweb.util.SpringContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RestController
public class BaseController {

    @Resource
    protected HttpServletRequest request;

    @Resource
    protected HttpServletResponse response;

    public BaseController() {
    }

    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    protected void setResponseEntity(File file) {
        this.setResponseEntity(file, file.getName());
    }

    protected void setResponseEntity(File file, String filename) {
        try {
            filename = (StringUtils.isEmpty(filename) ? file.getName() : URLEncoder.encode(filename, "UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        try (FileInputStream inputStream = new FileInputStream(file);
             ServletOutputStream outputStream = response.getOutputStream()) {
            byte[] bytes = new byte[1024];
            while (inputStream.read(bytes) > 0) {
                outputStream.write(bytes);
            }
            inputStream.close();
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/test")
    public Object test() {
        return RestTemplateUtils.get().url("https://baidu.com").addQuery("code", "D4A0D013119D51GD30").build().object(Result.class).getData();
    }

}
