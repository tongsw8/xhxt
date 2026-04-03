package lz.xhxt.common;

/**
 * 业务错误码（精简版，后续可扩展）
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    USER_LOGIN_ERROR(20002, "账号不存在或密码错误"),
    USER_DISABLED(20003, "账号已禁用"),
    USER_HAS_EXISTED(20005, "该账号已存在"),

    ILLEGAL_PARAMETER(30011, "非法参数");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return code;
    }

    public String message() {
        return message;
    }
}
