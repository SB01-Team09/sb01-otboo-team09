package com.part4.team09.otboo.module.domain.feed.service;

import com.part4.team09.otboo.module.common.enums.SortDirection;
import com.part4.team09.otboo.module.domain.feed.document.FeedSearchDocument;
import com.part4.team09.otboo.module.domain.feed.dto.request.FeedListRequest;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedSearchService {

    private final OpenSearchClient openSearchClient;
    private static final String INDEX = "feeds";

    public List<FeedSearchDocument> searchWithFilters(FeedListRequest request) {
        try {
            List<SortOptions> sortOptions = new ArrayList<>();

            SortOrder sortOrder = request.sortDirection() == SortDirection.DESCENDING
                    ? SortOrder.Desc
                    : SortOrder.Asc;

            // 기본 정렬 필드
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field(request.sortBy()).order(sortOrder))));

            // tie-breaker
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("id").order(sortOrder))));

            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index("feeds")
                    .size(request.limit() + 1) // 페이징 위해 +1
                    .sort(sortOptions);

            // 필터링 조건
            searchBuilder.query(q -> q.bool(b -> {
                if (request.keywordLike() != null && !request.keywordLike().isBlank()) {
                    b.must(m -> m.multiMatch(mm -> mm
                            .fields("content", "weather.skyStatus", "weather.precipitationType")
                            .query(request.keywordLike())));
                }

                if (request.skyStatusEqual() != null) {
                    b.filter(f -> f.term(t -> t.field("weather.skyStatus").value(FieldValue.of(request.skyStatusEqual().name()))));
                }

                if (request.precipitationTypeEqual() != null) {
                    b.filter(f -> f.term(t -> t.field("weather.precipitationType").value(FieldValue.of(request.precipitationTypeEqual().name()))));
                }

                return b;
            }));

            // TODO: OpenSearch 페이징 처리 - search_after 적용


            SearchResponse<FeedSearchDocument> response = openSearchClient.search(
                    searchBuilder.build(),
                    FeedSearchDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Search failed", e);
        }
    }
}