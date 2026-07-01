package org.jeecg.modules.interaction.moderation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.green20220302.Client;
import com.aliyun.green20220302.models.TextModerationRequest;
import com.aliyun.green20220302.models.TextModerationResponse;
import com.aliyun.green20220302.models.TextModerationResponseBody;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.interaction.config.FondiaAliyunGreenProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AliyunContentModerationService implements IContentModerationService {

    @Resource
    private FondiaAliyunGreenProperties properties;

    @Override
    public ModerationResult moderate(String content) {
        if (!properties.isEnabled()
                || oConvertUtils.isEmpty(properties.getAccessKeyId())
                || oConvertUtils.isEmpty(properties.getAccessKeySecret())) {
            log.debug("阿里云内容审核未启用或未配置密钥，默认通过");
            return ModerationResult.pass();
        }
        try {
            Config config = new Config()
                    .setAccessKeyId(properties.getAccessKeyId())
                    .setAccessKeySecret(properties.getAccessKeySecret())
                    .setEndpoint(properties.getEndpoint());
            Client client = new Client(config);

            Map<String, String> params = new HashMap<>();
            params.put("content", content);
            TextModerationRequest request = new TextModerationRequest()
                    .setService("comment_detection")
                    .setServiceParameters(JSON.toJSONString(params));

            TextModerationResponse response = client.textModeration(request);
            if (response == null || response.getBody() == null) {
                return ModerationResult.pending("审核服务无响应");
            }
            TextModerationResponseBody body = response.getBody();
            if (body.getCode() == null || body.getCode() != 200) {
                log.warn("阿里云审核调用失败 code={} msg={}", body.getCode(), body.getMessage());
                return ModerationResult.pending(body.getMessage());
            }
            TextModerationResponseBody.TextModerationResponseBodyData data = body.getData();
            if (data == null) {
                return ModerationResult.pass();
            }
            return parseModerationData(data);
        } catch (Exception e) {
            log.error("阿里云内容审核异常", e);
            return ModerationResult.pending("审核服务暂不可用");
        }
    }

    private ModerationResult parseModerationData(TextModerationResponseBody.TextModerationResponseBodyData data) {
        String labelsJson = data.getLabels();
        if (oConvertUtils.isEmpty(labelsJson) || "[]".equals(labelsJson.trim())) {
            return ModerationResult.pass();
        }

        try {
            JSONArray labels = JSON.parseArray(labelsJson);
            if (labels == null || labels.isEmpty()) {
                return ModerationResult.pass();
            }
            for (int i = 0; i < labels.size(); i++) {
                JSONObject label = labels.getJSONObject(i);
                if (label == null) {
                    continue;
                }
                String labelName = label.getString("label");
                if (oConvertUtils.isNotEmpty(labelName) && !"nonLabel".equalsIgnoreCase(labelName)) {
                    String reason = data.getReason();
                    if (oConvertUtils.isNotEmpty(reason)) {
                        return ModerationResult.reject(reason);
                    }
                    return ModerationResult.reject("内容不符合社区规范");
                }
            }
            return ModerationResult.pass();
        } catch (Exception e) {
            log.warn("解析阿里云审核 labels 失败: {}", labelsJson, e);
            if (oConvertUtils.isNotEmpty(data.getReason())) {
                return ModerationResult.reject(data.getReason());
            }
            return ModerationResult.pending("审核结果解析失败");
        }
    }
}
