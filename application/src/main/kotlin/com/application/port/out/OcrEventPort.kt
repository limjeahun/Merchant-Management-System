package com.application.port.out

import com.common.event.OcrRequestEvent

interface OcrEventPort {
    /** 사업자등록증 OCR 요청 이벤트 발행 */
    fun publishBusinessLicenseOcrRequest(event: OcrRequestEvent)
}