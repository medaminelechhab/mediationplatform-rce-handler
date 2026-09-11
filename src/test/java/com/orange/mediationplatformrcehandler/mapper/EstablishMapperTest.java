package com.MyProject.mediationplatformrcehandler.mapper;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.Account;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Attributes;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Body;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.UserRecord;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.UsersList;
import com.MyProject.mediationplatform.common.model.resiliency.technical.TechnicalTransaction;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.AccountService;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Establishment;
import com.MyProject.mediationplatformrcehandler.model.rce.*;
import com.MyProject.mediationplatformrcehandler.service.CacheService;
import com.MyProject.mediationplatformrcehandler.service.referential.EtaMeOrgNewService;
import com.MyProject.mediationplatformrcehandler.service.referential.GroupCCIALOrgService;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.service.utils.FileReader;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement;
import com.MyProject.mediationplatformrcehandler.utils.AccountMapper;
import com.MyProject.mediationplatformrcehandler.utils.Constants;
import java.util.Collections;
import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.MyProject.mediationplatformrcehandler.utils.TempFileFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class EstablishMapperTest {
    public static final String ID_18 = "Id18";
    public static final String RCE_ENT_ID = "00020554";
    private static AccountMapper accountMapper;

    @MockitoBean
    private  AccountService accountService;

    @MockitoBean private  RCEService rceService;
    @MockitoBean private  EtaMeOrgNewService etaMeOrgNewService;
    @MockitoBean private  GroupCCIALOrgService groupCCIALOrgService;

    @MockitoBean
    private  TechnicalTransaction technicalTransaction;

    private static List<EtaMeOrgNew> etaMeOrgNews = new ArrayList<>();

    private static List<RefRCE> refRCES = new ArrayList<>();

    private static List<EtaRefXL2> etaRefXL2s = new ArrayList<>();
    private static List<GroupCCIALOrg> groupCCIALOrgs = new ArrayList<>();
    private static List<GroupCCIALComp> groupCCIALComps = new ArrayList<>();

    private static List<GroupCCIALConstit> groupCCIALConstits = new ArrayList<>();

    private static List<EntMeOrg> entMeOrgs = new ArrayList<>();

    private static List<EntMeComp> entMeComps = new ArrayList<>();
    private static List<EntRefXL> entRefXLS = new ArrayList<>();

    private static List<EntScoringNoteort> entScoringNoteorts = new ArrayList<>();

    private static List<EntEcoNew> entEcoNews = new ArrayList<>();

    private static EstablishMapper establishMapper;

    @Autowired
    private ErrorHandler errorHandler;
    @MockitoBean
    private  CacheService cacheService;

    private final List<String> filteredEtablissements = new ArrayList<>();

    @TempDir
    private static File tempFile;
    @Value("#{${clinksplatform.customerlinks.rce.rce-segmentation-mapping}}")
    private HashMap<String, String> segmentationList;
    @Value("#{${clinksplatform.customerlinks.rce.rce-agence-list}}")
    private List<HashMap<String, String>> agenceList;
    @Value("#{${clinksplatform.customerlinks.rce.rce-edg-codes}}")
    private HashMap<String, String> edgCodes;
    @BeforeEach
     void init() throws IOException {
        File etaReferenceNewFile = new File(tempFile, "ETA-REF-XL2.txt");
        List<String> etaReferenceNewLines = getEtaReferenceNewFullData();
        Files.write(etaReferenceNewFile.toPath(), etaReferenceNewLines);

        File groupCCIALOrgFile = new File(tempFile, "GPE-CCIAL-ORG.txt");
        List<String> groupCCIALOrgLines = getGroupCCIALOrgFullData();
        Files.write(groupCCIALOrgFile.toPath(), groupCCIALOrgLines);

        File entReferenceNewFile = new File(tempFile, "ENT-REF-XL2.txt");
        List<String> entReferenceNewLines = getEntReferenceNewFullData();
        Files.write(entReferenceNewFile.toPath(), entReferenceNewLines);

        groupCCIALOrgs = FileReader.parseFile(groupCCIALOrgFile.toPath().toString(), FilesManagement.TypeManagement.SUNDAY, GroupCCIALOrg.class);
        groupCCIALComps = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-COMP.txt", GroupCCIALComp.class);
        groupCCIALConstits = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-CONSTIT.txt", GroupCCIALConstit.class);
        entMeOrgs = FileReader.parseFile("src/test/resources/rce/full/ENT-ME-ORG.txt", EntMeOrg.class);
        entMeComps = FileReader.parseFile("src/test/resources/rce/full/ENT-ME-COMP.txt", EntMeComp.class);
        entRefXLS = FileReader.parseFile(entReferenceNewFile.toPath().toString(), FilesManagement.TypeManagement.SUNDAY, EntRefXL.class);
        refRCES = FileReader.parseFile( "src/test/resources/rce/full/REF-RCE.txt",RefRCE.class);
        entScoringNoteorts = FileReader.parseFile("src/test/resources/rce/full/ENT-SCORING-NOTEORT.txt", EntScoringNoteort.class);
        etaRefXL2s = FileReader.parseFile(etaReferenceNewFile.toPath().toString(), FilesManagement.TypeManagement.SUNDAY, EtaRefXL2.class);
        etaMeOrgNews = FileReader.parseFile("src/test/resources/rce/full/ETA-ME-ORG-NEW.txt", EtaMeOrgNew.class);
        accountMapper = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap(), cacheService, groupCCIALOrgService);
    }

    @BeforeEach
    void initModifiedList() throws IOException {
        groupCCIALConstits = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-CONSTIT.txt", GroupCCIALConstit.class);
        accountMapper = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        establishMapper = new EstablishMapper(accountMapper, rceService, errorHandler, etaMeOrgNewService);
        for (var etab : accountMapper.etaRefXL2s()) {
            etab.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));
        }
        errorHandler.reset();
    }

    @Test
    @DisplayName("Map RCE objects to CL establishment must return OK")
    void mapEstablishmentRCEObjectToCL_must_return_ok() {
        when(rceService.getAccountByRceID(eq("Etablissement"), any())).thenReturn(Mono.empty());
        when(rceService.getAccountByRceID(eq("Entreprise"), any()))
                .thenReturn(Mono.just(getBody(null)));
        when(rceService.getIdUserByAlias(anyString())).thenReturn(createUsersList());

        var expectedRceId = etaRefXL2s.get(0).getIdETA();

        var result = establishMapper.mapEstablishmentToCL(null);

        assertFalse(result.isEmpty());

        var etablissement = result.stream().filter(e -> e.getRceID().equals(expectedRceId)).findAny().orElse(new Establishment());

        assertEquals(etablissement.getName(), etaRefXL2s.get(0).getEnseigne());
        assertEquals(etablissement.getAccountRecordType(), "012E0000000RR6wIAG");
        assertEquals(etablissement.getRceID(), expectedRceId);
        assertEquals(etablissement.getRceAccountType(), "Etablissement");
        assertEquals(etablissement.getSirenID(), etaRefXL2s.get(0).getSiren());
        assertEquals(etablissement.getAccountCurrency(), "EUR");
        assertEquals(etablissement.getAccountSource(), "RCE_France");
        assertEquals(etablissement.getHeadQuarterAccount(), true);
        assertEquals(etablissement.getStreet(), etaRefXL2s.get(0).getL4Normalisee() + System.lineSeparator() +
                    etaRefXL2s.get(0).getL3Normalisee());
        assertEquals(etablissement.getCity(), etaRefXL2s.get(0).getLibcom());
        assertEquals(etablissement.getPostalCode(), etaRefXL2s.get(0).getPostalCode());
        assertEquals(etablissement.getCountry(), "FRANCE");
        assertEquals(etablissement.getInactive(), false);
        assertEquals(etablissement.getCodeEDG(), "EBR");
        assertEquals(etablissement.getLabelEDG(), "DGC PUBLIC");
        assertEquals(etablissement.getNic(), etaRefXL2s.get(0).getNic());
        assertEquals(etablissement.getSiret(), etaRefXL2s.get(0).getSiren() + etaRefXL2s.get(0).getNic());
        assertEquals(etablissement.getDecisionDegree(), "T");
        assertEquals(etablissement.getDecisionDegreeLabel(), "Non decideur");
        Assertions.assertThat(etaRefXL2s)
            .extracting("idENT")
            .doesNotContain("00024239");
    }

    @Test
    @DisplayName("On Establishment creation/update, parentId18 must be set")
     void parentIdEtablissementtOk() {

        when(rceService.getAccountByRceID(eq("Entreprise"), any()))
                .thenReturn(Mono.just(getBody(null))); // check for parentId in CL
        when(rceService.getAccountByRceID(eq("Etablissement"), any())).thenReturn(Mono.empty());
        when(rceService.getIdUserByAlias(anyString())).thenReturn(createUsersList());
        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());

        var result = establishMapper.mapEstablishmentToCL(null);

        assertFalse(result.isEmpty());
        var ent = result.get(0);
        assertEquals(ID_18, ent.getParentAccount());
        verify(rceService, times(2)).getAccountByRceID("Entreprise", "00000735");
        verify(rceService, times(2)).getAccountByRceID("Entreprise", "00021752");
        verify(rceService, times(9)).getAccountByRceID(eq("Entreprise"), any());
    }

    @Test
    @DisplayName("when parse Establish with name is null return error ")
     void parseEstablishWithNameNullMustReturnError() {
        EtaRefXL2 etaReferenceNews = new EtaRefXL2();

        etaReferenceNews.setIdETA("12345678");
        etaReferenceNews.setEnseigne(null);
        etaReferenceNews.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, List.of(etaReferenceNews), entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        EstablishMapper establishMapper1 = new EstablishMapper(accountMapperTest, rceService, errorHandler, etaMeOrgNewService);

        establishMapper1.mapEstablishmentToCL(filteredEtablissements);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(etaReferenceNews.getIdETA()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ENTREPRISE &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("name can not be null for this establishment : " + etaReferenceNews.getIdETA()))));
        assertEquals(1, errorHandler.getErrors().size());

    }

    @Test
    @DisplayName("when parse Establish with name is empty return error ")
     void parseEstablishWithBlankNameMustReturnError() {
        EtaRefXL2 etaReferenceNews = new EtaRefXL2();

        etaReferenceNews.setIdETA("12345678");
        etaReferenceNews.setEnseigne("");
        etaReferenceNews.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, List.of(etaReferenceNews), entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        EstablishMapper establishMapper1 = new EstablishMapper(accountMapperTest, rceService, errorHandler, etaMeOrgNewService);

        establishMapper1.mapEstablishmentToCL(filteredEtablissements);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(etaReferenceNews.getIdETA()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ENTREPRISE &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("name can not be null for this establishment : " + etaReferenceNews.getIdETA()))));
        assertEquals(1, errorHandler.getErrors().size());

    }

    @Test
    @DisplayName("when parse Establish without ID return error ")
     void parseEstablishWithIDNullMustReturnError() {

        EtaRefXL2 etaReferenceNews = new EtaRefXL2();

        etaReferenceNews.setIdETA(null);
        etaReferenceNews.setEnseigne("test");
        etaReferenceNews.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, List.of(etaReferenceNews), entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        EstablishMapper establishMapperTest = new EstablishMapper(accountMapperTest, rceService, errorHandler, etaMeOrgNewService);

        establishMapperTest.mapEstablishmentToCL(filteredEtablissements);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(etaReferenceNews.getIdETA()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ETABLISSEMENT &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("id can not be null for this establishment : " + etaReferenceNews.getEnseigne()))));
        assertEquals(1, errorHandler.getErrors().size());

    }

    @Test
    @DisplayName("when parse Establish with blank ID return error ")
     void parseEstablishWithBlankIDMustReturnError() {

        EtaRefXL2 etaReferenceNews = new EtaRefXL2();

        etaReferenceNews.setIdETA("");
        etaReferenceNews.setEnseigne("test");
        etaReferenceNews.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, List.of(etaReferenceNews), entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        EstablishMapper establishMapperTest = new EstablishMapper(accountMapperTest, rceService, errorHandler, etaMeOrgNewService);

        establishMapperTest.mapEstablishmentToCL(filteredEtablissements);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(etaReferenceNews.getIdETA()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ETABLISSEMENT &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("id can not be null for this establishment : " + etaReferenceNews.getEnseigne()))));
        assertEquals(1, errorHandler.getErrors().size());

    }

    private Body<Account> getBody(String parentId) {
        var acc = new Account(new Attributes(), ID_18, "Name", "Type", parentId, "RCE_France", "OwnerId", "CodeEdg");
        return new Body<>(true, Arrays.asList(acc));
    }

    private UsersList createUsersList() {
        UsersList userList = new UsersList();
        UserRecord userRecord = new UserRecord();
        userRecord.setId("005E0000005hTeDIAU");
        userList.setRecords(List.of(userRecord));
        return userList;
    }
}