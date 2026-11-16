package de.niko

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class ChatController(builder: ChatClient.Builder, vectorStore: VectorStore) {
    private var chatClient: ChatClient = builder
        .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
        .build()

    @PostMapping("/")
    fun chat(@RequestParam prompt: String): String? {
        return chatClient.prompt()
            .user(prompt)
            .call()
            .content()
    }
}
