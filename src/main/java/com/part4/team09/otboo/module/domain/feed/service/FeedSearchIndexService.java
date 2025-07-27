package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.domain.feed.document.FeedSearchDocument;
import com.part4.team09.otboo.module.domain.feed.entity.Feed;
import com.part4.team09.otboo.module.domain.feed.mapper.FeedSearchDocumentAssembler;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedSearchIndexService {

    private final OpenSearchClient openSearchClient;
    private final FeedSearchDocumentAssembler assembler;

    private static final String INDEX = "feeds";

    public void index(Feed feed) {
        FeedSearchDocument document = assembler.toDocument(feed);
        try {
            openSearchClient.index(i -> i
                    .index(INDEX)
                    .id(document.getId().toString())
                    .document(document)
            );
        } catch (IOException e) {
            throw new RuntimeException("Indexing failed", e);
        }
    }

    public void update(Feed feed) {
        index(feed); // 동일 ID면 update
    }

    public void delete(UUID id) {
        try {
            openSearchClient.delete(d -> d
                    .index(INDEX)
                    .id(id.toString())
            );
        } catch (IOException e) {
            throw new RuntimeException("Delete failed", e);
        }
    }
}