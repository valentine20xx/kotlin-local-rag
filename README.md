# Local RAG with Kotlin

A fully local Retrieval-Augmented Generation (RAG) demo built with Kotlin, Spring Boot and Spring AI.
Nothing leaves your machine: the LLM and embeddings run in Ollama, vectors are stored in PostgreSQL (pgvector).

## How it works

1. **Ingestion** (`IngestionService`): on startup, every PDF in `src/main/resources/docs/` is read paragraph by
   paragraph (`ParagraphPdfDocumentReader`), split into token chunks (`TokenTextSplitter`), embedded with
   `nomic-embed-text` and stored in the `vector_store` table.
2. **Query** (`ChatController`): `POST /` takes a `prompt` parameter. `QuestionAnswerAdvisor` embeds the question,
   retrieves similar chunks from pgvector and adds them to the prompt sent to the chat model (`gemma4:12b`).
3. The model's answer is returned as plain text.

Note: ingestion runs on every start, so documents are re-inserted each time. Clear the table (see below) to avoid duplicates.

## Stack

| Part            | Technology                                   |
|-----------------|----------------------------------------------|
| Language        | Kotlin 2.4, JDK 25                           |
| Framework       | Spring Boot 4.1, Spring AI 2.0               |
| Chat model      | Ollama `gemma4:12b`                          |
| Embedding model | Ollama `nomic-embed-text` (768 dimensions)   |
| Vector store    | PostgreSQL 16 + pgvector (via `compose.yaml`) |

## Prerequisites

- JDK 25
- Docker (the DB container is started automatically by `spring-boot-docker-compose`)
- Ollama running on `http://localhost:11434`

## Setup & run

```bash
# Pull models
ollama pull gemma4:12b
ollama pull nomic-embed-text

# Start Gemma 4 in Ollama
ollama run gemma4:12b

# Start the app (also starts the pgvector container from compose.yaml)
./gradlew bootRun
```

Configuration lives in `src/main/resources/application.yml`; DB settings are in `compose.yaml`.

## Test request

```bash
curl -X POST http://localhost:8080/ -vks -d "prompt=How did the Federal Reserve\'s recent interest rate cut impact various asset classes according to the analysis"
```

## Useful commands

```bash
# Run unit tests
./gradlew test

# Start only the database
docker compose up -d

# List databases in the container
docker exec kotlin-local-rag-pgvector-1 psql -U user -d postgres -c '\l'

# Count stored chunks
docker exec kotlin-local-rag-pgvector-1 psql -U user -d kotlin-local-rag -c "SELECT count(*) FROM vector_store;"

# Clear database
docker exec kotlin-local-rag-pgvector-1 psql -U user -d kotlin-local-rag -c "DROP TABLE vector_store;"
```

## Troubleshooting

**`FATAL: database "kotlin-local-rag" does not exist`**
Postgres only creates `POSTGRES_DB` when a data volume is first initialized. If the volume was created by another
project, create the database manually:

```bash
docker exec kotlin-local-rag-pgvector-1 psql -U user -d postgres -c 'CREATE DATABASE "kotlin-local-rag"'
docker exec kotlin-local-rag-pgvector-1 psql -U user -d kotlin-local-rag -c 'CREATE EXTENSION IF NOT EXISTS vector'
```

**Ollama returns 501 on embeddings**
The embedding model must be a dedicated embedding model (e.g. `nomic-embed-text`), not a chat model like `gemma4:12b`.
