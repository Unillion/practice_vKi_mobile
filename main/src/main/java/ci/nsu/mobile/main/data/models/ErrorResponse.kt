package ci.nsu.mobile.main.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
    val statusCode: Int
)