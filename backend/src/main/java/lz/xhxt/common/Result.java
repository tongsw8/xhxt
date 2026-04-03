package lz.xhxt.common;

import lombok.Data;

/**
 * 统一返回结构
 */
@Data
public class Result {

    private Integer code;
    private Boolean success;
    private Object data;
    private String message;

    public static Result ok(Object data) {
        Result r = new Result();
        r.setCode(ResultCode.SUCCESS.code());
        r.setSuccess(true);
        r.setData(data);
        r.setMessage("success");
        return r;
    }

    public static Result error(Integer code, String message) {
        Result r = new Result();
        r.setCode(code);
        r.setSuccess(false);
        r.setData(null);
        r.setMessage(message);
        return r;
    }
}
