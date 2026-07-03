package org.jeecg.modules.user.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 解析华为 idToken（JWT payload）
 */
@Slf4j
public final class HuaweiIdTokenUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HuaweiIdTokenUtil() {
    }

    public static JsonNode parsePayload(String idToken) {
        if (oConvertUtils.isEmpty(idToken)) {
            return null;
        }
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return MAPPER.readTree(new String(decoded, StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.warn("解析华为 idToken 失败: {}", e.getMessage());
            return null;
        }
    }

    public static String getSub(String idToken) {
        JsonNode payload = parsePayload(idToken);
        if (payload == null) {
            return null;
        }
        JsonNode sub = payload.get("sub");
        return sub != null && !sub.isNull() ? sub.asText() : null;
    }

    public static String getUnionId(String idToken) {
        JsonNode payload = parsePayload(idToken);
        if (payload == null) {
            return null;
        }
        JsonNode unionId = payload.get("unionid");
        if (unionId == null || unionId.isNull()) {
            unionId = payload.get("unionID");
        }
        return unionId != null && !unionId.isNull() ? unionId.asText() : null;
    }
}
