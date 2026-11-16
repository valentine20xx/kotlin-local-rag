package de.niko

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader
import org.springframework.ai.transformer.splitter.TextSplitter
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class IngestionService(private val vectorStore: VectorStore) : CommandLineRunner {
    @Value("classpath:/docs/*")
    private val resources: Array<Resource> = emptyArray()

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        val textSplitter: TextSplitter = TokenTextSplitter.builder().build()

        val documents = resources.flatMap { resource ->
            val docs = ParagraphPdfDocumentReader(resource).get()

            textSplitter.apply(docs)
        }

        vectorStore.accept(documents)

        log.info("VectorStore Loaded with data!")
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(IngestionService::class.java)
    }
}
