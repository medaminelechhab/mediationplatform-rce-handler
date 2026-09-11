package com.MyProject.mediationplatformrcehandler.model.rce;

import com.MyProject.mediationplatformrcehandler.service.utils.FileReader;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.MyProject.mediationplatformrcehandler.utils.TempFileFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
class FilesParserTest {

    @TempDir
    File tempFile;
    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    void testParseEntEcoNew() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/ENT-ECO-NEW.txt");

        List<EntEcoNew> objects = FileReader.parseFile(csvPath.toString(), EntEcoNew.class);

        assertEquals(10, objects.size());
        assertEquals(EntEcoNew.class, objects.get(0).getClass());
        assertEquals("00000566", objects.get(0).getId());
        assertEquals("80", objects.get(0).getCa());
        assertEquals("0", objects.get(0).getCaExp());
        assertEquals("1", objects.get(0).getEffectif());
        assertEquals("20141231", objects.get(0).getDateBilan());
        assertEquals("12", objects.get(0).getDurationExercice());
        assertEquals("20151124", objects.get(0).getModificationDate());
    }

    @Test
    void testParseEntEcoNewWIthQuote() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/ENT-ECO-NEW_WIthQuote.txt");

        List<EntEcoNew> objects = FileReader.parseFile(csvPath.toString(), EntEcoNew.class);

        assertEquals(2, objects.size());
        assertEquals(EntEcoNew.class, objects.get(0).getClass());
        assertEquals("00000566", objects.get(0).getId());
        assertEquals("80", objects.get(0).getCa());
        assertEquals("0", objects.get(0).getCaExp());
        assertEquals("1", objects.get(0).getEffectif());
        assertEquals("20141231", objects.get(0).getDateBilan());
        assertEquals("12", objects.get(0).getDurationExercice());
        assertEquals("20151124", objects.get(0).getModificationDate());
    }

    @Test
    void testParseEntMeComp() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/ENT-ME-COMP.txt");

        List<EntMeComp> objects = FileReader.parseFile(csvPath.toString(), EntMeComp.class);

        assertEquals(14, objects.size());
        assertEquals(EntMeComp.class, objects.get(0).getClass());
        assertEquals("00000154", objects.get(0).getId());
        assertEquals("F", objects.get(0).getCodeMacroSec());
        assertEquals("F4", objects.get(0).getCodeMicroSec());
        assertEquals("F4", objects.get(0).getCodeMicroSecLoc());
        assertEquals("WA", objects.get(0).getCodeSegemntationDCE());
        assertEquals("WA", objects.get(0).getCodeSegemntationDCELoc());
        assertEquals("20210305", objects.get(0).getModificationDate());
    }

    @Test
    void testParseEntMeOrg() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/ENT-ME-ORG.txt");

        List<EntMeOrg> objects = FileReader.parseFile(csvPath.toString(), EntMeOrg.class);

        assertEquals(14, objects.size());
        assertEquals(EntMeOrg.class, objects.get(0).getClass());
        assertEquals("00000001", objects.get(0).getIdEnt());
        assertEquals("EN", objects.get(0).getCodeSegmentCCial());
        assertEquals("20230217", objects.get(0).getDattachement());
        assertEquals("GP", objects.get(0).getMotifattachement());
        assertEquals("", objects.get(0).getDDetachement());
        assertEquals("", objects.get(0).getMotifDetachement());
        assertEquals("017526", objects.get(0).getCodeEDSVente());
        assertEquals("EAYF01", objects.get(0).getAliasEDSVente());
        assertEquals("20230221", objects.get(0).getModificationDate());
    }

    @Test
    void testParseEntReferenceNew() throws IOException {

        File file = new File(tempFile, "ENT-REFERENCE-NEW.txt");
        List<String> lines = getEntReferenceNewData();
        Files.write(file.toPath(), lines);

        List<EntRefXL> objects = FileReader.parseFile(file.toPath().toString(), FilesManagement.TypeManagement.DAILY, EntRefXL.class);

        assertEquals(2, objects.size());
        assertEquals(EntRefXL.class, objects.get(0).getClass());
        assertEquals("00034981", objects.get(0).getId());
        assertEquals("19951111", objects.get(0).getDReferencement());
        assertEquals("CR", objects.get(0).getMotifReferencement());
        assertEquals("", objects.get(0).getDDReferencement());
        assertEquals("", objects.get(0).getMotifDeReferencement());
        assertEquals("XXXXX", objects.get(0).getCodPaysSiege());
        assertEquals("N", objects.get(0).getEnCoursImmat());
        assertEquals(now.format(formatter), objects.get(0).getModificationDate());
        assertEquals("", objects.get(0).getStatusInsee());
        assertEquals("", objects.get(0).getSiren());
        assertEquals("DIR. CENTRALE SERVICE ESSENCES DES ARMEE", objects.get(0).getNomrs());
        assertEquals("DCSEA", objects.get(0).getSigle());
        assertEquals("", objects.get(0).getCivility());
        assertEquals("N", objects.get(0).getIndPersphys());
        assertEquals("9900", objects.get(0).getCodeCatj());
        assertEquals("Z", objects.get(0).getCodNaf31());
        assertEquals("0000Z", objects.get(0).getCodNaf());
        assertEquals("11", objects.get(0).getMonthCreationEnt());
        assertEquals("1995", objects.get(0).getYearCreationEnt());
        assertEquals("", objects.get(0).getTefen());
        assertEquals("", objects.get(0).getEfencent());
        assertEquals("", objects.get(0).getDefen());
        assertEquals("", objects.get(0).getRecme());
        assertEquals("", objects.get(0).getMonoAct());
        assertEquals("", objects.get(0).getRegimp());
        assertEquals("", objects.get(0).getMonoreg());
        Assertions.assertThat(objects)
            .extracting("id")
            .contains("00034981", "00221867")
            .doesNotContain("00245783");
    }

    @Test
    void testParseEntScoringNoteort() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/ENT-SCORING-NOTEORT.txt");

        List<EntScoringNoteort> objects = FileReader.parseFile(csvPath.toString(), EntScoringNoteort.class);

        assertEquals(20, objects.size());
        assertEquals(EntScoringNoteort.class, objects.get(0).getClass());
        assertEquals("00000076", objects.get(0).getId());
        assertEquals("1", objects.get(0).getNoteScoring());
        assertEquals("", objects.get(0).getDeffJugement());
        assertEquals("12", objects.get(0).getNoteORT());
        assertEquals("20210705", objects.get(0).getModificationDate());
    }

    @Test
    void testParseEtaMeOrgNew() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/ETA-ME-ORG-NEW.txt");

        List<EtaMeOrgNew> objects = FileReader.parseFile(csvPath.toString(), EtaMeOrgNew.class);

        assertEquals(8, objects.size());
        assertEquals(EtaMeOrgNew.class, objects.get(0).getClass());
        assertEquals("00020554", objects.get(0).getId());
        assertEquals("T", objects.get(0).getCodeDGDEC());
        assertEquals("EAY", objects.get(0).getCodeAGGEO());
        assertEquals("025592", objects.get(0).getCodeEDSVente());
        assertEquals("EBR010", objects.get(0).getAliasEDSVente());
        assertEquals("19930416", objects.get(0).getDattachment());
        assertEquals("CR", objects.get(0).getMotifAttachement());
        assertEquals("", objects.get(0).getDDattachment());
        assertEquals("", objects.get(0).getMotifDetachment());
        assertEquals("20130124", objects.get(0).getModificationDate());
        //assertEquals("", objects.get(0).getIdETA());
    }


    @Test
    void testParseEtaReferenceNew() throws IOException {

        File file = new File(tempFile, "ETA-REF-XL2.txt");
        List<String> lines = getEtaReferenceNewData();
        Files.write(file.toPath(), lines);

        List<EtaRefXL2> objects = FileReader.parseFile(file.toPath().toString(), FilesManagement.TypeManagement.DAILY, EtaRefXL2.class);

        assertEquals(2, objects.size());
        assertEquals(EtaRefXL2.class, objects.get(0).getClass());
        assertEquals("00021752", objects.get(0).getIdENT());
        assertEquals("00823921", objects.get(0).getIdETA());
        assertEquals("20021123", objects.get(0).getDReferencement());
        assertEquals("CR", objects.get(0).getMotifReferencement());
        assertEquals("", objects.get(0).getDDReferencement());
        assertEquals("", objects.get(0).getMotifDeReferencement());
        assertEquals(now.format(formatter), objects.get(0).getModificationDate());
        assertEquals("ACTI", objects.get(0).getStatutINSEE());
        assertEquals("178104311", objects.get(0).getSiren());
        assertEquals("00265", objects.get(0).getNic());
        assertEquals("N", objects.get(0).getSiege());
        assertEquals("IEN 1ER DEGRE MAZAMET", objects.get(0).getEnseigne());
        assertEquals("27", objects.get(0).getNumStreet());
        assertEquals("", objects.get(0).getIndrep());
        assertEquals("RUE", objects.get(0).getTypeStreet());
        assertEquals("MEYER", objects.get(0).getLabelStreet());
        assertEquals("81200", objects.get(0).getPostalCode());
        assertEquals("", objects.get(0).getStreetCode());
        assertEquals("MAZAMET", objects.get(0).getLibcom());
        assertEquals("XXXXX", objects.get(0).getCountryCode());
        assertEquals("76", objects.get(0).getRpet());
        assertEquals("81", objects.get(0).getDepet());
        assertEquals("163", objects.get(0).getComet());
        assertEquals("DIRECTION DES SERVICES DEPARTEMENTAUX", objects.get(0).getL1Normalisee());
        assertEquals("", objects.get(0).getL2Normalisee());
        assertEquals("", objects.get(0).getL3Normalisee());
        assertEquals("27 Rue MEYER", objects.get(0).getL4Normalisee());
        assertEquals("", objects.get(0).getL5Normalisee());
        assertEquals("81200 MAZAMET", objects.get(0).getL6Normalisee());
        assertEquals("FRANCE", objects.get(0).getL7Normalisee());
        assertEquals("O", objects.get(0).getCodeNAF31());
        assertEquals("8412Z", objects.get(0).getCodeNaf());
        assertEquals("03", objects.get(0).getTefet());
        assertEquals("8", objects.get(0).getEfetcent());
        assertEquals("2019", objects.get(0).getDefet());
        assertEquals("", objects.get(0).getOriginCode());
        assertEquals("20011005", objects.get(0).getDcret());
        assertEquals("", objects.get(0).getActivNat());
        assertEquals("", objects.get(0).getLieuAct());
        assertEquals("", objects.get(0).getSaisonNat());
        Assertions.assertThat(objects)
            .extracting("idENT")
            .contains("00021752", "00018963")
            .doesNotContain("00162613");

    }

    @Test
    void testParseRefRCE() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/REF-RCE.txt");

        List<RefRCE> objects = FileReader.parseFile(csvPath.toString(), RefRCE.class);

        assertEquals(41832, objects.size());
        assertEquals(RefRCE.class, objects.get(0).getClass());
        assertEquals("ASSO_GRPT", objects.get(0).getReferenceName());
        assertEquals("F", objects.get(0).getCode());
        assertEquals("Federation", objects.get(0).getLabel());
        assertEquals("20210324", objects.get(0).getModificationDate());
        assertEquals("", objects.get(0).getDeletionDate());
    }

    @Test
    void testParseGroupCCIALComp() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/GPE-CCIAL-COMP.txt");

        List<GroupCCIALComp> objects = FileReader.parseFile(csvPath.toString(), GroupCCIALComp.class);

        assertEquals(6, objects.size());
        assertEquals(GroupCCIALComp.class, objects.get(0).getClass());
        assertEquals("000143", objects.get(0).getIdGroup());
        assertEquals("Y", objects.get(0).getSectorMacroCode());
        assertEquals("Y1", objects.get(0).getSectorMicroCode());
        assertEquals("F4", objects.get(0).getSectorMicroCodeLoc());
        assertEquals("K", objects.get(0).getSegmentationDCECode());
        assertEquals("K", objects.get(0).getSegmentationDCECodeLoc());
        assertEquals("", objects.get(0).getReserve());
        assertEquals("20230110", objects.get(0).getModificationDate());
    }

    @Test
    void testParseGroupCCIALConstit() throws IOException {
        Path csvPath = Paths.get("src/test/resources/rce/full/GPE-CCIAL-CONSTIT.txt");

        List<GroupCCIALConstit> objects = FileReader.parseFile(csvPath.toString(), GroupCCIALConstit.class);

        assertEquals(4, objects.size());
        assertEquals(GroupCCIALConstit.class, objects.get(0).getClass());
        assertEquals("049060", objects.get(0).getIdGroup());
        assertEquals("20180719", objects.get(0).getModificationDate());
        assertEquals("00020554", objects.get(0).getIdENT());
    }

    // filter
    @Test
    void testParseGroupCCIALOrg() throws IOException {

        File file = new File(tempFile, "GPE-CCIAL-ORG.txt");
        List<String> lines = getGroupCCIALOrgData();
        Files.write(file.toPath(), lines);

        List<GroupCCIALOrg> objects = FileReader.parseFile(file.toPath().toString(), FilesManagement.TypeManagement.DAILY, GroupCCIALOrg.class);

        assertEquals(2, objects.size());
        assertEquals(GroupCCIALOrg.class, objects.get(0).getClass());
        assertEquals("049262", objects.get(0).getIdGroup());
        assertEquals("ANTARGAZ", objects.get(0).getNameGroup());
        assertEquals("010238", objects.get(0).getCodeEDSVente());
        assertEquals("EAJR39", objects.get(0).getAliasEDSVente());
        assertEquals("00000269", objects.get(0).getIdEntMm());
        assertEquals(now.format(formatter), objects.get(0).getModificationDate());
        assertEquals("20040929", objects.get(0).getCreationDate());
        assertEquals("", objects.get(0).getDeletionDate());
        Assertions.assertThat(objects)
            .extracting("idGroup")
            .contains("049262", "094121")
            .doesNotContain("036164");
    }

    @Test
    void verifyModificationDateForEntReferenceNew() {
        EntRefXL entRefXL = new EntRefXL();
        List<EntRefXL> listToCompare = createEntReferences();
        assertEquals(3, listToCompare.size());

        List<EntRefXL> dailyFilteredList = FileReader.filterByMode(listToCompare, FilesManagement.TypeManagement.DAILY);
        assertEquals(1, dailyFilteredList.size());
        assertEquals("id1", dailyFilteredList.get(0).getId());

        List<EntRefXL> sundayFilteredList = FileReader.filterByMode(listToCompare, FilesManagement.TypeManagement.SUNDAY);
        assertEquals(2, sundayFilteredList.size());
        assertEquals("id1", sundayFilteredList.get(0).getId());
        assertEquals("id3", sundayFilteredList.get(1).getId());
    }

    private List<EntRefXL> createEntReferences() {
        LocalDate before = LocalDate.now().minusDays(5);

        List<EntRefXL> list = new ArrayList<EntRefXL>();
        list.add(createEntReference("id1", now.format(formatter)));
        list.add(createEntReference("id2", "20220704"));
        list.add(createEntReference("id3", before.format(formatter)));
        return list;
    }

    private EntRefXL createEntReference(String id, String modificationDate) {
        EntRefXL entRefXL = new EntRefXL();
        entRefXL.setId(id);
        entRefXL.setModificationDate(modificationDate);
        return entRefXL;
    }

}