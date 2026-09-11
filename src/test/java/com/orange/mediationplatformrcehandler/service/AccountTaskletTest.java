package com.MyProject.mediationplatformrcehandler.service;

import com.MyProject.mediationplatform.common.error.ApiCallException;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import com.MyProject.mediationplatformrcehandler.service.efiles.EFilesService;
import com.MyProject.mediationplatformrcehandler.service.efiles.FileContentService;
import com.MyProject.mediationplatformrcehandler.service.referential.GroupCCIALOrgService;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement;
import com.MyProject.mediationplatformrcehandler.utils.Constants;
import com.MyProject.mediationplatformrcehandler.utils.TempFileFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.MyProject.mediationplatform.common.util.Constants.API_CALL_RESPONSE_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
@Slf4j
class AccountTaskletTest {

    @Value("${clinksplatform.tmp-storage-dir}")
    private String tmpDirectory;

    @Autowired
    private AccountTasklet accountTasklet;

    @MockBean
    private EFilesService eFilesService;

    @MockBean
    private FileContentService fileContentService;

    @MockBean
    private PersistenceService persistenceService;

    @MockBean
    private GroupCCIALOrgService groupCcialOrgService;

    @Mock
    private StepExecution stepExecution;

    @BeforeEach
    void createTmpDirectory() {
        File tmpDir = new File(tmpDirectory);
        if (!tmpDir.exists()){
            tmpDir.mkdirs();
        }
        log.info("Working directory is {}", tmpDirectory);
    }

    @AfterEach
    void cleanTmpDirectory() throws IOException {
        Files.list(Path.of(tmpDirectory)).forEach((p) -> {
            try {
                Files.deleteIfExists(p);
                log.info("files deleted");
            } catch (Exception e) {
                log.error("cannot clean directory {}", e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("when launch job, must call parse files and get desired objects")
    void givenValidFiles_WhenParseIsCalled_resultIsOK() throws IOException {

        // here
        generateTempFiles();
        doNothing().when(persistenceService).refresh(anyList(), anyList(), anyList(),
                anyList(), anyList(), anyList(),
                anyList(), anyList(), anyList(),
                anyList(), anyList());

        accountTasklet.parseAndSaveFiles();
        assertNotNull(accountTasklet.getGroupeMapper());
        assertNotNull(accountTasklet.getGroupeMapper().getAccountMapper());
        assertEquals(2, accountTasklet.getGroupeMapper().getAccountMapper().groupCCIALOrgs().size());
    }

    @Test
    @DisplayName("when launch job, if one of files not found an exception expected")
    void givenNonexistentFiles_WhenParseIsCalled_noResult() {

        IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> accountTasklet.parseAndSaveFiles(),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("not found"));

        assertNull(accountTasklet.getGroupeMapper());
        assertNull(accountTasklet.getEntrepriseMapper());
        assertNull(accountTasklet.getEstablishMapper());
    }

    @Test
    @DisplayName("when launch job, if one of files not found an exception expected")
    void givenValidFiles_WhenRefreshReferentialFailed_anExceptionThrown() throws IOException {

        generateTempFiles();
        doThrow(new IllegalStateException()).when(persistenceService).refresh(anyList(), anyList(), anyList(),
                anyList(), anyList(), anyList(),
                anyList(), anyList(), anyList(),
                anyList(), anyList());

        IllegalStateException thrown = Assertions.assertThrows(
                IllegalStateException.class,
                () -> accountTasklet.parseAndSaveFiles(),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("Cannot refresh referential"));

        assertNull(accountTasklet.getGroupeMapper());
        assertNull(accountTasklet.getEntrepriseMapper());
        assertNull(accountTasklet.getEstablishMapper());
    }

    @Test
    @DisplayName("when launch job, if files have bad delimiter an exception thrown")
    void givenFilesWithBadDelimiter_WhenParseIsCalled_anExceptionThrown() throws IOException {

        Supplier<List<String>> badDelimiterBody = () -> TempFileFactory.getGroupCCIALOrgData().stream()
                .map(e -> e.replace('|', ';'))
                .toList();

        generateFile("GPE-CCIAL-ORG_", badDelimiterBody);

        IllegalArgumentException thrown = Assertions.assertThrows(
            IllegalArgumentException.class,
                () -> accountTasklet.parseAndSaveFiles(),
                "Expected doThing() to throw, but it didn't"
        );
    }

    @DisplayName("when launch job , must call tasklet to get file and extracted in method before step")
    @Test
    void beforeStep() {

        when(stepExecution.getJobParameters()).thenReturn(getJobParameters());
        when(eFilesService.getFiles(anyMap(), anyMap())).thenReturn(Mono.just(List.of(createObject())));
        when(fileContentService.getContentFile("1ab87pfm")).thenReturn(Mono.empty());

        accountTasklet.beforeStep(stepExecution);

        verify(eFilesService, times(1)).getFiles(anyMap(), anyMap());

        verify(fileContentService, times(1)).getContentFile("1ab87pfm");
    }

    @DisplayName("when launch job with non existent files, must call tasklet to get file and extracted in method before step")
    @Test
    void beforeStepWithNonexistentFiles() throws IOException {

        File file = new File("src/test/resources/rce/data-rce.tgz");

        Files.copy(file.toPath(),
                (new File(tmpDirectory + file.getName())).toPath());

        when(eFilesService.getFiles(anyMap(), anyMap())).thenReturn(Mono.just(List.of(createObject())));
        when(fileContentService.getContentFile("1ab87pfm")).thenReturn(Mono.just(new File(tmpDirectory.concat("data-rce.tgz"))));
        when(stepExecution.getJobParameters()).thenReturn(getJobParameters());


        IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> accountTasklet.beforeStep(stepExecution),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(thrown.getMessage().contains("not found"));

        verify(eFilesService, times(1)).getFiles(anyMap(), anyMap());

        verify(fileContentService, times(1)).getContentFile("1ab87pfm");
    }

    @DisplayName("when launch job with Error Connecting To Efile, must throw  ApiCallException")
    @Test
    void beforeStepWithErrorConnectingToEfile() {

        ApiCallException apiCallException = new ApiCallException(API_CALL_RESPONSE_ERROR_MESSAGE, HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable");

        when(eFilesService.getFiles(anyMap(), anyMap()))
            .thenReturn(Mono.error(apiCallException));
        when(stepExecution.getJobParameters()).thenReturn(getJobParameters());

        try {
            accountTasklet.beforeStep(stepExecution);
        } catch (Exception e) {
            assertNotNull(e);
            assertTrue(e.getCause() instanceof ApiCallException);
        }
    }

    @DisplayName("when launch job, at some point if eFILES doesn't provide the today tgz file, must throw IllegalArgumentException")
    @Test
    void beforeStepWithUnavailableTGZFile() {

        EFile eFile = createObject();
        eFile.setCreationDate(OffsetDateTime.now().minusDays(1));

        when(eFilesService.getFiles(anyMap(), anyMap())).thenReturn(Mono.empty());
        when(stepExecution.getJobParameters()).thenReturn(getJobParameters());

        try {
            accountTasklet.beforeStep(stepExecution);
        } catch (Exception e) {
            assertNotNull(e);
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    private EFile createObject() {
        return EFile.builder()
                .fileName("test" + Constants.RCE_FILE_EXTENTION)
                .creationDate(OffsetDateTime.now())
                .tags(List.of("tag 1", "tag 2"))
                .criticality("test")
                .expirationDate(null)
                .fileSize(500)
                .operationId("1ab87pfm")
                .build();
    }

    private void generateFile(String name, Supplier<List<String>> linesSupplier) throws IOException {
        File file = new File(tmpDirectory, name + Constants.DF_YYYYMMDD.format(LocalDate.now()) + ".txt");
        Files.write(file.toPath(), linesSupplier.get());
    }

    private void generateTempFiles() throws IOException {
        generateFile("REF-RCE_", TempFileFactory::getRefRceData);
        generateFile("ETA-REF-XL2_", TempFileFactory::getEtaReferenceNewData);
        generateFile("ETA-ME-ORG-NEW_", TempFileFactory::getEtaMeOrgNewData);
        generateFile("ENT-ME-ORG_", TempFileFactory::getEntMeOrgData);
        generateFile("GPE-CCIAL-ORG_", TempFileFactory::getGroupCCIALOrgData);
        generateFile("GPE-CCIAL-COMP_", TempFileFactory::getGroupCCIALCompData);
        generateFile("ENT-REF-XL2_", TempFileFactory::getEntReferenceNewData);
        generateFile("ENT-ME-COMP_", TempFileFactory::getEntMeCompData);
        generateFile("GPE-CCIAL-CONSTIT_", TempFileFactory::getGroupeCcialConstitData);
        generateFile("ENT-SCORING-NOTEORT_", TempFileFactory::getEntScoringNoteortData);
        generateFile("ENT-ECO-NEW_", TempFileFactory::getEntEcoNewData);
    }

    private JobParameters getJobParameters() {
        return new JobParametersBuilder()
            .addString("typeManagement", FilesManagement.TypeManagement.DAILY.toString())
            .toJobParameters();
    }
}
