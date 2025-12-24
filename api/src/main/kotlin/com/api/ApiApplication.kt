package com.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

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
@EnableJpaRepositories(basePackages = ["com.infrastructure.persistence.jpa"])
@EntityScan(basePackages = ["com.domain.model"])
@EnableRedisRepositories(basePackages = ["com.infrastructure.persistence.redis.repository"])
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
