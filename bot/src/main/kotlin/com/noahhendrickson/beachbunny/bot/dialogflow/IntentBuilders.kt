package com.noahhendrickson.beachbunny.bot.dialogflow

import com.google.cloud.dialogflow.v2.*

suspend fun textInput(builder: suspend TextInput.Builder.() -> Unit = {}): TextInput {
    return TextInput.newBuilder().apply { builder() }.build()
}

suspend fun queryInput(builder: suspend QueryInput.Builder.() -> Unit = {}): QueryInput {
    return QueryInput.newBuilder().apply { builder() }.build()
}

suspend fun queryParameters(builder: suspend QueryParameters.Builder.() -> Unit = {}): QueryParameters {
    return QueryParameters.newBuilder().apply { builder() }.build()
}

suspend fun requestIntent(builder: suspend DetectIntentRequest.Builder.() -> Unit = {}): DetectIntentRequest {
    return DetectIntentRequest.newBuilder().apply { builder() }.build()
}
