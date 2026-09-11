package com.MyProject.mediationplatformrcehandler.service.efiles;

import com.MyProject.mediationplatform.common.error.ApiCallException;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFilesErrorResponse;
import com.MyProject.mediationplatformrcehandler.network.EFilesClientConfigurer;
import com.MyProject.mediationplatformrcehandler.utils.Decoder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.JobParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EFilesService {

    private final EFilesClientConfigurer clientConfiguration;

    public EFilesService(EFilesClientConfigurer clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    public Mono<List<EFile>> getFiles(Map<String, Object> parameters, Map<String, String> customParams) {
        
        Map<String, Object> efilesParams = new HashMap<>();
        //Add custom params
        if (customParams!=null && !customParams.isEmpty()){
            efilesParams.putAll(customParams);
            log.info("Used custom params {}", customParams);
            customParams.clear();
        } else {
            efilesParams.putAll(parameters);
            log.info("Used config params {}", parameters);
        }
        

        log.info("Attempting to get files RCE filtered by {}", efilesParams);

        return clientConfiguration.buildWebClient()
                .get()
                .uri(clientConfiguration.buildUri("/files", efilesParams))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(Decoder::decodeEFilesResponse)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(10))
                    .filter(throwable -> throwable instanceof HttpServerErrorException || throwable instanceof WebClientRequestException))
                .doOnError(ApiCallException.class, e -> {
                    log.error("An error occured {} : ", e.getMessage());
                    Mono.just(EFilesErrorResponse.mapError(e));
                });
    }
}
