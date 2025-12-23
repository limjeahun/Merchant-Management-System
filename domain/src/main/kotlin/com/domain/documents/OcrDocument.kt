package com.domain.documents

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed

/**
 * "ocr_results"라는 키 prefix 로 저장, TTL 600초(10분)
 */
@RedisHash(value = "ocr_results", timeToLive = 600)
data class OcrDocument(
    @Id // Redis Key로 사용 (requestId)
    val requestId: String,
    @Indexed // 보조 인덱스가 필요한 경우 사용
    val status: String,         // PROCESSING, COMPLETED, FAILED
    val rawJson: String? = null,       // Gemini가 반환한 Raw JSON
    val parsedData: Map<String, String> = emptyMap()
)
