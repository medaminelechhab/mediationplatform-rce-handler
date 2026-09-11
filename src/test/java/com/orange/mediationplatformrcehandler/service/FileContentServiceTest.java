package com.MyProject.mediationplatformrcehandler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.MyProject.mediationplatform.common.error.ApiCallException;
import com.MyProject.mediationplatform.common.model.Token;
import com.MyProject.mediationplatform.common.network.CacheProvider;
import com.MyProject.mediationplatform.common.network.NetworkConfigurer;
import com.MyProject.mediationplatform.common.network.RetryPolicy;
import com.MyProject.mediationplatformrcehandler.network.*;
import com.MyProject.mediationplatformrcehandler.service.efiles.FileContentService;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static com.MyProject.mediationplatform.common.network.CacheProvider.COMPONENT.EFILES;
import static com.MyProject.mediationplatform.common.util.Constants.API_CALL_RESPONSE_ERROR_MESSAGE;
import static com.MyProject.mediationplatformrcehandler.utils.FileFactory.errorResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class FileContentServiceTest {

    private static MockWebServer mockBackEnd;

    private FileContentService fileContentService;

    @MockitoBean
    private JWTParameters jwtParameters;

    @MockitoBean
    private JWTAuthenticator authenticator;

    @MockitoBean
    private EFilesParameters eFilesParameters;

    @MockitoBean
    private EFilesAuthenticator eFilesAuthenticator;

    @MockitoBean
    private NetworkConfigurer networkConfigurer;

    @Spy
    private RetryPolicy retryPolicy;

    private final CacheProvider cacheProvider = new CacheProvider();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

    private static final String OPERATIONID = "78ff1783-0950-4134-b207-760683ab2425";

    private Token tokenJWT;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        JWTParameters mockParameters = jwtParameters;
        EFilesParameters filesParameters = eFilesParameters;
        when(mockParameters.getBaseUrl()).thenReturn(baseUrl);
        when(filesParameters.getBaseUrl()).thenReturn(baseUrl);
        Mockito.when(mockParameters.isUseProxy()).thenReturn(false);
        EFilesClientConfigurer eFilesClientConfigurer = new EFilesClientConfigurer(filesParameters, eFilesAuthenticator, networkConfigurer, retryPolicy, cacheProvider);
        ReflectionTestUtils.setField(eFilesClientConfigurer, "component", CacheProvider.COMPONENT.INTERFACE);

        JWTClientConfigurer jwtClientConfigurer = new JWTClientConfigurer(mockParameters, authenticator, networkConfigurer, retryPolicy, cacheProvider);
        ReflectionTestUtils.setField(jwtClientConfigurer, "component", CacheProvider.COMPONENT.EFILES);
        fileContentService = new FileContentService(jwtClientConfigurer, authenticator);

        tokenJWT = new Token(
                "FAKE_TOKEN",
                "type",
                "scope",
                1800,
                LocalDateTime.now(),
                "appName"
        );
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();

        Files.deleteIfExists(Path.of("data-rce.tgz"));
    }

    @Test
    @DisplayName("Verify e-files call  to get file content ")
    void getFileContent_mustReturnOk() throws Exception {

        ReflectionTestUtils.setField(fileContentService, "temporaryDir", "");

        String fileContent = "Mock file content";
        File file = createTempFileWithContent(fileContent);

        MockResponse response = new MockResponse()
                .setResponseCode(200)
                .setBody(String.valueOf(file))
                .setHeader("Content-Type", "application/octet-stream");


        mockBackEnd.enqueue(response);

        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());

        when(authenticator.authenticate()).thenReturn(Mono.just(tokenJWT));

        cacheProvider.setToken(EFILES, tokenJWT);

        var retrievedFile = fileContentService.getContentFile(OPERATIONID).block();

        assertNotNull(retrievedFile);
        verify(authenticator, times(1)).authenticate();
        assertTrue(retrievedFile.exists());
        assertEquals("data-rce.tgz", retrievedFile.getName());
    }

    @Test
    @DisplayName("Verify e-files call  to get unauthorized response from efiles when get file content")
    void getFileContent_mustReturnUnauthorized() {
        ApiCallException customApiException = new ApiCallException(API_CALL_RESPONSE_ERROR_MESSAGE, HttpStatus.UNAUTHORIZED, "Invalid body field");

        Mockito.doAnswer(
                        invocation -> {
                            throw customApiException;
                        })
                .when(authenticator)
                .authenticate();

        ApiCallException apiCallException = assertThrows(ApiCallException.class,
                () -> fileContentService.getContentFile(OPERATIONID).block());

        assertEquals("Invalid body field", apiCallException.getResponseBody());
    }

    @Test
    @DisplayName("Verify e-files call  to get bad request response from efiles when get file content")
    void getFileContent_mustReturnBadRequest() throws Exception {

        when(authenticator.authenticate()).thenReturn(Mono.just(tokenJWT));
        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(objectMapper.writeValueAsString(errorResponse())));

        cacheProvider.setToken(EFILES, tokenJWT);

        try {
            var response = fileContentService.getContentFile(OPERATIONID).block();

        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ApiCallException);
            assertTrue(e.getMessage().contains("400"));
        }
    }

    @Test
    @DisplayName("Verify e-files call  to get not found response from efiles when get file content")
    void getFileContent_mustReturnNotFound() throws Exception {

        when(authenticator.authenticate()).thenReturn(Mono.just(tokenJWT));
        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .setBody(objectMapper.writeValueAsString(errorResponse())));

        cacheProvider.setToken(EFILES, tokenJWT);

        try {
            var response = fileContentService.getContentFile(OPERATIONID).block();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ApiCallException);
            assertTrue(e.getMessage().contains("404"));
        }
    }


    @Test
    @DisplayName("Verify e-files call  to get conflict response from efiles when get file content")
    void getFileContent_mustReturnConflict() throws Exception {

        when(authenticator.authenticate()).thenReturn(Mono.just(tokenJWT));
        when(networkConfigurer.tcp(anyBoolean(), anyBoolean())).thenReturn(TcpClient.newConnection());

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.CONFLICT.value())
                .setBody(objectMapper.writeValueAsString(errorResponse())));

        cacheProvider.setToken(EFILES, tokenJWT);

        try {
            var response = fileContentService.getContentFile(OPERATIONID).block();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ApiCallException);
            assertTrue(e.getMessage().contains("409"));
        }
    }

    private File createTempFileWithContent(String content) {
        try {
            File tempFile = File.createTempFile("mock-file-", ".tgz");
            FileWriter fileWriter = new FileWriter(tempFile);
            fileWriter.write(content);
            fileWriter.close();
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary file with content", e);
        }
    }
}
