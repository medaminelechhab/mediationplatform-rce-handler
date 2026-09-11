package com.MyProject.mediationplatformrcehandler.network;

import com.MyProject.mediationplatform.common.model.Token;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
public class EFilesAuthenticatorTest {
    @Autowired
    private EFilesAuthenticator eFilesAuthenticator;

    @Test
    @DisplayName("Authenticating to Efiles should return a token")
    @Disabled
    void shouldGetToken() {
        Token token = eFilesAuthenticator.authenticate().block();
        System.out.println(token.toString());
        Assertions.assertThat(token.getAccessToken()).isNotNull();
    }
}
