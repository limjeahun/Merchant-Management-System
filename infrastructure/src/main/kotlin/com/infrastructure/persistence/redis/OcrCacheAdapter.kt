package com.infrastructure.persistence.redis

import com.domain.documents.OcrDocument
import com.domain.repository.OcrCacheRepository
import com.infrastructure.persistence.redis.repository.OcrRedisRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class OcrCacheAdapter(
    private val ocrRedisRepository: OcrRedisRepository
): OcrCacheRepository {
    /**
     * Redis OCR 정보 저장
     */
    override fun save(result: OcrDocument) {
        val document = OcrDocument(
            requestId = result.requestId,
            status    = result.status,
            rawJson   = result.rawJson,
            message   = null
        )
        ocrRedisRepository.save(document)
    }

    /**
     * requestId로 OCR 정보 조회
     */
    override fun findByRequestId(requestId: String): OcrDocument {
        val entityOpt = ocrRedisRepository.findById(requestId)
        return entityOpt.map { document ->
            OcrResult(
                requestId = document.requestId,
                status = document.status,
                rawText = document.rawJson,
                parsedData = emptyMap() // 필요 시 rawJson 파싱하여 맵핑
            )
        }.orElse(null)
    }
}