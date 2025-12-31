package com.provider.onnxtr

import com.common.ocr.OcrRawResult
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

/** OnnxTR OCR API Provider OnnxTR (DocTR ONNX) 기반 다국어 OCR 서비스 클라이언트 */
@Component
class OnnxtrOcrProvider(
        @Value("\${onnxtr.api-url:http://localhost:9005}") private val apiUrl: String,
        @Value("\${onnxtr.timeout:60s}") private val timeout: String
) {
    private val logger = LoggerFactory.getLogger(OnnxtrOcrProvider::class.java)
    private val webClient = WebClient.builder().baseUrl(apiUrl).build()

    private val timeoutDuration: Duration
        get() = Duration.parse("PT${timeout.uppercase()}")

    /** 이미지에서 텍스트 추출 */
    fun extractText(imageBytes: ByteArray): OcrRawResult {
        return try {
            logger.info("Starting OnnxTR OCR extraction...")

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
                            .bodyToMono(OnnxtrOcrResponse::class.java)
                            .block(timeoutDuration)

            if (response?.success == true) {
                logger.info("OnnxTR OCR completed, extracted ${response.line_count} lines")
                OcrRawResult(
                        success = true,
                        fullText = response.text ?: "",
                        lines = response.lines?.map { it.text } ?: emptyList(),
                        confidence = response.lines?.map { it.confidence }?.average() ?: 0.0,
                        engine = "onnxtr"
                )
            } else {
                logger.warn("OnnxTR OCR failed: ${response?.error}")
                OcrRawResult(
                        success = false,
                        errorMessage = response?.error ?: "Unknown error",
                        engine = "onnxtr"
                )
            }
        } catch (e: Exception) {
            logger.error("OnnxTR OCR extraction failed: ${e.message}", e)
            OcrRawResult(
                    success = false,
                    errorMessage = e.message ?: "Unknown error",
                    engine = "onnxtr"
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
            logger.warn("OnnxTR health check failed: ${e.message}")
            false
        }
    }
}

data class OnnxtrOcrResponse(
        val success: Boolean,
        val text: String?,
        val lines: List<OnnxtrLine>?,
        val line_count: Int?,
        val korean_ratio: Double?,
        val error: String?
)

data class OnnxtrLine(val text: String, val confidence: Double)
