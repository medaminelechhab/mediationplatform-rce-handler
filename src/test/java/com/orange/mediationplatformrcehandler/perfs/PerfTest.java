package com.MyProject.mediationplatformrcehandler.perfs;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.*;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.AccountService;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.mapper.EntrepriseMapper;
import com.MyProject.mediationplatformrcehandler.mapper.EstablishMapper;
import com.MyProject.mediationplatformrcehandler.mapper.GroupeMapper;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Entreprise;
import com.MyProject.mediationplatformrcehandler.model.rce.*;
import com.MyProject.mediationplatformrcehandler.service.CacheService;
import com.MyProject.mediationplatformrcehandler.service.referential.EntRefXL2Service;
import com.MyProject.mediationplatformrcehandler.service.referential.EtaMeOrgNewService;
import com.MyProject.mediationplatformrcehandler.service.referential.EtaRefXL2Service;
import com.MyProject.mediationplatformrcehandler.service.referential.GroupCCIALOrgService;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.service.utils.FileReader;
import com.MyProject.mediationplatformrcehandler.service.utils.StatsService;
import com.MyProject.mediationplatformrcehandler.utils.AccountMapper;
import com.MyProject.mediationplatformrcehandler.utils.Constants;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.test.context.junit.jupiter.EnabledIf;
import reactor.core.publisher.Mono;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@SpringBootTest
@ActiveProfiles({"perf", "db", "account", "rce", "clinks", "tx"})
@EnabledIf(expression = "#{environment['spring.profiles.active'] == 'perf'}", reason = "Enabled only in perf profile")
class PerfTest {
    public static final String ID_18 = "Id18";
    public static final String RCE_ENT_ID = "00020554";
    private static AccountMapper accountMapper;

    @MockitoBean
    private static AccountService accountService;
    @MockitoBean private static RCEService rceService;
    @MockitoBean private static EtaRefXL2Service etaRefXl2Service;
    @MockitoBean private static EntRefXL2Service entRefXl2Service;
    @MockitoBean private static EtaMeOrgNewService etaMeOrgNewService;
    @MockitoBean private static GroupCCIALOrgService groupCCIALOrgService;

    @Autowired
    private  ErrorHandler errorHandler;
    @MockitoBean
    private static CacheService cacheService;

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
    private static GroupeMapper groupMapper;
    private static EstablishMapper establishMapper;
    @Value("#{${clinksplatform.customerlinks.rce.rce-segmentation-mapping}}")
    private HashMap<String, String> segmentationList;
    @Value("#{${clinksplatform.customerlinks.rce.rce-agence-list}}")
    private List<HashMap<String, String>> agenceList;
    @Value("#{${clinksplatform.customerlinks.rce.rce-edg-codes}}")
    private HashMap<String, String> edgCodes;

    private final List<String> filteredEntreprises = new ArrayList<>();

    @TempDir
    private static File tempFile;

    @Mock private StatsService statsService;

    @BeforeAll
    static void init() throws IOException {
        int numberOfLines = 1_000; // Specify the number of lines to generate
        String groupTemplateLine = "_ID_|VILLE DE FOUGERES|010207|EAMR21|_IDENT_|20120706|20040927|\n";
        String entTemplateLine = "_ID_|19930416|CR|||XXXXX|N|20210504|ACTI|213501158|COMMUNE DE FOUGERES|||||Y|7210|O|8411Z|19830301||03|1983|32|375|2020|||||ETI|||||N||\n";
        String etaTemplateLine = "_IDENT_|_ID_|20021123|CR|||20050206|ACTI|178104311|00265|Y|IEN 1ER DEGRE MAZAMET|27||RUE|MEYER|81200|||MAZAMET|XXXXX|76|81|163|2|99|81|4|01||41||DIRECTION DES SERVICES DEPARTEMENTAUX|||27 Rue MEYER||81200 MAZAMET|FRANCE|O|8412Z|03|8|2019||20011005|||||||O||||||||\n";
        String entMeOrgTemplateLine = "_ID_|EN|20230217|GP|||017526|EAYF01|20230221\n";
        String etaMeOrgTemplateLine = "_ID_|T|EAY|025592|EBR010|19930416|CR|||20130124|\n\n";
        File etaReferenceNewFile = new File(tempFile, "ETA-REF-XL2.txt");
        File etaMeOrgFile = new File(tempFile, "ETA-ME-ORG-NEW.txt");
        File entMeOrgFile = new File(tempFile, "ENT-ME-ORG.txt");
        File groupCCIALOrgFile = new File(tempFile, "GPE-CCIAL-ORG.txt");
        File entReferenceNewFile = new File(tempFile, "ENT-REF-XL2.txt");
        createBigFile(etaMeOrgFile, numberOfLines, etaMeOrgTemplateLine);
        createBigFile(entMeOrgFile, numberOfLines, entMeOrgTemplateLine);
        createBigFile(groupCCIALOrgFile, numberOfLines, groupTemplateLine);
        createBigFile(entReferenceNewFile, numberOfLines, entTemplateLine);
        createBigFile(etaReferenceNewFile, numberOfLines, etaTemplateLine);

        groupCCIALOrgs = (List<GroupCCIALOrg>) FileReader.parseBigFile(groupCCIALOrgFile.toPath().toString(), GroupCCIALOrg.class);
        entRefXLS = (List<EntRefXL>) FileReader.parseBigFile(entReferenceNewFile.toPath().toString(), EntRefXL.class);
        etaRefXL2s = (List<EtaRefXL2>) FileReader.parseBigFile(etaReferenceNewFile.toPath().toString(), EtaRefXL2.class);
        entMeOrgs = (List<EntMeOrg>) FileReader.parseBigFile(entMeOrgFile.toPath().toString(), EntMeOrg.class);
        etaMeOrgNews = (List<EtaMeOrgNew>) FileReader.parseBigFile(etaMeOrgFile.toPath().toString(), EtaMeOrgNew.class);
        groupCCIALComps = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-COMP.txt", GroupCCIALComp.class);
        groupCCIALConstits = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-CONSTIT.txt", GroupCCIALConstit.class);
        entMeComps = FileReader.parseFile("src/test/resources/rce/full/ENT-ME-COMP.txt", EntMeComp.class);
        refRCES = FileReader.parseFile( "src/test/resources/rce/full/REF-RCE.txt",RefRCE.class);
        entScoringNoteorts = FileReader.parseFile("src/test/resources/rce/full/ENT-SCORING-NOTEORT.txt", EntScoringNoteort.class);
        accountMapper = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, Collections.emptyMap(), Collections.emptyList() , Collections.emptyMap(), cacheService, groupCCIALOrgService);
    }

    private static void createBigFile(File entReferenceNewFile, int numberOfLines, String templateLine) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(entReferenceNewFile))) {
            for (int i = 0; i < numberOfLines; i++) {
                String uniqueId = String.format("%08d", i + 1); // Generate unique ID
                String uniqueId2 = String.format("%08d", i + 1); // Generate unique ID
                String line = templateLine.replace("_ID_", uniqueId).replace("_IDENT_", uniqueId2); // Replace _ID_ with the unique ID
                writer.write(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create big file");
        }
    }

    @BeforeEach
    void initModifiedList() throws IOException {
        groupCCIALConstits = FileReader.parseFile("src/test/resources/rce/full/GPE-CCIAL-CONSTIT.txt", GroupCCIALConstit.class);
        accountMapper = new AccountMapper(groupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps, entRefXLS, refRCES, entScoringNoteorts, etaRefXL2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);
        entrepriseMapper = new EntrepriseMapper(accountMapper, rceService, errorHandler, etaRefXl2Service);
        establishMapper = new EstablishMapper(accountMapper, rceService, errorHandler, etaMeOrgNewService);
        groupMapper = new GroupeMapper(accountMapper, rceService, errorHandler, entRefXl2Service, etaRefXl2Service);
        for (var ent : entRefXLS) {
            ent.setModificationDate(LocalDate.now().format(Constants.DF_YYYYMMDD));
        }
        errorHandler.reset();
    }

    @Test
    @DisplayName("Try to map rce objects to CL Objects")
     void getRCEObjects_mustReturnCLEntrepriseObjects() {

        //Given 1
        when(rceService.getAccountByRceID(eq("Entreprise"), eq("00000001")))
                .thenReturn(Mono.just(new Body<>())); //check if creation or update
        when(rceService.getAccountByRceID(eq("Entreprise"), eq("00000002")))
                .thenReturn(Mono.just(new Body<>())); //check if creation or update
        when(rceService.getAccountByRceID(eq("Groupe"), any()))
                .thenReturn(Mono.just(getBody(null))); //check for parent information with rce groupID
        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());

        //Given 2
        when(rceService.getAccountByRceID(any(), any()))
                .thenReturn(Mono.just(getBody("ParentId")));
        when(rceService.getIdUserByUsername(anyString())).thenReturn(createUsersList());
        when(rceService.getUserById(anyString())).thenReturn(createUsersList());

        //When
        var mappedGroups = groupMapper.mapGroupToCL(null);
        var mappedEntreprises = entrepriseMapper.mapEnterpriseToCL(null);
        var mappedEtablissements = establishMapper.mapEstablishmentToCL(null);

        //Then 1
        assertFalse(mappedEntreprises.isEmpty());
        assertFalse(mappedGroups.isEmpty());
        assertFalse(mappedEtablissements.isEmpty());

        var entNoParent = mappedEntreprises.stream().filter(e -> e.getRceID().equals("00000001")).findAny().orElse(new Entreprise());
        var entWithParent = mappedEntreprises.stream().filter(e -> e.getRceID().equals("00000002")).findAny().orElse(new Entreprise());
        assertEquals(ID_18, entWithParent.getParentAccount());
        assertNull(entNoParent.getParentAccount());

        //Then 2
        var ent = mappedEntreprises.stream().filter(e -> e.getRceID().equals("00000001")).findAny().orElse(new Entreprise());
        assertEquals(ent.getNationalMacroSectorization(), "A-Administrations & vie publique");
        assertEquals(ent.getNationalMicroSectorization(), "A2-Collectivites locales");
        assertEquals(ent.getLocalMicroSectorization(), "A1-Administration publique generale");

        assertEquals(ent.getName(), entRefXLS.get(0).getNomrs());
        assertEquals(ent.getAccountRecordType(), "012E0000000RR6wIAG");
        assertEquals(ent.getRceID(), "00000001");
        assertEquals(ent.getRceAccountType(), "Entreprise");
        assertEquals(ent.getSirenID(), entRefXLS.get(0).getSiren());
        assertEquals(ent.getGroupSirenID(), entRefXLS.get(0).getSiren());
        assertEquals(ent.getAccountCurrency(), "EUR");
        assertEquals(ent.getAccountSource(), "RCE_France");
        assertEquals(ent.getInactive(), false);
        assertEquals(ent.getCodeEDG(), "EAM");
        assertEquals(ent.getLabelEDG(), "Dir Ciale GO OA");
        assertEquals(ent.getNationalCodeSegment(), "V");
        assertEquals(ent.getRceSegmentationNational(), "MdM SPE");
        assertEquals(ent.getCodeSegLoc(), "V");
        assertEquals(ent.getLegalStructure(), "Commune et commune nouvelle");
        assertEquals(ent.getApen31(), "O ADMINISTRATION PUBLIQUE");
        assertEquals(ent.getNaf(), "8411Z Administration publique generale");
        assertEquals(ent.getScoreNote(), "2");
        assertEquals(ent.getScoreLabel(), "2 Risque modere (A surveiller)");
        assertNull(ent.getDetachmentDate());
        assertEquals(ent.getScoreLabel(), "2 Risque modere (A surveiller)");
        assertEquals(ent.getStreet(), "2  RUE PORTE ST-LEONARD" + System.lineSeparator());
        assertEquals(ent.getPostalCode(), "35300");
        assertEquals(ent.getCountry(), "FRANCE");
        assertEquals(ent.getCity(), "FOUGERES");
        Assertions.assertThat(entRefXLS)
                .extracting("id")
                .doesNotContain("00024239");
    }


    /*
     * 1 check si ent.parentId == null dans CL
     * 2 check si ent a 1 parentId maintenant avec les fichier RCE
     * */
    @Test
    @DisplayName("when Enterprise have a group after the update, ParentId18 must set")
     void UpdateParentIdtcase1_2() {

        when(rceService.getAccountByRceID(eq("Entreprise"), eq(RCE_ENT_ID)))
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

        var UpdatedGroup = getBody("XXX");
        UpdatedGroup.getRecords().get(0).setId("UpdatedParentId18");

        when(rceService.getAccountByRceID(eq("Entreprise"), eq(RCE_ENT_ID)))
                .thenReturn(Mono.just(getBody("ParentId")));

        when(rceService.getAccountByRceID(eq("Groupe"), any()))
                .thenReturn(Mono.just(UpdatedGroup));

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


        var UpdatedGroup = getBody("XXX");
        UpdatedGroup.getRecords().get(0).setId("UpdatedParentId18");
        UpdatedGroup.getRecords().get(0).setAccountSource("Other");

        when(rceService.getAccountByRceID(eq("Entreprise"), eq(RCE_ENT_ID)))
                .thenReturn(Mono.just(getBody("ParentId")));


        when(rceService.getAccountByRceID(eq("Groupe"), any()))
                .thenReturn(Mono.just(UpdatedGroup));

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
}