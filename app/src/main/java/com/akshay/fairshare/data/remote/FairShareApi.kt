package com.akshay.fairshare.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class ShareDto(
    @SerialName("member_id") val memberId: String,
    @SerialName("amount_cents") val amountCents: Long,
)

@Serializable
data class ExpenseDto(
    val id: String,
    @SerialName("group_id") val groupId: String,
    val description: String,
    @SerialName("total_cents") val totalCents: Long,
    @SerialName("paid_by") val paidBy: String,
    val shares: List<ShareDto>,
    @SerialName("created_at") val createdAt: Long,
)

@Serializable
data class MemberDto(val id: String, val name: String)

@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val members: List<MemberDto>,
    @SerialName("updated_at") val updatedAt: Long,
)

interface FairShareApi {

    @GET("groups/{groupId}")
    suspend fun group(@Path("groupId") groupId: String): GroupDto

    @GET("groups/{groupId}/expenses")
    suspend fun expenses(@Path("groupId") groupId: String): List<ExpenseDto>

    @POST("groups/{groupId}/expenses")
    suspend fun pushExpenses(
        @Path("groupId") groupId: String,
        @Body expenses: List<ExpenseDto>,
    ): List<ExpenseDto>
}
