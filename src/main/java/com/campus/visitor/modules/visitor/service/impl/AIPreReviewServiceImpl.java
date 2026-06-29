package com.campus.visitor.modules.visitor.service.impl;

import com.campus.visitor.modules.visitor.service.AIPreReviewService;
import com.campus.visitor.modules.visitor.entity.VisAppointment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * AI智能预审服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIPreReviewServiceImpl implements AIPreReviewService {

    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${ai.api.key}")
    private String aiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Async("aiTaskExecutor")
    public RiskLevel assessRisk(VisAppointment appointment) {
        try {
            // 异步调用AI API进行风险评估
            String riskLevel = callAIApi(appointment);
            
            // 根据AI返回结果判断风险等级
            if ("HIGH_RISK".equalsIgnoreCase(riskLevel)) {
                return RiskLevel.HIGH_RISK;
            } else {
                return RiskLevel.NORMAL;
            }
        } catch (Exception e) {
            log.error("AI风险评估失败，预约ID: {}", appointment.getId(), e);
            // 出现异常时，默认认为是正常预约
            return RiskLevel.NORMAL;
        }
    }

    /**
     * 调用AI API进行风险评估
     */
    private String callAIApi(VisAppointment appointment) {
        try {
            // 构造请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // 构造prompt
            StringBuilder prompt = new StringBuilder();
            prompt.append("请分析以下来访事由是否存在风险，如果包含维权、投诉、闹事等高风险内容，请回复HIGH_RISK，否则回复NORMAL。\n\n");
            prompt.append("来访事由：").append(appointment.getReason()).append("\n\n");
            prompt.append("请只回复HIGH_RISK或NORMAL，不要添加其他内容。");
            
            // 构造messages数组
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");
            message.put("content", prompt.toString());
            
            ObjectNode input = objectMapper.createObjectNode();
            input.set("messages", objectMapper.createArrayNode().add(message));
            
            requestBody.set("input", input);
            requestBody.put("model", "qwen-turbo"); // 使用通义千问turbo模型
            
            // 设置参数
            ObjectNode parameters = objectMapper.createObjectNode();
            parameters.put("temperature", 0.1); // 低温度确保结果稳定
            parameters.put("top_p", 0.8);
            parameters.put("seed", 1234);
            parameters.put("enable_search", false);
            
            requestBody.set("parameters", parameters);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);
            headers.set("X-DashScope-SSE", "disable");

            HttpEntity<ObjectNode> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(aiApiUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // 解析AI返回的结果
                ObjectNode result = (ObjectNode) objectMapper.readTree(response.getBody());
                
                // 提取AI的回答内容
                String output = result.get("output").get("text").asText();
                
                // 判断是否包含高风险关键词
                if (output.contains("HIGH_RISK")) {
                    return "HIGH_RISK";
                } else {
                    return "NORMAL";
                }
            } else {
                log.warn("AI API调用失败，状态码: {}", response.getStatusCode());
                return "NORMAL"; // 默认正常
            }
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            return "NORMAL"; // 默认正常
        }
    }
}