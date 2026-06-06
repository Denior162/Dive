package com.deep.dive.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val status: String,
    val code: Int,
    val message: String,
    val request_id: String? = null
)