package com.application.port.out

import com.common.ocr.BusinessLicenseData

/** LLM 기반 텍스트 처리 Port Gemma3를 통한 OCR 오류 보정 및 필드 파싱을 담당 사업자 유형(개인/법인)에 따라 다른 프롬프트를 사용 */
interface TextProcessorPort {

    /**
     * OCR 오류를 보정하고 필드를 파싱합니다. (사업자 유형 기반)
     * @param text OCR 추출 텍스트
     * @param businessType 사업자 유형 (INDIVIDUAL or CORPORATE)
     * @param imageBytes 원본 이미지 (멀티모달 검증용, nullable)
     * @return 파싱된 사업자등록증 데이터
     */
    fun correctAndParse(
            text: String,
            businessType: String,
            imageBytes: ByteArray?
    ): BusinessLicenseData

    /**
     * 앙상블 OCR 결과를 교차검증하여 필드를 파싱합니다.
     * @param ensembleResults 3개 OCR 엔진 결과 포맷 문자열
     * @param businessType 사업자 유형 (INDIVIDUAL or CORPORATE)
     * @return 파싱된 사업자등록증 데이터
     */
    fun crossValidateAndParse(ensembleResults: String, businessType: String): BusinessLicenseData
}
