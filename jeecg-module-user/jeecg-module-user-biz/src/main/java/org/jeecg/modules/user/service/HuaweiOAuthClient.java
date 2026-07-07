package org.jeecg.modules.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.config.HuaweiOAuthProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * 华为 OAuth：authorizationCode 换 token
 */
@Slf4j
@Service
public class HuaweiOAuthClient {

    private final HuaweiOAuthProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HuaweiOAuthClient(HuaweiOAuthProperties properties) {
        this.properties = properties;
    }

    public HuaweiTokenResult exchangeAuthorizationCode(String authorizationCode) {
        if (oConvertUtils.isEmpty(properties.getClientId()) || oConvertUtils.isEmpty(properties.getClientSecret())) {
            throw new JeecgBootException("华为 OAuth 未配置 clientId/clientSecret，请在 application.yml 配置 thirdparty.huawei");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        if (oConvertUtils.isNotEmpty(properties.getRedirectUri())) {
            form.add("redirect_uri", properties.getRedirectUri());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json");

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    properties.getTokenUrl(),
                    new HttpEntity<>(form, headers),
                    String.class
            );
            return parseTokenResponse(response.getBody());
        } catch (HttpStatusCodeException e) {
            String desc = extractOAuthError(e.getResponseBodyAsString());
            log.error("华为 token 交换失败 HTTP {}: clientId={} desc={}",
                    e.getStatusCode(), maskClientId(properties.getClientId()), desc);
            if ("client_id not match".equals(desc)) {
                throw new JeecgBootException(
                        "华为登录验证失败: client_id 与 App 不一致，请将 thirdparty.huawei.client-id 改为与 HarmonyOS module.json5 中 metadata.client_id 相同（当前 App: 6917609941030203315）");
            }
            throw new JeecgBootException("华为登录验证失败: " + desc);
        } catch (JeecgBootException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用华为 OAuth 异常", e);
            throw new JeecgBootException("华为登录验证失败，请稍后重试");
        }
    }

    private HuaweiTokenResult parseTokenResponse(String responseBody) throws Exception {
        JsonNode body = objectMapper.readTree(responseBody);
        if (body.has("error")) {
            String desc = body.path("error_description").asText(body.path("error").asText("unknown"));
            log.error("华为 token 交换失败: {}", desc);
            throw new JeecgBootException("华为登录验证失败: " + desc);
        }
        HuaweiTokenResult result = new HuaweiTokenResult();
        result.setAccessToken(body.path("access_token").asText(null));
        result.setIdToken(body.path("id_token").asText(null));
        result.setRefreshToken(body.path("refresh_token").asText(null));
        if (oConvertUtils.isEmpty(result.getAccessToken()) && oConvertUtils.isEmpty(result.getIdToken())) {
            throw new JeecgBootException("华为登录验证失败: 未返回有效 token");
        }
        return result;
    }

    private String extractOAuthError(String responseBody) {
        if (oConvertUtils.isEmpty(responseBody)) {
            return "unknown";
        }
        try {
            JsonNode body = objectMapper.readTree(responseBody);
            if (body.has("error_description")) {
                return body.path("error_description").asText();
            }
            if (body.has("error")) {
                return body.path("error").asText();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return responseBody.length() > 200 ? responseBody.substring(0, 200) : responseBody;
    }

    private String maskClientId(String clientId) {
        if (oConvertUtils.isEmpty(clientId)) {
            return "(empty)";
        }
        if (clientId.length() <= 8) {
            return clientId;
        }
        return clientId.substring(0, 4) + "****" + clientId.substring(clientId.length() - 4);
    }

    @Data
    public static class HuaweiTokenResult {
        private String accessToken;
        private String idToken;
        private String refreshToken;
    }
}
