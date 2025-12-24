package com.infrastructure.kafka

import com.application.port.out.OcrEventPort
import com.common.event.OcrRequestEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaOcrProducer(
    private val kafkaTemplate: KafkaTemplate<String, OcrRequestEvent>
): OcrEventPort {
    override fun publishBusinessLicenseOcrRequest(event: OcrRequestEvent) {
        kafkaTemplate.send("mms.ocr.business-license.request", event.requestId, event)
    }

}