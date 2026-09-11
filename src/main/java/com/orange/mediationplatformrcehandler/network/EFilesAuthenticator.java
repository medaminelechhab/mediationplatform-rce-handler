package com.MyProject.mediationplatformrcehandler.network;

import com.MyProject.mediationplatform.common.network.Authenticator;
import com.MyProject.mediationplatform.common.network.CacheProvider;
import com.MyProject.mediationplatform.common.network.NetworkConfigurer;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EFilesAuthenticator extends Authenticator<EFilesParameters> {

    public EFilesAuthenticator(EFilesParameters connectionParameters, NetworkConfigurer provider, RetryPolicy retryPolicy, CacheProvider cacheProvider) {
        super(connectionParameters, provider, retryPolicy, cacheProvider);
        this.log = LoggerFactory.getLogger(EFilesAuthenticator.class);
    }


}
