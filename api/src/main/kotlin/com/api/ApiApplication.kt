package com.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * scanBasePackages 를 통해 다른 모듈의 패키지를 포함.
 */
@SpringBootApplication(
    scanBasePackages = [
        "com.api",
        "com.application",
        "com.infrastructure",
        "com.common"
    ]
)
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
