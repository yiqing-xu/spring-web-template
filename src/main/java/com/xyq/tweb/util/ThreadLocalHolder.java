package com.xyq.tweb.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

/**
 * <p>
 *
 * </p>
 *
 * @author xuyiqing
 * @since 2021/12/31
 */
public class ThreadLocalHolder {

    private static final Logger logger = LogManager.getLogger(ThreadLocalHolder.class);
    private static final ThreadLocal<Context> threadLocal = ThreadLocal.withInitial(Context::new);

    public static void set(Context context) {
        threadLocal.set(context);
    }

    public static Context get() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }

    public static void set(String field, Object value) {
        Context context = threadLocal.get();
        try {
            Field fieldField = context.getClass().getDeclaredField(field);
            if (!fieldField.isAccessible()) fieldField.setAccessible(true);
            fieldField.set(context, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object get(String field) {
        Object obj = null;
        Context context = threadLocal.get();
        try {
            Field fieldField = context.getClass().getDeclaredField(field);
            if (!fieldField.isAccessible()) fieldField.setAccessible(true);
            obj = fieldField.get(context);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static class Context {

    }

}
