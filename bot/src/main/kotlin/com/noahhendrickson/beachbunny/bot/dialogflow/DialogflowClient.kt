package com.noahhendrickson.beachbunny.bot.dialogflow

import com.google.cloud.dialogflow.v2.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DialogflowClient(
    val sessionsClient: SessionsClient?,
    val sessionName: SessionName?
) {

    suspend fun queryResult(request: DetectIntentRequest): QueryResult? {
        return detectIntent(request)?.queryResult
    }

    private suspend fun detectIntent(request: DetectIntentRequest): DetectIntentResponse? {
        return coroutineScope {
            async {
                sessionsClient?.detectIntent(request)
            }
        }.await()
    }

    fun isNotNull() = sessionsClient != null && sessionName != null

    override fun toString() = sessionName?.toString() ?: ""
}
