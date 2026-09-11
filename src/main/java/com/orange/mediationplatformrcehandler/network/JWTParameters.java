package com.MyProject.mediationplatformrcehandler.network;

import com.MyProject.mediationplatform.common.model.InterfaceParameters;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@Data
@ToString
public class JWTParameters extends InterfaceParameters {

    @Value("${clinksplatform.efiles.connection.base-url}")
    private String baseUrl;
    @Value("${clinksplatform.efiles.connection.auth-url}")
    private String authUrl;

    @Override
    public void setAuthorisation(HttpHeaders httpHeaders, String accessToken) {
        httpHeaders.setBearerAuth(accessToken);
    }
}
