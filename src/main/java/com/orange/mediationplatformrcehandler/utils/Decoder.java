package com.MyProject.mediationplatformrcehandler.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Decoder {

    public static Mono<List<EFile>> decodeEFilesResponse(String responseAsString) {

        List<EFile> responseBodyList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());

        try {
            JsonNode root = objectMapper.readTree(responseAsString);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    EFile file = objectMapper.treeToValue(node, EFile.class);
                    responseBodyList.add(file);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Cannot deserialize query", e);
        }

        return Mono.just(responseBodyList);
    }
}
