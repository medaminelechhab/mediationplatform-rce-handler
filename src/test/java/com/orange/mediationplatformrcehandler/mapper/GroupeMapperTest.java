package com.MyProject.mediationplatformrcehandler.mapper;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.UserRecord;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.UsersList;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.model.rce.*;
import com.MyProject.mediationplatformrcehandler.service.CacheService;
import com.MyProject.mediationplatformrcehandler.service.referential.EntRefXL2Service;
import com.MyProject.mediationplatformrcehandler.service.referential.EtaRefXL2Service;
import com.MyProject.mediationplatformrcehandler.service.referential.GroupCCIALOrgService;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.service.utils.FileReader;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement;
import com.MyProject.mediationplatformrcehandler.utils.AccountMapper;
import com.MyProject.mediationplatformrcehandler.utils.Constants;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.MyProject.mediationplatformrcehandler.utils.TempFileFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class GroupeMapperTest {
    public static final String ID_18 = "Id18";
    private static AccountMapper accountMapper;
    private static GroupeMapper groupeMapper;

    @MockitoBean
    private RCEService rceService;


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

    @Autowired
    private ErrorHandler errorHandler;
    @MockitoBean
    private CacheService cacheService;

    @MockitoBean
    private EntRefXL2Service entRefXl2Service;
    @MockitoBean
    private EtaRefXL2Service etaRefXl2Service;
    @MockitoBean
    private GroupCCIALOrgService groupCCIALOrgService;

    @TempDir
    private static File tempFile;
    @Value("#{${clinksplatform.customerlinks.rce.rce-segmentation-mapping}}")
    private HashMap<String, String> segmentationList;
    @Value("#{${clinksplatform.customerlinks.rce.rce-agence-list}}")
    private List<HashMap<String, String>> agenceList;
    @Value("#{${clinksplatform.customerlinks.rce.rce-edg-codes}}")
    private HashMap<String, String> edgCodes;

    private final List<String> filteredGroups = List.of("049060");

    @BeforeEach
    public void init() throws IOException {
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
        refRCES = FileReader.parseFile("src/test/resources/rce/full/REF-RCE.txt", RefRCE.class);
        entScoringNoteorts = FileReader.parseFile("src/test/resources/rce/full/ENT-SCORING-NOTEORT.txt", EntScoringNoteort.class);
        etaRefXL2s = FileReader.parseFile(etaReferenceNewFile.toPath().toString(), FilesManagement.TypeManagement.SUNDAY, EtaRefXL2.class);
        etaMeOrgNews = FileReader.parseFile("src/test/resources/rce/full/ETA-ME-ORG-NEW.txt", EtaMeOrgNew.class);
        accountMapper = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap(), cacheService, groupCCIALOrgService);
    }

    @BeforeEach
    void initModifiedList() throws IOException {
        groupCCIALConstits = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-CONSTIT.txt", GroupCCIALConstit.class);
        accountMapper = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        groupeMapper = new GroupeMapper(accountMapper, rceService, errorHandler, entRefXl2Service, etaRefXl2Service);
        errorHandler.reset();

        //Case when no entRefXL is in file but is in DB
        EntRefXL entRefXL = new EntRefXL();
        entRefXL.setSiren("Siren0000");
        when(entRefXl2Service.getByIdEnt("90024238")).thenReturn(entRefXL);

        EtaRefXL2 etaRefXL2 = new EtaRefXL2();
        when(etaRefXl2Service.getHqByIdENT(anyString())).thenReturn(etaRefXL2);

        for (var etab : accountMapper.groupCCIALOrgs()) {
            etab.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));
        }
    }

    @DisplayName("Map RCE objects to CL Group")
    @Test
    void mapAllGroupObject() {
        when(rceService.getAccountByRceID(anyString(), anyString())).thenReturn(Mono.empty());
        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getIdUserByAlias(anyString())).thenReturn(createUsersList());

        var groupes = groupeMapper.mapGroupToCL(new ArrayList<>());

        assertFalse(groupes.isEmpty());
        var clGroup1 = groupes.stream().filter(e -> e.getRceID().equals("049060")).toList().get(0);
        assertEquals("213501158", clGroup1.getSirenID());
        assertEquals("EUR", clGroup1.getAccountCurrency());
        assertEquals("RCE_France", clGroup1.getAccountSource());
        assertFalse(clGroup1.getInactive());
        assertEquals("EAM", clGroup1.getCodeEDG());
        assertEquals("Dir Ciale GO OA", clGroup1.getLabelEDG());
        assertEquals("V", clGroup1.getCodeSegNat());
        assertEquals("MdM SPE", clGroup1.getRceSegmentationNational());
        assertEquals("V", clGroup1.getCodeSegLoc());
        assertEquals("MdM SPE", clGroup1.getRceSegmentationLocal());
        assertEquals("Commune et commune nouvelle", clGroup1.getLegalStructure());
        assertEquals("O ADMINISTRATION PUBLIQUE", clGroup1.getApen31());
        assertEquals("8411Z Administration publique generale", clGroup1.getNaf());
        assertEquals("00017", clGroup1.getNic());
        assertEquals(clGroup1.getStreet(), "2  RUE PORTE ST-LEONARD" + System.lineSeparator());
        assertEquals("35300", clGroup1.getPostalCode());
        assertEquals("FRANCE", clGroup1.getCountry());
        assertNull(clGroup1.getSuppressionDate());
        assertNull(clGroup1.getDetachmentDate());
        assertEquals("A-Administrations & vie publique", clGroup1.getNationalMacroSectorization());
        assertEquals("A2-Collectivites locales", clGroup1.getNationalMicroSectorization());
        assertEquals("A1-Administration publique generale", clGroup1.getLocalMicroSectorization());
        Assertions.assertThat(groupes)
                .extracting("rceID")
                .doesNotContain("095433");
    }

    @DisplayName("Map filtered RCE objects to CL Group")
    @Test
    void mapGroupObject() {
        when(rceService.getAccountByRceID(anyString(), anyString())).thenReturn(Mono.empty());
        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getIdUserByAlias(anyString())).thenReturn(createUsersList());

        var groupes = groupeMapper.mapGroupToCL(filteredGroups);

        assertFalse(groupes.isEmpty());
        var clGroup1 = groupes.get(0);
        assertEquals("049060", clGroup1.getRceID());
        assertEquals("213501158", clGroup1.getSirenID());
        assertEquals("EUR", clGroup1.getAccountCurrency());
        assertEquals("RCE_France", clGroup1.getAccountSource());
        assertFalse(clGroup1.getInactive());
        assertEquals("EAM", clGroup1.getCodeEDG());
        assertEquals("Dir Ciale GO OA", clGroup1.getLabelEDG());
        assertEquals("V", clGroup1.getCodeSegNat());
        assertEquals("MdM SPE", clGroup1.getRceSegmentationNational());
        assertEquals("V", clGroup1.getCodeSegLoc());
        assertEquals("MdM SPE", clGroup1.getRceSegmentationLocal());
        assertEquals("Commune et commune nouvelle", clGroup1.getLegalStructure());
        assertEquals("O ADMINISTRATION PUBLIQUE", clGroup1.getApen31());
        assertEquals("8411Z Administration publique generale", clGroup1.getNaf());
        assertEquals("00017", clGroup1.getNic());
        assertEquals(clGroup1.getStreet(), "2  RUE PORTE ST-LEONARD" + System.lineSeparator());
        assertEquals("35300", clGroup1.getPostalCode());
        assertEquals("FRANCE", clGroup1.getCountry());
        assertNull(clGroup1.getSuppressionDate());
        assertNull(clGroup1.getDetachmentDate());
        assertEquals("A-Administrations & vie publique", clGroup1.getNationalMacroSectorization());
        assertEquals("A2-Collectivites locales", clGroup1.getNationalMicroSectorization());
        assertEquals("A1-Administration publique generale", clGroup1.getLocalMicroSectorization());
        Assertions.assertThat(groupes)
                .extracting("rceID")
                .doesNotContain("095433");
    }

    @DisplayName("Verify the account owner")
    @Test
    void verifyAccountOwner() {
        when(rceService.getAccountByRceID(anyString(), anyString())).thenReturn(Mono.empty());
        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getIdUserByAlias(anyString())).thenReturn(createUsersList());
        when(rceService.getUserById(anyString())).thenReturn(createUsersList());

        var groupes = groupeMapper.mapGroupToCL(null);

        assertEquals(5, groupes.size());

        assertEquals(3, groupes.stream().filter(g -> g.getAccountOwner().equals("005E0000005hTeDIAU")).count());
        assertEquals(2, groupes.stream().filter(g -> g.getAccountOwner().equals("0051v000009mGAUAA2")).count());
    }

    @Test
    @DisplayName("when parse group with name is null return error ")
    void parseGroupWithNameNullMustReturnError() {
        GroupCCIALOrg groupCCIALOrg = new GroupCCIALOrg();

        groupCCIALOrg.setIdGroup("12345678");
        groupCCIALOrg.setNameGroup(null);
        groupCCIALOrg.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(List.of(groupCCIALOrg), groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        GroupeMapper groupeMapperTest = new GroupeMapper(accountMapperTest, rceService, errorHandler, entRefXl2Service, etaRefXl2Service);

        groupeMapperTest.mapGroupToCL(List.of(groupCCIALOrg.getIdGroup()));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(groupCCIALOrg.getIdGroup()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ENTREPRISE &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("name can not be null for this group : " + groupCCIALOrg.getIdGroup()))));
        assertEquals(1, errorHandler.getErrors().size());

    }

    @Test
    @DisplayName("when parse group with name is empty or null return error ")
    void parseGroupWithBlankNameMustReturnError() {
        GroupCCIALOrg groupCCIALOrg = new GroupCCIALOrg();

        groupCCIALOrg.setIdGroup("12345678");
        groupCCIALOrg.setNameGroup("");
        groupCCIALOrg.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(List.of(groupCCIALOrg), groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        GroupeMapper groupeMapperTest = new GroupeMapper(accountMapperTest, rceService, errorHandler, entRefXl2Service, etaRefXl2Service);

        groupeMapperTest.mapGroupToCL(null);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(groupCCIALOrg.getIdGroup()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ENTREPRISE &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("name can not be null for this group : " + groupCCIALOrg.getIdGroup()))));
        assertEquals(1, errorHandler.getErrors().size());

    }

    @Test
    @DisplayName("when parse group without ID return error ")
    void parseGroupWithIDNullMustReturnError() {

        GroupCCIALOrg groupCCIALOrg = new GroupCCIALOrg();

        groupCCIALOrg.setIdGroup(null);
        groupCCIALOrg.setNameGroup("test");
        groupCCIALOrg.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(List.of(groupCCIALOrg), groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        GroupeMapper groupeMapperTest = new GroupeMapper(accountMapperTest, rceService, errorHandler, entRefXl2Service, etaRefXl2Service);

        groupeMapperTest.mapGroupToCL(null);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(groupCCIALOrg.getIdGroup()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ENTREPRISE &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("id can not be null for this group : " + groupCCIALOrg.getNameGroup()))));
        assertEquals(1, errorHandler.getErrors().size());

    }

    @Test
    @DisplayName("when parse group with blank ID return error ")
    void parseGroupWithBlankIDMustReturnError() {

        GroupCCIALOrg groupCCIALOrg = new GroupCCIALOrg();

        groupCCIALOrg.setIdGroup("");
        groupCCIALOrg.setNameGroup("test");
        groupCCIALOrg.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(List.of(groupCCIALOrg), groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        GroupeMapper groupeMapperTest = new GroupeMapper(accountMapperTest, rceService, errorHandler, entRefXl2Service, etaRefXl2Service);

        groupeMapperTest.mapGroupToCL(null);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(groupCCIALOrg.getIdGroup()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ENTREPRISE &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("id can not be null for this group : " + groupCCIALOrg.getNameGroup()))));
        assertEquals(1, errorHandler.getErrors().size());
    }

    private UsersList createUsersList() {
        UsersList userList = new UsersList();
        UserRecord userRecord = new UserRecord();
        userRecord.setId("005E0000005hTeDIAU");
        userList.setRecords(List.of(userRecord));
        return userList;
    }
}