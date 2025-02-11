package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("This is a program that let's you ask question about a set of PDF files.");
        System.out.println("For exit type 'exit' or 'quit' (all of them without quotes).");
        System.out.println();

        if (args.length != 2) {
            System.err.println("Error: wrong arguments count.");
            System.err.println("Usage: program api-key path-for-file-or-directory");
            System.exit(1);
        }

        String apiKey = args[0];
        Path documentOrDirectoryPath = Path.of(args[1]);

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor
                .builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        if (Files.isDirectory(documentOrDirectoryPath)) {
            for (Path filePath : Files.list(documentOrDirectoryPath).toList()) {
                Document document = FileSystemDocumentLoader.loadDocument(filePath.toAbsolutePath(), new ApachePdfBoxDocumentParser());
                ingestor.ingest(document);
            }
        } else {
            // Oops, code duplication :)
            Document document = FileSystemDocumentLoader.loadDocument(documentOrDirectoryPath.toAbsolutePath(), new ApachePdfBoxDocumentParser());
            ingestor.ingest(document);
        }

        OpenAiChatModel model = OpenAiChatModel
                .builder()
                .modelName("gpt-4o-mini")
                .apiKey(apiKey)
                .build();

        ContentRetriever retriever = EmbeddingStoreContentRetriever
                .builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build();

        ConversationalRetrievalChain chain = ConversationalRetrievalChain
                .builder()
                .chatLanguageModel(model)
                .contentRetriever(retriever)
                .build();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("User: ");
            String userMessage = scanner.nextLine();

            if (userMessage.startsWith("quit") || userMessage.startsWith("exit")) {
                break;
            }

            String answer = chain.execute(userMessage);
            System.out.println("AI: " + answer);
        }
    }
}