package com.ruoyi.common.enums;

public enum ResponseCode {
//    SUCCESS(0, "成功"),
//    REQUEST_EMPTY(1000, "请求报文为空"),
//    APP_UNREGISTERED(1001, "应用系统未注册"),
//    APP_DISABLED(1002, "应用系统被禁用"),
//    REQUEST_FORMAT_ERROR(1003, "请求报文格式错误"),
//    SERVER_ERROR(1004, "服务端异常");
//
//    private final int code;
//    private final String message;
//
//    ResponseCode(int code, String message) {
//        this.code = code;
//        this.message = message;
//    }
//
//    public int getCode() { return code; }
//    public String getMessage() { return message; }
//
//    public static ResponseCode getByCode(int code) {
//        for (ResponseCode responseCode : values()) {
//            if (responseCode.getCode() == code) {
//                return responseCode;
//            }
//        }
//        return null;
//    }

    SUCCESS("0", "成功"),
    REQUEST_EMPTY("1000", "请求报文为空"),
    SYSTEM_NOT_REGISTERED("1001", "应用系统未注册"),
    SYSTEM_DISABLED("1002", "应用系统被禁用"),
    REQUEST_FORMAT_ERROR("1003", "请求报文格式错误"),
    SERVER_ERROR("1004", "服务端异常");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseCode getByCode(String code) {
        for (ResponseCode responseCode : values()) {
            if (responseCode.getCode().equals(code)) {
                return responseCode;
            }
        }
        return null;
    }
}
