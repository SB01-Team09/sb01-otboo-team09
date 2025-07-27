package com.part4.team09.otboo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
public class OpenSearchDevConfig {

    @Value("${opensearch.host}")
    private String host;

    @Value("${opensearch.port}")
    private int port;

    @Bean
    public OpenSearchClient openSearchClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(host, port, "http")
        ).build();

        // ObjectMapper에 JavaTimeModule 등록
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // 날짜를 배열이나 숫자가 아니라 문자열로 직렬화
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(mapper);
        RestClientTransport transport = new RestClientTransport(restClient, jsonpMapper);

        return new OpenSearchClient(transport);

    }
}