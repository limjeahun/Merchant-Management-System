package com.provider.gemini.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Part(
    val text: String? = null,
    @JsonProperty("inline_data") val inlineData: InlineData? = null
)
