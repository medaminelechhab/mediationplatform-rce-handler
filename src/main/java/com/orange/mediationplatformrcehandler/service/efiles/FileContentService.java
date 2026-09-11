package com.MyProject.mediationplatformrcehandler.service.efiles;

import com.MyProject.mediationplatform.common.error.ApiCallException;
import com.MyProject.mediationplatform.common.model.Token;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFilesErrorResponse;
import com.MyProject.mediationplatformrcehandler.network.JWTAuthenticator;
import com.MyProject.mediationplatformrcehandler.network.JWTClientConfigurer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

@Service
@Slf4j
public class FileContentService {

    private JWTClientConfigurer clientConfiguration;

    private JWTAuthenticator jwtAuthenticator;

    @Value("${clinksplatform.tmp-storage-dir}")
    private String temporaryDir;

    public FileContentService(JWTClientConfigurer clientConfiguration, JWTAuthenticator jwtAuthenticator) {
        this.clientConfiguration = clientConfiguration;
        this.jwtAuthenticator = jwtAuthenticator;
    }

    public Mono<File> getContentFile(String operationID){
        log.info("Attempting to get content files RCE");

        return jwtAuthenticator.authenticate().flatMap(token -> {
            log.info("jwt token : {}", token);
            return clientConfiguration.buildWebClient()
                    .get()
                    .uri(clientConfiguration.buildUri(String.format("/files/%s/content", operationID), Collections.emptyMap()))
                    .headers(getAuthJWTHeaders(token))
                    .retrieve()
                    .bodyToMono(ByteArrayResource.class)
                    .flatMap(content -> {
                        byte[] byteContent = content.getByteArray();
                        File file = new File(temporaryDir.concat("data-rce.tgz"));
                        
                        if(byteContent.length==0){
                            throw new IllegalStateException("File " + operationID + " is empty");
                        }

                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(byteContent);
                            return Mono.just(file);
                        } catch (IOException e) {
                            return Mono.error(e);
                        }
                    })
                    .retryWhen(Retry.backoff(5, Duration.ofSeconds(10))
                        .filter(throwable -> throwable instanceof HttpServerErrorException || throwable instanceof WebClientRequestException))
                    .doOnError(ApiCallException.class, e -> {
                        log.error("Log Exception {} : " + e);
                        Mono.just(EFilesErrorResponse.mapError(e));
                    });

        });

    }

    private Consumer<HttpHeaders> getAuthJWTHeaders(Token token) {
        return httpHeaders -> {
            httpHeaders.setBearerAuth(token.getAccessToken());
            log.info("header token: {}", token.getAccessToken());
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString());
            httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
            httpHeaders.add(HttpHeaders.CACHE_CONTROL, "no-cache");
        };
    }
}
