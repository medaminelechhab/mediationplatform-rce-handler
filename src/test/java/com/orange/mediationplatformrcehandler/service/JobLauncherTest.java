package com.MyProject.mediationplatformrcehandler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Account;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Body;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.mapper.EntrepriseMapper;
import com.MyProject.mediationplatformrcehandler.mapper.EstablishMapper;
import com.MyProject.mediationplatformrcehandler.mapper.GroupeMapper;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Entreprise;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Establishment;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Group;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import com.MyProject.mediationplatformrcehandler.service.efiles.EFilesService;
import com.MyProject.mediationplatformrcehandler.service.efiles.FileContentService;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement;
import com.MyProject.mediationplatformrcehandler.utils.Constants;
import com.MyProject.mediationplatformrcehandler.utils.TempFileFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class JobLauncherTest {

    @Autowired
    private JobLauncherTestUtils jobLauncher;


    @MockitoBean
    private RCEService rceService;
    @MockitoBean
    private EFilesService eFilesService;
    @MockitoBean
    private FileContentService fileContentService;
    private EntrepriseMapper entrepriseMapper;
    private EstablishMapper establishMapper;
    private GroupeMapper groupeMapper;

    private List<String> filteredEntreprises = new ArrayList<>();
    private List<String> filteredEtablissements = new ArrayList<>();

    @Mock
    private StepExecution stepExecution;

    @Value("${clinksplatform.tmp-storage-dir}")
    private String tmpDirectory;


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

    @DisplayName("when launch job, must call methods getFiles and getContentFile")
    @Test
    void launchJob() throws Exception {

        var account = new Account();
        account.setId("001E000001IMSBKI99");
        generateTempFiles();
        File file = new File("src/test/resources/rce/data-rce.tgz");

        when(eFilesService.getFiles(anyMap(),anyMap()))
            .thenReturn(Mono.just(List.of(createObject())));
        when(fileContentService.getContentFile(anyString()))
            .thenReturn(Mono.just(file));

        when(rceService.getAccountByRceID(any(), any()))
            .thenReturn(Mono.just(new Body<>(false, List.of(account))));

        when(rceService.patch(any()))
            .thenReturn(Mono.just(new ArrayList<>()));

        var jobParameters = new JobParametersBuilder()
            .addString("typeManagement", FilesManagement.TypeManagement.DAILY.toString())
            .toJobParameters();
        jobLauncher.launchJob(jobParameters);

        verify(eFilesService, times(1)).getFiles(anyMap(), anyMap());
        verify(fileContentService, times(1)).getContentFile(anyString());
    }

    @DisplayName("when group exist in CL, must call patch method")
    @Test
    @Disabled
    void patchGroupInCL() throws Exception {

        var account = new Account();
        account.setId("001E000001IMSBKI99");

        when(eFilesService.getFiles(anyMap(),anyMap())).thenReturn(Mono.just(List.of(createObject())));

        when(fileContentService.getContentFile("1ab87pfm")).thenReturn(Mono.empty());

        when(rceService.getAccountByRceID(any(), any()))
                .thenReturn(Mono.just(new Body<>(false, List.of(account))));

        when(rceService.patch(any()))
                .thenReturn(Mono.just(new ArrayList<>()));


        jobLauncher.launchJob();

        verify(rceService, times(0)).post(any());
        verify(rceService, times(2)).getAccountByRceID(eq("Groupe"), eq("049060"));
        verify(rceService, times(1)).patch(any());

        ArgumentCaptor<Body<Group>> argument = ArgumentCaptor.forClass(Body.class);
        verify(rceService, times(1)).patch(argument.capture());

        ObjectMapper mapper = new ObjectMapper();
        Group group = mapper.convertValue(argument.getValue().getRecords().get(0), Group.class);

        assertEquals("001E000001IMSBKI99", group.getId());
        assertEquals("VILLE DE FOUGERES", group.getName());
        assertEquals("049060", group.getRceID());
    }


    @DisplayName("when group is not created in CL, must call post methode")
    @Test
    @Disabled
    void postGroupInCL() throws Exception {

        when(eFilesService.getFiles(anyMap(),anyMap())).thenReturn(Mono.just(List.of(createObject())));

        when(fileContentService.getContentFile("1ab87pfm")).thenReturn(Mono.empty());

        when(rceService.getAccountByRceID(any(), any()))
                .thenReturn(Mono.just(new Body<>(false, new ArrayList<>())));

        when(rceService.post(any()))
                .thenReturn(Mono.just(new ArrayList<>()));

        jobLauncher.launchJob();

        //verify(rceService, times(2)).getAccountByRceID(eq("Groupe"), any());
        verify(rceService, times(1)).post(any());
        verify(rceService, times(0)).patch(any());

        ArgumentCaptor<Body<Group>> argument = ArgumentCaptor.forClass(Body.class);
        verify(rceService).post(argument.capture());

        ObjectMapper mapper = new ObjectMapper();
        Group group = mapper.convertValue(argument.getValue().getRecords().get(0), Group.class);

        assertNull(group.getId());
        assertEquals("VILLE DE FOUGERES", group.getName());
        assertEquals("049060", group.getRceID());

    }

    @DisplayName("when enterprise exist in CL, must call patch methode")
    @Test
    @Disabled
    void patchEntrepriseInCL() throws Exception {

        var account = new Account();
        account.setId("001E000001IMSBKI99");

        when(eFilesService.getFiles(anyMap(),anyMap())).thenReturn(Mono.just(List.of(createObject())));

        when(fileContentService.getContentFile("1ab87pfm")).thenReturn(Mono.empty());

        when(rceService.getAccountByRceID(any(), any()))
                .thenReturn(Mono.just(new Body<>(false, List.of(account))));

        when(rceService.patch(any()))
                .thenReturn(Mono.just(new ArrayList<>()));

        when(entrepriseMapper.mapEnterpriseToCL(filteredEntreprises))
                .thenReturn(List.of(Entreprise.builder().
                        id("001E000001IMSBKI99").rceID("rceId").name("Name").build()));

        jobLauncher.launchJob();
        verify(rceService, times(0)).post(any());
        verify(rceService, times(1)).patch(any());

        ArgumentCaptor<Body<Entreprise>> argument = ArgumentCaptor.forClass(Body.class);
        verify(rceService).patch(argument.capture());

        ObjectMapper mapper = new ObjectMapper();
        Entreprise entreprise = mapper.convertValue(argument.getValue().getRecords().get(0), Entreprise.class);

        assertEquals("001E000001IMSBKI99", entreprise.getId());
        assertEquals("Name", entreprise.getName());
        assertEquals("rceId", entreprise.getRceID());

    }

    @DisplayName("when enterprise does not exist in CL, must call post methode")
    @Test
    @Disabled
    void postEntrepriseInCL() throws Exception {
        when(eFilesService.getFiles(any(), anyMap())).thenReturn(Mono.empty());

        when(entrepriseMapper.mapEnterpriseToCL(filteredEntreprises))
                .thenReturn(List.of(Entreprise.builder()
                        .rceID("rceId")
                        .name("Name").build()));

        when(rceService.getAccountByRceID(eq("Entreprise"), eq("rceId")))
                .thenReturn(Mono.just(new Body<>(false, new ArrayList<>())));

        when(rceService.post(any()))
                .thenReturn(Mono.just(new ArrayList<>()));

        jobLauncher.launchJob();
        verify(rceService, times(1)).post(any());
        verify(rceService, times(0)).patch(any());

        ArgumentCaptor<Body<Entreprise>> argument = ArgumentCaptor.forClass(Body.class);
        verify(rceService).post(argument.capture());

        ObjectMapper mapper = new ObjectMapper();
        Entreprise entreprise = mapper.convertValue(argument.getValue().getRecords().get(0), Entreprise.class);

        assertEquals(null, entreprise.getId());
        assertEquals("Name", entreprise.getName());
        assertEquals("rceId", entreprise.getRceID());

    }


    @DisplayName("when establishment exist in CL, must call patch methode")
    @Test
    @Disabled
    void patchEstablishmentInCL() throws Exception {
        when(eFilesService.getFiles(anyMap(),anyMap())).thenReturn(Mono.empty());

        when(establishMapper.mapEstablishmentToCL(filteredEtablissements))
                .thenReturn(List.of(Establishment.builder().rceID("rceId").
                        name("Name").build()));

        var account = new Account();
        account.setId("001E000001IMSBKI99");

        when(rceService.getAccountByRceID(eq("Etablissement"), eq("rceId")))
                .thenReturn(Mono.just(new Body<>(false, List.of(account))));

        when(rceService.patch(any()))
                .thenReturn(Mono.just(new ArrayList<>()));

        jobLauncher.launchJob();

        verify(rceService, times(0)).post(any());
        verify(rceService, times(1)).getAccountByRceID(eq("Etablissement"), eq("rceId"));
        verify(rceService, times(1)).patch(any());

        ArgumentCaptor<Body<Establishment>> argument = ArgumentCaptor.forClass(Body.class);
        verify(rceService).patch(argument.capture());

        ObjectMapper mapper = new ObjectMapper();
        Establishment establishment = mapper.convertValue(argument.getValue().getRecords().get(0), Establishment.class);

        assertEquals("001E000001IMSBKI99", establishment.getId());
        assertEquals("Name", establishment.getName());
        assertEquals("rceId", establishment.getRceID());
    }

    @DisplayName("when establishment does not exist in CL, must call post methode")
    @Test
    @Disabled
    void postEstablishmentInCL() throws Exception {
        when(eFilesService.getFiles(anyMap(),anyMap())).thenReturn(Mono.empty());

        when(establishMapper.mapEstablishmentToCL(filteredEtablissements))
                .thenReturn(List.of(Establishment.builder().rceID("rceId").
                        name("Name").build()));

        when(rceService.getAccountByRceID(eq("Etablissement"), eq("rceId")))
                .thenReturn(Mono.just(new Body<>(false, List.of())));

        when(rceService.post(any()))
                .thenReturn(Mono.just(new ArrayList<>()));

        jobLauncher.launchJob();
        verify(rceService, times(1)).post(any());
        verify(rceService, times(1)).getAccountByRceID(eq("Etablissement"), eq("rceId"));
        verify(rceService, times(0)).patch(any());

        ArgumentCaptor<Body<Establishment>> argument = ArgumentCaptor.forClass(Body.class);
        verify(rceService).post(argument.capture());

        ObjectMapper mapper = new ObjectMapper();
        Establishment establishment = mapper.convertValue(argument.getValue().getRecords().get(0), Establishment.class);

        assertNull(establishment.getId());
        assertEquals("Name", establishment.getName());
        assertEquals("rceId", establishment.getRceID());
    }


    @DisplayName("when group is deleted in RCE, must call delete methode")
    @Test
    @Disabled
    void deleteGroupInCL() throws Exception {
        when(eFilesService.getFiles(anyMap(),anyMap())).thenReturn(Mono.empty());

        var account = new Account();
        account.setId("001E000001IMSBKI99");
        //049060
        when(rceService.getAccountByRceID(eq("Groupe"), eq("049060")))
                .thenReturn(Mono.just(new Body<>(false, List.of(account))));

        when(rceService.delete(List.of("001E000001IMSBKI99"), false))
                .thenReturn(Mono.just(new ArrayList<>()));

        var groupToDelete = rceGroup();
        groupToDelete.setSuppressionDate(LocalDate.now());

        //when(groupeMapper.mapGroupToCL()).thenReturn(List.of(groupToDelete));
        jobLauncher.launchJob();
        verify(rceService, times(0)).post(any());
        verify(rceService, times(1)).getAccountByRceID(eq("Groupe"), eq("049060"));
        verify(rceService, times(0)).patch(any());
        verify(rceService).delete(List.of("001E000001IMSBKI99"), false);

    }


    private Group rceGroup() {

        return Group.builder()
                .rceID("049060")
                .name("VILLE DE FOUGERES")
                .sirenID("213501158")
                .groupSirenId("213501158")
                .accountCurrency("EUR")
                .accountSource("RCE_France")
                .inactive(false)
                .codeEDG("EAM")
                .labelEDG("Dir Ciale GO OA")
                .codeSegNat("V")
                .rceSegmentationNational("MdM SPE")
                .codeSegLoc("V")
                .rceSegmentationLocal("MdM SPE")
                .legalStructure("Commune et commune nouvelle")
                .apen31("O ADMINISTRATION PUBLIQUE")
                .naf("8411Z Administration publique generale")
                .nic("00017")
                .street("2  RUE PORTE ST-LEONARD" + System.lineSeparator() + "COMMUNE DE FOUGERES" + System.lineSeparator())
                .postalCode("35300")
                .country("FRANCE")
                .nationalMacroSectorization("A-Administrations & vie publique")
                .nationalMicroSectorization("A2-Collectivites locales")
                .localMicroSectorization("A1-Administration publique generale")
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
}