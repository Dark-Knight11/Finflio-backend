package com.finflio.utils.responses

import com.finflio.data.models.Stats
import kotlinx.serialization.Serializable

@Serializable
class StatsResponse(
    override val status: Int,
    override val message: String,
    val stats: Stats? = null
) : BaseResponse {

    companion object {

        fun failed(message: String) = StatsResponse(
            State.FAILED.value,
            message
        )

        fun success(stats: Stats, message: String) = StatsResponse(
            State.SUCCESS.value,
            message,
            stats
        )
    }
}