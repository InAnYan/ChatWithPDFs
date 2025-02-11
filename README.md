# ChatWithPDFs

![Screenshot](screenshot.jpg)

Prototype app for question answering over a library of PDF files.

Created to demonstrate the capability to contribute to Google Summer of Code 2024 at JabRef.

## Features

- It supports QA either for one PDF file or several PDF file in one directory.
- It uses `AllMiniLmL6V2EmbeddingModel` and `OpenAiChatModel`.

## How to Run this Project

```sh
program <API_KEY> <PATH>
```

Where:

- `API_KEY`: OpenAI API key/token.
- `PATH`: path to one PDF file or a folder containing several PDF files.

## How this Project is Implemented

Tech stack:

- OpenAI API
- `langchain4j`
- Apache PDFBox

The program contains of 2 algorithms:

1. Indexing
   1. PDF files are loaded into memory.
   2. Text is extracted from PDFs.
   3. This text is then splitted into smaller chunks.
   4. An embedding model generates a vector for each chunk.
   5. Vector-chunk pair is stored in a vector database.
2. Chatting
   1. User message is converted into a vector.
   2. Vector database searches for chunks with a similar (close) vector.
   3. Found chunks are appended to use user message.
   4. Final message is sent to LLM.
