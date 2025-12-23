package com.provider.gemini.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class InlineData(
    @JsonProperty("mime_type") val mimeType: String,
    val data: String // Base64 Encoded String
)
