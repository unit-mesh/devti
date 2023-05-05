package cc.unitmesh.core.prompter

import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.time.Duration

class OpenAiPrompter(val openAiKey: String, val openAiProxy: String) {
    private val openAiVersion: String = "gpt-3.5-turbo"
    private var service: OpenAiService
    private val timeout = Duration.ofSeconds(600)

    init {
        if (openAiProxy.trim().isEmpty()) {
            service = OpenAiService(openAiKey, timeout)
        } else {
            val mapper = OpenAiService.defaultObjectMapper()
            val client = OpenAiService.defaultClient(openAiKey, timeout)

            val retrofit = Retrofit.Builder()
                .baseUrl(openAiProxy)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

            val api = retrofit.create(OpenAiApi::class.java)
            service = OpenAiService(api)
        }
    }

    fun prompt(instruction: String): String {
        val messages: MutableList<ChatMessage> = ArrayList()
        val systemMessage = ChatMessage(ChatMessageRole.USER.value(), instruction)
        messages.add(systemMessage)

        val completionRequest = ChatCompletionRequest.builder()
            .model(openAiVersion)
            .messages(messages)
            .build()

        val completion = service.createChatCompletion(completionRequest)

        return completion
            .choices[0].message.content
    }
}
