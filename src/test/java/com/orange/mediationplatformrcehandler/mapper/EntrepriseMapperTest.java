package com.MyProject.mediationplatformrcehandler.mapper;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.*;
import com.MyProject.mediationplatform.common.model.resiliency.technical.TechnicalTransaction;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.AccountService;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Entreprise;
import com.MyProject.mediationplatformrcehandler.model.rce.*;
import com.MyProject.mediationplatformrcehandler.service.CacheService;
import com.MyProject.mediationplatformrcehandler.service.referential.EtaRefXL2Service;
import com.MyProject.mediationplatformrcehandler.service.referential.GroupCCIALOrgService;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.service.utils.FileReader;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement;
import com.MyProject.mediationplatformrcehandler.service.utils.StatsService;
import com.MyProject.mediationplatformrcehandler.utils.AccountMapper;
import com.MyProject.mediationplatformrcehandler.utils.Constants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
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
import java.util.*;

import static com.MyProject.mediationplatformrcehandler.utils.TempFileFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class EntrepriseMapperTest {
    public static final String ID_18 = "Id18";
    public static final String RCE_ENT_ID = "00020554";
    private static AccountMapper accountMapper;

    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private RCEService rceService;
    @MockitoBean
    private EtaRefXL2Service etaRefXl2Service;
    @MockitoBean
    private GroupCCIALOrgService groupCCIALOrgService;
    @Autowired
    private ErrorHandler errorHandler;
    @MockitoBean
    private CacheService cacheService;
    @MockitoBean
    private TechnicalTransaction technicalTransaction;
    @TempDir
    private static File tempFile;
    @Mock
    private StatsService statsService;


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
    private static EntrepriseMapper entrepriseMapper;
    private final List<String> filteredEntreprises = new ArrayList<>();

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
        refRCES = FileReader.parseFile("src/test/resources/rce/full/REF-RCE.txt", RefRCE.class);
        entScoringNoteorts = FileReader.parseFile("src/test/resources/rce/full/ENT-SCORING-NOTEORT.txt", EntScoringNoteort.class);
        etaRefXL2s = FileReader.parseFile(etaReferenceNewFile.toPath().toString(), FilesManagement.TypeManagement.SUNDAY, EtaRefXL2.class);
        etaMeOrgNews = FileReader.parseFile("src/test/resources/rce/full/ETA-ME-ORG-NEW.txt", EtaMeOrgNew.class);
        accountMapper = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap(), cacheService, groupCCIALOrgService);
        groupCCIALConstits = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-CONSTIT.txt", GroupCCIALConstit.class);
        accountMapper = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        entrepriseMapper = new EntrepriseMapper(accountMapper, rceService, errorHandler, etaRefXl2Service);
        for (var ent : entRefXLS) {
            ent.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));
        }
        errorHandler.reset();
    }
    @Test
    @DisplayName("Should map basic enterprise data correctly")
    void shouldMapBasicEnterpriseData() {
        when(rceService.getAccountByRceID(any(), any()))
                .thenReturn(Mono.just(getBody("ParentId")));
        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getUserById(anyString())).thenReturn(createUsersList());

        var result = entrepriseMapper.mapEnterpriseToCL(null);

        assertFalse(result.isEmpty());
        var ent = result.stream().filter(e -> e.getRceID().equals("00020554")).findAny().orElse(new Entreprise());

        assertEquals(entRefXLS.get(0).getNomrs(), ent.getName());
        assertEquals("012E0000000RR6wIAG", ent.getAccountRecordType());
        assertEquals("00020554", ent.getRceID());
        assertEquals("Entreprise", ent.getRceAccountType());
    }
    @Test
    @DisplayName("Should map sectorization fields correctly")
    void shouldMapSectorizationFields() {
        var ent = getMappedEnterprise(); // extract common logic to avoid repetition

        assertEquals("A-Administrations & vie publique", ent.getNationalMacroSectorization());
        assertEquals("A2-Collectivites locales", ent.getNationalMicroSectorization());
        assertEquals("A1-Administration publique generale", ent.getLocalMicroSectorization());
    }

    @Test
    @DisplayName("Should map enterprise identification data")
    void shouldMapEnterpriseIdentification() {
        var ent = getMappedEnterprise();

        assertAll("Enterprise identification",
                () -> assertEquals(entRefXLS.get(0).getSiren(), ent.getSirenID()),
                () -> assertEquals(entRefXLS.get(0).getSiren(), ent.getGroupSirenID()),
                () -> assertEquals("EUR", ent.getAccountCurrency()),
                () -> assertEquals("RCE_France", ent.getAccountSource()),
                () -> assertFalse(ent.getInactive())
        );
    }

    @Test
    @DisplayName("On Enterprise creation,when group exist in RCE, parentId18 must be set")
    void parentIdtOk() {
        when(rceService.getAccountByRceID(eq("Entreprise"), any()))
                .thenReturn(Mono.just(new Body<>())); //check if creation or update

        when(rceService.getAccountByRceID(eq("Groupe"), any()))
                .thenReturn(Mono.just(getBody(null))); //check for parent information with rce groupID

        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());

        var result = entrepriseMapper.mapEnterpriseToCL(filteredEntreprises);

        assertFalse(result.isEmpty());
        var entNoParent = result.stream().filter(e -> e.getRceID().equals("00024238")).findAny().orElse(new Entreprise());
        var entWithParent = result.stream().filter(e -> e.getRceID().equals("00020554")).findAny().orElse(new Entreprise());
        assertEquals(ID_18, entWithParent.getParentAccount());
        assertNull(entNoParent.getParentAccount());
    }

    /*
     * 1 check si ent.parentId == null dans CL
     * 2 check si ent a 1 parentId maintenant avec les fichier RCE
     * */
    @Test
    @DisplayName("when Enterprise have a group after the update, ParentId18 must set")
    void UpdateParentIdtcase1_2() {
        when(rceService.getAccountByRceID(eq("Entreprise"), eq(RCE_ENT_ID)))//NOSONAR
                .thenReturn(Mono.just(getBody("ParentId")));

        when(rceService.getAccountByRceID(eq("Groupe"), any()))
                .thenReturn(Mono.just(getBody("ParentId")));

        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getUserById(anyString())).thenReturn(createUsersList());

        var result = entrepriseMapper.mapEnterpriseToCL(filteredEntreprises);

        assertFalse(result.isEmpty());
        var ent = result.get(0);

        assertEquals(ID_18, ent.getParentAccount());
    }


    /*
     * 1 Check if the enterprise is present in Clinks with its RCE_id
     * 2 For above RCE_id, check its ParentId.
     * 3 check AccountSource for this ParentId
     * */

    @Test
    @DisplayName("when Enterprise change group in RCE, check parentId18 updated")
    void UpdateParentIdtcase2_changeGroup() {
        var updateGroup = getBody("XXX");
        updateGroup.getRecords().get(0).setId("UpdatedParentId18");

        when(rceService.getAccountByRceID(eq("Entreprise"), eq(RCE_ENT_ID)))//NOSONAR
                .thenReturn(Mono.just(getBody("ParentId")));

        when(rceService.getAccountByRceID(eq("Groupe"), any()))
                .thenReturn(Mono.just(updateGroup));

        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getUserById(anyString())).thenReturn(createUsersList());

        //entreprise should not have groupe in RCE

        var result = entrepriseMapper.mapEnterpriseToCL(filteredEntreprises);

        assertEquals(1, result.size());
        var ent = result.get(0);

        assertEquals("UpdatedParentId18", ent.getParentAccount());
    }

    /*
     * 1 Check if the enterprise is present in Clinks with its RCE_id
     * 2 For above RCE_id, check its ParentId.
     * 3 check AccountSource for this ParentId
     * */
    @Test
    @DisplayName("when Enterprise have a group that is not an RCE_France group ")
    void UpdateParentIdtcase2_2() {
        var updateGroup = getBody("XXX");
        updateGroup.getRecords().get(0).setId("UpdatedParentId18");
        updateGroup.getRecords().get(0).setAccountSource("Other");

        when(rceService.getAccountByRceID(eq("Entreprise"), eq(RCE_ENT_ID)))//NOSONAR
                .thenReturn(Mono.just(getBody("ParentId")));

        when(rceService.getAccountByRceID(eq("Groupe"), any()))
                .thenReturn(Mono.just(updateGroup));

        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getUserById(anyString())).thenReturn(createUsersList());

        //entreprise should not have groupe in RCE

        var result = entrepriseMapper.mapEnterpriseToCL(filteredEntreprises);

        assertEquals(1, result.size());
        var ent = result.get(0);

        assertEquals(null, ent.getParentAccount());
    }

    @Test
    @DisplayName("when parse Enterprise with name is null return error ")
    void parseEntrepriseWithoutNameMustReturnError() {
        EntRefXL entRefXLWithoutName = new EntRefXL();

        entRefXLWithoutName.setId("12345678");
        entRefXLWithoutName.setNomrs(null);
        entRefXLWithoutName.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, List.of(entRefXLWithoutName), refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        EntrepriseMapper entrepriseMapperTest = new EntrepriseMapper(accountMapperTest, rceService, errorHandler, etaRefXl2Service);

        entrepriseMapperTest.mapEnterpriseToCL(filteredEntreprises);
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(entRefXLWithoutName.getId()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ENTREPRISE &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("name can not be null for this entreprise : " + entRefXLWithoutName.getId()))));
        assertEquals(1, errorHandler.getErrors().size());
    }

    @Test
    @DisplayName("when parse Enterprise with name is empty return error ")
    void parseEntrepriseWithBlankNameMustReturnError() {
        EntRefXL entRefXLWithoutName = new EntRefXL();

        entRefXLWithoutName.setId("12345678");
        entRefXLWithoutName.setNomrs("");
        entRefXLWithoutName.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));

        AccountMapper accountMapperTest = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, List.of(entRefXLWithoutName), refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        EntrepriseMapper entrepriseMapperTest = new EntrepriseMapper(accountMapperTest, rceService, errorHandler, etaRefXl2Service);

        entrepriseMapperTest.mapEnterpriseToCL(filteredEntreprises);
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> verify(errorHandler).add(argThat(errorDto ->
                errorDto.getRceId().equals(entRefXLWithoutName.getId()) &&
                        errorDto.getClinksId() == null &&
                        errorDto.getType() == ErrorDTO.AccountType.ENTREPRISE &&
                        errorDto.getErrorCode().equals("400") &&
                        errorDto.getErrorMessage().equals("name can not be null for this entreprise : " + entRefXLWithoutName.getId()))));
        assertEquals(1, errorHandler.getErrors().size());
    }

    private Body<Account> getBody(String parentId) {
        var acc = new Account(new Attributes(), ID_18, "Name", "Type", parentId, "RCE_France", "OwnerId", "codeEdg");
        return new Body<>(true, Arrays.asList(acc));
    }

    private UsersList createUsersList() {
        UsersList userList = new UsersList();
        UserRecord userRecord = new UserRecord();
        userRecord.setId("005E0000005hTeDIAU");
        userList.setRecords(List.of(userRecord));
        return userList;
    }
    private Entreprise getMappedEnterprise() {
        when(rceService.getAccountByRceID(any(), any()))
                .thenReturn(Mono.just(getBody("ParentId")));
        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getUserById(anyString())).thenReturn(createUsersList());

        var result = entrepriseMapper.mapEnterpriseToCL(null);
        return result.stream().filter(e -> e.getRceID().equals("00020554")).findAny().orElse(new Entreprise());
    }

}
