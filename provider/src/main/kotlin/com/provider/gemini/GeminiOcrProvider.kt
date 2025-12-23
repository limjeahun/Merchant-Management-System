package com.provider.gemini

import com.application.port.out.ExternalOcrPort
import com.provider.gemini.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GeminiOcrProvider(
    @Value("\${google.gemini.api-key}")
    private val apiKey: String,
): ExternalOcrPort {
    private val restClient = RestClient.builder()
        .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
        .build()

    override fun extractText(imageUrl: String): String {
        // 1. 프롬프트 구성 (사업자등록증 파싱 전용)
        val systemPrompt = """
            당신은 한국의 '사업자등록증' 및 '신분증' 이미지를 분석하는 OCR 전문가입니다.
            이미지에서 다음 정보를 추출하여 반드시 **JSON 형식**으로만 응답하세요.
            다른 말은 하지 마세요. Markdown code block 없이 순수 JSON만 반환하세요.
            
            [추출 항목]
            - merchantName: 법인명(단체명) 또는 상호
            - businessNumber: 등록번호 (사업자등록번호)
            - representativeName: 대표자 성명
            - address: 사업장 소재지
            - businessType: 업태
            - businessItem: 종목
            - openingDate: 개업연월일 (YYYY-MM-DD 형식)
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = systemPrompt),
                        // image byte[]로 받아 base64로 변환
                        Part(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = imageUrl
                            )
                        )
                    )
                )
            )
        )

        // 3. API 호출
        val response = restClient.post()
            .uri { it.queryParam("key", apiKey).build() }
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(GeminiResponse::class.java)

        return response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "{}"
    }

}