package com.xyq.tweb.controller;

import com.xyq.tweb.domain.web.Result;
import org.springframework.util.StringUtils;
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

    @Resource(name = "httpServletRequest")
    protected HttpServletRequest request;

    @Resource(name = "httpServletResponse")
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
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            while (inputStream.read(bytes) > 0) {
                outputStream.write(bytes);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
