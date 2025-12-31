package com.common.ocr

/** OCR 추출 결과를 담는 DTO */
data class OcrRawResult(
        /** 전체 추출 텍스트 (라인별 연결) */
        val fullText: String = "",

        /** 개별 텍스트 라인들 */
        val lines: List<String> = emptyList(),

        /** 전체 처리 성공 여부 */
        val success: Boolean = true,

        /** 에러 메시지 (실패 시) */
        val errorMessage: String? = null,

        /** 평균 신뢰도 점수 (0.0 ~ 1.0) */
        val confidence: Double = 0.0,

        /** 사용된 OCR 엔진 이름 */
        val engine: String = "unknown"
) {
    companion object {
        fun empty() = OcrRawResult(fullText = "", lines = emptyList(), success = true)

        fun error(message: String, engine: String = "unknown") =
                OcrRawResult(
                        fullText = "",
                        lines = emptyList(),
                        success = false,
                        errorMessage = message,
                        engine = engine
                )
    }
}

/** OCR로 추출된 개별 텍스트 라인 (상세 정보 포함) */
data class OcrLine(
        /** 텍스트 내용 */
        val text: String,

        /** 신뢰도 점수 (0.0 ~ 1.0) */
        val confidence: Float,

        /** 바운딩 박스 좌표 [x1, y1, x2, y2, x3, y3, x4, y4] */
        val boundingBox: List<Float>? = null
)
