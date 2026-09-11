package com.MyProject.mediationplatformrcehandler.network;

import com.MyProject.mediationplatform.common.model.Token;
import com.MyProject.mediationplatform.common.network.CacheProvider;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

import static com.MyProject.mediationplatform.common.network.CacheProvider.COMPONENT.INTERFACE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
class EFilesRetryPolicyTest {

    static final ClientRequest CLIENT_REQUEST = ClientRequest.create(HttpMethod.POST, URI.create("dummy")).build();
    RetryPolicy retryPolicy = new RetryPolicy();

    @MockitoBean
    private EFilesAuthenticator eFilesAuthenticator;

    @MockitoBean
    private EFilesParameters eFilesParameters;

    @MockitoBean
    private CacheProvider cacheProvider;
    @BeforeEach
    void init() throws IOException {
        ReflectionTestUtils.setField(eFilesAuthenticator, "connectionParameters", eFilesParameters);

    }

    @Test
    @DisplayName("If token is expired, throw token then authenticate")
    void renew_whenTokenExpired() {

        Token tokenFrom60MinutesAgo = new Token(
                "FAKE_TOKEN",
                "type",
                "scope",
                3600,
                LocalDateTime.now().minusMinutes(60),"appName"
        );

        when(cacheProvider.getToken(CacheProvider.COMPONENT.INTERFACE)).thenReturn(tokenFrom60MinutesAgo);

        when(eFilesAuthenticator.authenticate()).thenReturn(Mono.just(tokenFrom60MinutesAgo));

        doNothing().when(cacheProvider).throwToken(CacheProvider.COMPONENT.INTERFACE);
        doNothing().when(eFilesParameters).setAuthorisation(any(), any());

        retryPolicy.checkTokenOrElseRenew(CLIENT_REQUEST, eFilesAuthenticator, cacheProvider, CacheProvider.COMPONENT.INTERFACE).block();

        verify(cacheProvider, times(1)).throwToken(CacheProvider.COMPONENT.INTERFACE);
        verify(eFilesAuthenticator, times(1)).authenticate();

    }

    @Test
    @DisplayName("If token is null, throw token then authenticate")
    void renew_whenTokenNull() {
        //ARRANGE
        when(cacheProvider.getToken(INTERFACE)).thenReturn(new Token());
        when(eFilesAuthenticator.authenticate()).thenReturn(Mono.just(new Token()));
        doNothing().when(cacheProvider).throwToken(INTERFACE);

        //ACT
        retryPolicy.checkTokenOrElseRenew(CLIENT_REQUEST, eFilesAuthenticator, cacheProvider, INTERFACE).block();

        //ASSERT
        verify(cacheProvider, times(1)).throwToken(INTERFACE);
        verify(eFilesAuthenticator, times(1)).authenticate();

    }


}
