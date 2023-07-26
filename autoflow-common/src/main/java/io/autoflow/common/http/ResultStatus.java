package io.autoflow.common.http;

/**
 * 影响结果状态枚举
 *
 * @author yiuman
 * @date 2022/6/22
 */
public enum ResultStatus {
    OK("成功", 100200),
    BAD_REQUEST("请求错误", 100400),
    SERVER_ERROR("服务器异常", 100500);
    private final String text;
    private final int statusCode;

    ResultStatus(String text, int statusCode) {
        this.text = text;
        this.statusCode = statusCode;
    }

    public String getText() {
        return text;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
