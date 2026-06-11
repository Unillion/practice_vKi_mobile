package ci.nsu.mobile.main.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    @SerialName("groupId")
    val groupId: Int,
    @SerialName("groupName")
    val groupName: String
)