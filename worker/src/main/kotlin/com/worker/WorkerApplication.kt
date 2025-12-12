package com.worker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * scanBasePackages 를 통해 다른 모듈의 패키지를 포함.
 */
@SpringBootApplication(
    scanBasePackages = [
        "com.worker",
        "com.application",
        "com.infrastructure",
        "com.provider",
        "com.common"
    ]
)
class WorkerApplication

fun main(args: Array<String>) {
    runApplication<WorkerApplication>(*args)
}
