package com.provider.pororo

import com.common.ocr.OcrRawResult
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

/** Pororo OCR API Provider Kakaobrain Pororo 기반 한국어 OCR 서비스 클라이언트 */
@Component
class PororoOcrProvider(
        @Value("\${pororo.api-url:http://localhost:9004}") private val apiUrl: String,
        @Value("\${pororo.timeout:60s}") private val timeout: String
) {
    private val logger = LoggerFactory.getLogger(PororoOcrProvider::class.java)
    private val webClient = WebClient.builder().baseUrl(apiUrl).build()

    private val timeoutDuration: Duration
        get() = Duration.parse("PT${timeout.uppercase()}")

    /** 이미지에서 텍스트 추출 */
    fun extractText(imageBytes: ByteArray): OcrRawResult {
        return try {
            logger.info("Starting Pororo OCR extraction...")

            val bodyBuilder = MultipartBodyBuilder()
            bodyBuilder
                    .part("image_file", imageBytes)
                    .filename("image.png")
                    .contentType(MediaType.IMAGE_PNG)

            val response =
                    webClient
                            .post()
                            .uri("/ocr")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                            .retrieve()
                            .bodyToMono(PororoOcrResponse::class.java)
                            .block(timeoutDuration)

            if (response?.success == true) {
                logger.info("Pororo OCR completed, extracted ${response.line_count} lines")
                OcrRawResult(
                        success = true,
                        fullText = response.text ?: "",
                        lines = response.lines?.map { it.text } ?: emptyList(),
                        confidence = response.lines?.map { it.confidence }?.average() ?: 0.0,
                        engine = "pororo"
                )
            } else {
                logger.warn("Pororo OCR failed: ${response?.error}")
                OcrRawResult(
                        success = false,
                        errorMessage = response?.error ?: "Unknown error",
                        engine = "pororo"
                )
            }
        } catch (e: Exception) {
            logger.error("Pororo OCR extraction failed: ${e.message}", e)
            OcrRawResult(
                    success = false,
                    errorMessage = e.message ?: "Unknown error",
                    engine = "pororo"
            )
        }
    }

    /** 헬스체크 */
    fun isHealthy(): Boolean {
        return try {
            val response =
                    webClient
                            .get()
                            .uri("/health")
                            .retrieve()
                            .bodyToMono(Map::class.java)
                            .block(Duration.ofSeconds(5))
            response?.get("status") == "healthy"
        } catch (e: Exception) {
            logger.warn("Pororo health check failed: ${e.message}")
            false
        }
    }
}

data class PororoOcrResponse(
        val success: Boolean,
        val text: String?,
        val lines: List<PororoLine>?,
        val line_count: Int?,
        val korean_ratio: Double?,
        val error: String?
)

data class PororoLine(val text: String, val confidence: Double)
