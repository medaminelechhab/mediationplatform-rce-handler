package com.MyProject.mediationplatformrcehandler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.MyProject.mediationplatform.common.error.ApiCallException;
import com.MyProject.mediationplatform.common.model.Token;
import com.MyProject.mediationplatform.common.network.CacheProvider;
import com.MyProject.mediationplatform.common.network.NetworkConfigurer;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import com.MyProject.mediationplatformrcehandler.network.EFilesAuthenticator;
import com.MyProject.mediationplatformrcehandler.network.EFilesClientConfigurer;
import com.MyProject.mediationplatformrcehandler.network.EFilesParameters;
import com.MyProject.mediationplatformrcehandler.service.efiles.EFilesService;
import com.MyProject.mediationplatformrcehandler.utils.FileFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.MyProject.mediationplatformrcehandler.utils.FileFactory.errorResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Slf4j
class EFilesServiceTest {

    private static final Map<String, Object> FILTERS = Map.of("sort", "-creationDate");

    private static MockWebServer mockBackEnd;

    @MockitoBean
    private EFilesParameters eFilesParameters;

    @MockitoBean
    private EFilesAuthenticator authenticator;

    @MockitoBean
    private NetworkConfigurer networkConfigurer;

    @Spy
    private RetryPolicy retryPolicy;

    private final CacheProvider cacheProvider = new CacheProvider();

    private EFilesService eFilesService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() {
        try {
            mockBackEnd.shutdown();
        } catch (IOException e) {
            log.warn("", e);
        }
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        EFilesParameters mockParameters = eFilesParameters;
        Mockito.when(mockParameters.getBaseUrl()).thenReturn(baseUrl);
        Mockito.when(mockParameters.isUseProxy()).thenReturn(false);
        EFilesClientConfigurer eFilesClientConfigurer = new EFilesClientConfigurer(mockParameters, authenticator, networkConfigurer, retryPolicy, cacheProvider);
        ReflectionTestUtils.setField(eFilesClientConfigurer, "component", CacheProvider.COMPONENT.INTERFACE);

        //Mockito.when(mock.retryWhen(any())).thenReturn(Mono.just(null));
        eFilesService = new EFilesService(eFilesClientConfigurer);

    }

    @Test
    @DisplayName("Verify e-files call  to get rce files")
    void getFiles_mustReturnOk() throws Exception {

        Token token = new Token(
                "FAKE_TOKEN",
                "type",
                "scope",
                3600,
                LocalDateTime.now()
                , "appName"
        );

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(FileFactory.fileList()))
                .addHeader("Content-Type", "application/json")
        );

        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());

        when(authenticator.authenticate()).thenReturn(Mono.just(token));

        cacheProvider.setToken(CacheProvider.COMPONENT.INTERFACE, token);

        List<EFile> result = eFilesService.getFiles(FILTERS, null).block();
        RecordedRequest request = mockBackEnd.takeRequest();

        assertEquals("GET", request.getMethod());
        assertNotNull(request.getPath());
        assertTrue(request.getPath().contains("/files"));

        assertNotNull(result);
        Assertions.assertEquals(result.get(0).getFileName(), FileFactory.fileList().get(0).getFileName());
        Assertions.assertEquals(FileFactory.fileList().get(0).getFileSize(), result.get(0).getFileSize());
        Assertions.assertEquals(FileFactory.fileList().get(0).getOperationId(), result.get(0).getOperationId());
        Assertions.assertEquals(FileFactory.fileList().get(0).getStreamId(), result.get(0).getStreamId());
        Assertions.assertEquals(FileFactory.fileList().get(0).getCreationDate(), result.get(0).getCreationDate());
        Assertions.assertEquals(FileFactory.fileList().get(0).getExpirationDate(), result.get(0).getExpirationDate());
        Assertions.assertEquals(FileFactory.fileList().get(0).getSender(), result.get(0).getSender());
        Assertions.assertEquals(FileFactory.fileList().get(0).getRecipients().get(0), result.get(0).getRecipients().get(0));
        Assertions.assertEquals(FileFactory.fileList().get(0).getTags().get(0), result.get(0).getTags().get(0));
        Assertions.assertEquals(FileFactory.fileList().get(0).getCriticality(), result.get(0).getCriticality());
    }

    @Test
    @DisplayName("Verify e-files call  to get unauthorized response from efiles")
    void getFiles_mustReturnUnauthorized() throws JsonProcessingException {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.UNAUTHORIZED.value())
                .setBody(objectMapper.writeValueAsString(errorResponse())));
        try {
            var response = eFilesService.getFiles(FILTERS, null).block();

        } catch (Exception e) {
            log.info("exception" + e.getMessage());
            assertThat(e instanceof ApiCallException);
        }
    }

    @Test
    @DisplayName("Verify e-files call  to get bad request response from efiles")
    void getFiles_mustReturnBadRequest() throws Exception {
        Token token = new Token(
                "FAKE_TOKEN",
                "type",
                "scope",
                3600,
                LocalDateTime.now(),
                "appName"
        );

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(objectMapper.writeValueAsString(errorResponse())));

        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());

        when(authenticator.authenticate()).thenReturn(Mono.just(token));

        cacheProvider.setToken(CacheProvider.COMPONENT.INTERFACE, token);

        try {
            eFilesService.getFiles(FILTERS, null).block();
        } catch (Exception e) {
            log.info("exception" + e.getMessage());
            assertTrue(e.getCause() instanceof ApiCallException);
            assertTrue(e.getMessage().contains("400"));
        }
    }
}
