package com.noahhendrickson.beachbunny.bot.dialogflow

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.QueryResult
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.SessionsSettings
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.InputStream
import java.util.*

suspend fun createDialogflowAssistant(builder: suspend DialogflowAssistantBuilder.() -> Unit): DialogflowAssistant {
    val dialogflowAssistantBuilder = DialogflowAssistantBuilder().apply { builder() }
    return DialogflowAssistant(dialogflowAssistantBuilder.client)
}

suspend fun DialogflowAssistant.detectIntentTextAsync(content: String): Deferred<QueryResult?>? {
    if (client == null || !client.isNotNull()) return null

    return coroutineScope {
        async {
            val request = requestIntent {
                session = client.toString()

                queryInput = queryInput {
                    text = textInput {
                        text = content
                        languageCode = "en"
                    }
                }

                queryParams = queryParameters()
            }

            client.queryResult(request)
        }
    }
}

data class DialogflowAssistant(val client: DialogflowClient?)

class DialogflowAssistantBuilder {

    private var _credentials: GoogleCredentials? = null
    private var _projectId: String? = null

    private val _sessionsSettings: SessionsSettings?
        get() {
            return try {
                _credentials?.let {
                    SessionsSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(it))
                        .build()
                }
            } catch (e: Exception) {
                null
            }
        }

    val client: DialogflowClient?
        get() {
            return try {
                if (_sessionsSettings != null && _projectId != null)
                    DialogflowClient(
                        SessionsClient.create(_sessionsSettings),
                        SessionName.of(_projectId, UUID.randomUUID().toString())
                    )
                else null
            } catch (e: Exception) {
                null
            }
        }

    fun credentials(builder: () -> InputStream?) {
        val inputStream = builder()

        if (inputStream == null) _credentials = GoogleCredentials.getApplicationDefault()
        else inputStream.apply {
            try {
                _credentials = GoogleCredentials.fromStream(this)
                _projectId = (_credentials as ServiceAccountCredentials).projectId
            } catch (e: Exception) {
            }
        }
    }
}
