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
public class EFilesWebClientConfigurerTest {

    @MockitoBean
    private EFilesAuthenticator eFilesAuthenticator;

    @MockitoBean
    private EFilesParameters eFilesParameters;

    @MockitoBean
    private NetworkConfigurer networkConfigurer;

    private EFilesClientConfigurer eFilesClientConfigurer;

    private RetryPolicy retryPolicyMock;

    private final RetryPolicy retryPolicy = new RetryPolicy();
    private final CacheProvider cacheProvider = new CacheProvider();

    private static final String NEW_ACCESS_TOKEN = "new access token";
    private String baseUrl;

    public static MockWebServer mockBackEnd;

    @BeforeEach
    void init() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        ReflectionTestUtils.setField(eFilesAuthenticator, "connectionParameters", eFilesParameters);
    }

    @PostConstruct
    void postConstruct() {
        retryPolicyMock = spy(retryPolicy);
        eFilesClientConfigurer = new EFilesClientConfigurer(eFilesParameters, eFilesAuthenticator, networkConfigurer, retryPolicyMock, cacheProvider);
    }



    @Test
    void whenARequestIsFired_checkToken() throws Exception {
        //ARRANGE
        Token tokenFrom60MinutesAgo = new Token(
                "FAKE_TOKEN",
                "type",
                "scope",
                3600,
                LocalDateTime.now().minusMinutes(57),
                "appName"
        );

        Token newToken = new Token(
                NEW_ACCESS_TOKEN,
                "type",
                "scope",
                3600,
                LocalDateTime.now(),
                "appName"
        );

        when(eFilesParameters.getBaseUrl()).thenReturn(baseUrl);
        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());
        cacheProvider.setToken(CacheProvider.COMPONENT.INTERFACE, tokenFrom60MinutesAgo);

        when(eFilesAuthenticator.authenticate()).thenAnswer(invocationOnMock -> {
            cacheProvider.setToken(CacheProvider.COMPONENT.INTERFACE, newToken);
            return Mono.just(newToken);
        });
        mockBackEnd.enqueue(new MockResponse().setResponseCode(200));
        ReflectionTestUtils.setField(eFilesClientConfigurer, "component", CacheProvider.COMPONENT.INTERFACE);

        WebClient webClient = eFilesClientConfigurer.buildWebClient();
        webClient.get().exchangeToMono(clientResponse -> Mono.just("")).block();


        verify(retryPolicyMock, times(1))
                .checkTokenOrElseRenew(any(), eq(eFilesAuthenticator), eq(cacheProvider), eq(CacheProvider.COMPONENT.INTERFACE));
        Assertions.assertEquals(cacheProvider.getToken(CacheProvider.COMPONENT.INTERFACE).getAccessToken(), NEW_ACCESS_TOKEN);
    }
}
