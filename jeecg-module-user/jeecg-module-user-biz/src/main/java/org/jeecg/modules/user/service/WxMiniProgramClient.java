package org.jeecg.modules.user.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.user.config.WxMiniProgramProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Component
public class WxMiniProgramClient {

    @Resource
    private WxMiniProgramProperties properties;

    public String codeToOpenId(String code) {
        if (oConvertUtils.isNotEmpty(properties.getAppId())
                && oConvertUtils.isNotEmpty(properties.getAppSecret())) {
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid="
                    + properties.getAppId()
                    + "&secret=" + properties.getAppSecret()
                    + "&js_code=" + code
                    + "&grant_type=authorization_code";
            try {
                String body = HttpUtil.get(url);
                JSONObject json = JSONUtil.parseObj(body);
                if (json.containsKey("openid")) {
                    return json.getStr("openid");
                }
                log.warn("微信 code2session 失败: {}", body);
            } catch (Exception e) {
                log.error("调用微信 jscode2session 异常", e);
            }
        }
        return "wx_dev_" + md5(code);
    }

    private String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return text;
        }
    }
}
