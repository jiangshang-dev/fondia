package org.jeecg.modules.ai.tools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tool 统一返回。业务数据放在 {@code data}，不要把查询结果拼成一段 String。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult<T> {

    private boolean success;
    /** OK / NOT_FOUND / NOT_IMPLEMENTED / INVALID_PARAM */
    private String code;
    /** 状态说明，不是给用户的最终回答 */
    private String message;
    private T data;

    public static <T> ToolResult<T> ok(T data) {
        return ToolResult.<T>builder()
                .success(true)
                .code("OK")
                .message("ok")
                .data(data)
                .build();
    }

    public static <T> ToolResult<T> fail(String code, String message) {
        return ToolResult.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ToolResult<T> notFound(String message) {
        return fail("NOT_FOUND", message);
    }

    public static <T> ToolResult<T> notImplemented(String toolName) {
        return fail("NOT_IMPLEMENTED", toolName + " 尚未接入业务服务，禁止编造数据库中不存在的记录");
    }
}
