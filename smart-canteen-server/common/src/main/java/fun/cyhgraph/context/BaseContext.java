package fun.cyhgraph.context;

public class BaseContext {
    // 【核心修复】彻底升级为 Long，解决拦截器中的类型转换错误
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }
}
