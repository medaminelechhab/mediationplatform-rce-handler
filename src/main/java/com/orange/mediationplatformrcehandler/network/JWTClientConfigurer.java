package com.MyProject.mediationplatformrcehandler.network;

import com.MyProject.mediationplatform.common.network.CacheProvider;
import com.MyProject.mediationplatform.common.network.NetworkConfigurer;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import com.MyProject.mediationplatform.common.network.WebClientConfigurer;
import org.springframework.stereotype.Service;

@Service
public class JWTClientConfigurer extends WebClientConfigurer<JWTParameters, JWTAuthenticator> {
    public JWTClientConfigurer(JWTParameters parameters, JWTAuthenticator authenticator, NetworkConfigurer provider, RetryPolicy retryPolicy, CacheProvider cacheProvider) {
        super(parameters, authenticator, provider, retryPolicy, cacheProvider);
    }
}
