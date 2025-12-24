package com.application.port.`in`

import com.application.port.out.OcrResult
import com.domain.model.Merchant

interface OcrUseCase {
    /** 사업자등록증 OCR 요청 제출 */
    fun submitBusinessLicenseOcr(ocrCommand: OcrCommand): String
    
    /** OCR 결과 조회 */
    fun getOcrResult(requestId: String): OcrResult
    
    /** OCR 결과 확인 및 저장 */
    fun confirmAndSave(requestId: String): Merchant
}