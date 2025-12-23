package com.infrastructure.persistence.redis.repository

import com.domain.documents.OcrDocument
import org.springframework.data.repository.CrudRepository

interface OcrRedisRepository: CrudRepository<OcrDocument, String> {

}