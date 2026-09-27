package ${basePackage}.common;

/**
 * 业务异常，供各业务 Service 抛出。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
