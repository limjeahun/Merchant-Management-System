package com.domain.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "merchant")
class Merchant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val requestId: String,          // OCR 요청 ID와 연결
    @Column(nullable = false)
    val merchantName: String,       // 법인명(단체명) 또는 상호 (예: 주식회사 마이제이디나눔기업)
    @Column(nullable = false, unique = true)
    val businessNumber: String,     // 등록번호 (예: 476-81-00434)
    @Column(nullable = false)
    val representativeName: String, // 대표자 (예: 이임순)
    @Column(nullable = false)
    val address: String,            // 사업장 소재지 (예: 경기도 남양주시...)
    val businessType: String? = null,      // 업태 (예: 도소매, 제조)
    val businessItem: String? = null,      // 종목 (예: 단체용품, 전자상거래)
    val openingDate: LocalDate? = null,    // 개업연월일 (예: 2015-12-01)
    // 시스템 관리 필드
    val verified: Boolean = false,   // 검증 여부
    val registeredAt: LocalDate = LocalDate.now()
) {

}