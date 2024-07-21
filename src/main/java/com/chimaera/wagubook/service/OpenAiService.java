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

    public String requestImageAnalysis(BufferedImage resizedImage, String menuName, String categoryName) throws IOException {
        // base 64 변환
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpeg", os);
        byte[] imageBytes = os.toByteArray();

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String imageUrl = "data:image/jpeg;base64," + base64Image;

        // 요청할 데이터 설정
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("model", "${openai.model}");
        requestData.put("max_tokens", 100);
        requestData.put("messages", Arrays.asList(
                Map.of("role", "user", "content", String.format("주어진 음식 이미지를 분석해서 이에 대한 긍정적인 리뷰를 100글자 내외로 사용자가 자신의 기록을 남기는 어투로 작성해줘! " +
                        "해당 음식은 메뉴 이름이 %s이고, 음식 카테고리가 %s인 음식이야." +
                        "이때 시간, 날짜 관련 표현은 제외해줘." +
                        "출력은 리뷰만 해줘.", imageUrl, menuName, categoryName))
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
