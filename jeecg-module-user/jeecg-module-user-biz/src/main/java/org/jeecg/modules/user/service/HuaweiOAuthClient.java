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
            JsonNode body = objectMapper.readTree(response.getBody());
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
        } catch (JeecgBootException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用华为 OAuth 异常", e);
            throw new JeecgBootException("华为登录验证失败，请稍后重试");
        }
    }

    @Data
    public static class HuaweiTokenResult {
        private String accessToken;
        private String idToken;
        private String refreshToken;
    }
}
