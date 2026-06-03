package com.ruoyi.accept.entity;


import lombok.Data;

@Data
public class ApiResponse {
    private boolean success;
    private String code;
    private String message;
    private Object data;

    // 构造方法
    public ApiResponse() {}

    public ApiResponse(boolean success, String code) {
        this.success = success;
        this.code = code;
    }

    public ApiResponse(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    // 成功响应
    public static ApiResponse success() {
        return new ApiResponse(true, "0", "成功");
    }

    public static ApiResponse success(Object data) {
        ApiResponse response = new ApiResponse(true, "0", "成功");
        response.setData(data);
        return response;
    }

    // 失败响应
    public static ApiResponse error(String code, String message) {
        return new ApiResponse(false, code, message);
    }
}
