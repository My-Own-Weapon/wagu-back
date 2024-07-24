package com.chimaera.wagubook.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiService {
    @Value("${openai.model}")
    private String apiModel;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    // 이미지
    public String requestImageAnalysis(BufferedImage resizedImage, String menuName, String categoryName) throws IOException {
        // base 64 변환
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpeg", os);
        byte[] imageBytes = os.toByteArray();

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String imageUrl = "data:image/jpeg;base64," + base64Image;

        // 요청할 데이터 설정
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("model", apiModel);
        requestData.put("max_tokens", 150);
        requestData.put("messages", Arrays.asList(
                Map.of("role", "user", "content", String.format("주어진 음식 이미지를 분석해서 이에 대한 긍정적인 리뷰를 100글자 내외로 남겨줘" +
                        "해당 음식은 이름이 %s이고, 카테고리가 %s인 음식이야." +
                        "이때 숫자, 시간, 날짜 관련 표현은 제외해줘." +
                        "또한 사용자가 기록을 남기는 어투로 작성해줘야 하고, 반드시 완성형 문장이어야 해!" +
                        "마지막으로 출력은 리뷰만 해줘.", imageUrl, menuName, categoryName))
        ));

        // OpenAI API 호출
        Map<String, Object> response = restTemplate.postForObject(apiUrl, requestData, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> choice = choices.get(0);
        Map<String, String> message = (Map<String, String>) choice.get("message");
        String review = message.get("content");

        return review;
    }

    // 텍스트
    public String requestText(String menuName, String categoryName) throws IOException {
        // 요청할 데이터 설정
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("model", apiModel);
        requestData.put("max_tokens", 150);
        requestData.put("messages", Arrays.asList(
                Map.of("role", "user", "content", String.format("주어진 음식 이미지를 분석해서 이에 대한 긍정적인 리뷰를 100글자 내외로 남겨줘" +
                        "해당 음식은 이름이 %s이고, 카테고리가 %s인 음식이야." +
                        "이때 숫자, 시간, 날짜 관련 표현은 제외해줘." +
                        "또한 사용자가 기록을 남기는 어투로 작성해줘야 하고, 반드시 완성형 문장이어야 해!" +
                        "마지막으로 출력은 리뷰만 해줘.", menuName, categoryName))
        ));

        // OpenAI API 호출
        Map<String, Object> response = restTemplate.postForObject(apiUrl, requestData, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> choice = choices.get(0);
        Map<String, String> message = (Map<String, String>) choice.get("message");
        String review = message.get("content");

        return review;
    }
}
