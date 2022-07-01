package com.xyq.tweb.bean.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author xuyiqing
 * @since 2022/6/22
 */
@Component
public class TestPool {

    @Async("customThreadPool")
    public void run() {
        System.out.println(Thread.currentThread().getName());
        System.out.println("");
    }

}
