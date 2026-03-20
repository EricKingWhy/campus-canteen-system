package fun.cyhgraph.exception;

/**
 * 业务异常根类
 */
public class BaseException extends RuntimeException {
    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }
}
