package com.MyProject.mediationplatformrcehandler.network;

import com.MyProject.mediationplatform.common.model.Token;
import com.MyProject.mediationplatform.common.network.CacheProvider;
import com.MyProject.mediationplatform.common.network.NetworkConfigurer;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;


@ExtendWith(SpringExtension.class)
 class JWTWebClientConfigurerTest {

    @MockitoBean
    private JWTAuthenticator jwtAuthenticator;

    @MockitoBean
    private JWTParameters jwtParameters;

    @MockitoBean
    private NetworkConfigurer networkConfigurer;

    @MockitoBean
    private EFilesParameters eFilesParameters;

    private JWTClientConfigurer jwtClientConfigurer;

    @MockitoBean
    private EFilesAuthenticator eFilesAuthenticator;



    private RetryPolicy retryPolicyMock;

    private final RetryPolicy retryPolicy = new RetryPolicy();
    private final CacheProvider cacheProvider = new CacheProvider();

    private static final String NEW_ACCESS_TOKEN = "new access token";

    private static final String NEW_ACCESS_TOKEN_JWT = "new access token";
    private String baseUrl;

    public static MockWebServer mockBackEnd;


    @BeforeEach
    void init() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        ReflectionTestUtils.setField(jwtAuthenticator, "connectionParameters", jwtParameters);
    }

    @PostConstruct
    void postConstruct() {
        retryPolicyMock = spy(retryPolicy);
        jwtClientConfigurer = new JWTClientConfigurer(jwtParameters, jwtAuthenticator, networkConfigurer, retryPolicyMock, cacheProvider);
    }

    @Test
    void whenARequest_getToken() throws Exception {
        //ARRANGE

        Token newODIToken = new Token(
                NEW_ACCESS_TOKEN,
                "type",
                "scope",
                3600,
                LocalDateTime.now(),
                "appName"
        );


        Token newJWTToken = new Token(
                NEW_ACCESS_TOKEN_JWT,
                "type",
                "scope",
                1800,
                LocalDateTime.now()
                ,"appName"
        );


        when(eFilesParameters.getBaseUrl()).thenReturn(baseUrl);
        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());
        cacheProvider.setToken(CacheProvider.COMPONENT.INTERFACE, newODIToken);


        when(jwtParameters.getBaseUrl()).thenReturn(baseUrl);
        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());
        cacheProvider.setToken(CacheProvider.COMPONENT.EFILES, newODIToken);
        when(jwtAuthenticator.authenticate()).thenReturn(Mono.just(newJWTToken));

        mockBackEnd.enqueue(new MockResponse().setResponseCode(200));
        ReflectionTestUtils.setField(jwtClientConfigurer, "component", CacheProvider.COMPONENT.EFILES);

        WebClient webClient = jwtClientConfigurer.buildWebClient();
        webClient.get().exchangeToMono(clientResponse -> Mono.just("")).block();


        verify(retryPolicyMock, times(1))
                .checkTokenOrElseRenew(any(), eq(jwtAuthenticator), eq(cacheProvider), eq(CacheProvider.COMPONENT.EFILES));
        Assertions.assertEquals(cacheProvider.getToken(CacheProvider.COMPONENT.INTERFACE).getAccessToken(), newODIToken.getAccessToken());
        Assertions.assertEquals(cacheProvider.getToken(CacheProvider.COMPONENT.EFILES).getAccessToken(), newJWTToken.getAccessToken());
    }

}
