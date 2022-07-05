package com.xyq.tweb.util;

/**
 * <p>
 *
 * </p>
 *
 * @author xuyiqing
 * @since 2021/12/31
 */
public class ThreadLocalHolder {

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
        ReflectUtils.setFieldValue(context, field, value);
    }

    public static <T> T get(String field) {
        Context context = threadLocal.get();
        return ReflectUtils.getFieldValue(context, field);
    }

    public static class Context {

    }

}
