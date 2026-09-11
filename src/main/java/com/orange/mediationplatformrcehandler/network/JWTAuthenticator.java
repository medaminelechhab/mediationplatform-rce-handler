package com.MyProject.mediationplatformrcehandler.network;

import com.MyProject.mediationplatform.common.model.Token;
import com.MyProject.mediationplatform.common.network.Authenticator;
import com.MyProject.mediationplatform.common.network.CacheProvider;
import com.MyProject.mediationplatform.common.network.NetworkConfigurer;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import com.MyProject.mediationplatformrcehandler.model.efiles.JWTToken;
import io.netty.channel.ChannelException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
@Component
public class JWTAuthenticator extends Authenticator<JWTParameters> {

    @Autowired
    private EFilesAuthenticator eFilesAuthenticator;


    @Getter
    private Token tokenODI= new Token();


    public JWTAuthenticator(JWTParameters connectionParameters, NetworkConfigurer provider, RetryPolicy retryPolicy, CacheProvider cacheProvider) {
        super(connectionParameters, provider, retryPolicy, cacheProvider);
    }

    private Token accessToken = new Token();

    @Override
    public Mono<Token> authenticate() {

        Token token = cacheProvider.getToken(component);
        if (token.getAccessToken() == null) {
            return getODIToken().flatMap(token1 -> {

                    return webClient.post()
                            .headers(getJWTAuthHeaders(token1))
                            .retrieve()
                            .bodyToMono(JWTToken.class)
                            .flatMap(jwtToken1 -> {
                                accessToken.setAccessToken(jwtToken1.getAccessToken());
                                accessToken.setExpiresIn(jwtToken1.getExpiresIn());
                                accessToken.setRetrievedAt(jwtToken1.getDate());
                                log.info("access token",accessToken);
                                return Mono.just(accessToken);
                            })
                            .doOnError(ChannelException.class, e -> log.warn("The request is taking longer than usual ..."))
                            .retryWhen(retryPolicy.forServerError)
                            .flatMap(this::cacheToken);
                });

        } else {
            return returnSavedToken(component, token);
        }

    }

    private Consumer<HttpHeaders> getJWTAuthHeaders(Token token) {
        return httpHeaders -> {
            httpHeaders.setBearerAuth(token.getAccessToken());
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED.toString());
            httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
            httpHeaders.add(HttpHeaders.CACHE_CONTROL, "no-cache");
        };
    }


    private Mono<Token> getODIToken(){
        Token token = cacheProvider.getToken(component);
        if (tokenODI.getAccessToken() == null) {
            Mono<Token>tokenODI = eFilesAuthenticator.authenticate();
            log.info("token ODI get it",tokenODI);
            return tokenODI;

        }else {
            return returnSavedToken(component, token);
        }
    }
}
