package com.MyProject.mediationplatformrcehandler.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class TempFileFactory {
    private static LocalDate now = LocalDate.now();
    private static LocalDate before = LocalDate.now().minusDays(6);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static List<String> getEtaReferenceNewData() {
        return Arrays.asList("00021752|00823921|20021123|CR|||"+ now.format(formatter) +"|ACTI|178104311|00265|N|IEN 1ER DEGRE MAZAMET|27||RUE|MEYER|81200|||MAZAMET|XXXXX|76|81|163|2|99|81|4|01||41||DIRECTION DES SERVICES DEPARTEMENTAUX|||27 Rue MEYER||81200 MAZAMET|FRANCE|O|8412Z|03|8|2019||20011005|||||||O||||||||",
                "00018963|01083904|20040612||||" + now.format(formatter) + "|ACTI|356000117|23778|N|PLATEFORME DISTRIBUTION COURRIER 1|   2||RUE|PIERRE BROSSOLETTE|78220|1035|VIROFLAY|XXXXX|11|78|686|LA POSTE|PLATEFORME DISTRIBUTION COURRIER 1||2 RUE PIERRE BROSSOLETTE|VIROFLAY|78221 viroflay cedex||H|5310Z|11|10|2011|1|052004|NR|99|P|0|",
                "00162613|00718744|20020131|CR|||20230913|INAC|407569490|00029|N|SPELOG - EDULOG|  15||CRS|ALEXANDRE BORODINE|26000|0073|VALENCE|XXXXX|82|26|362|AXESS TECHNOLOGY|SPELOG - EDULOG|LOT-MOZART|15 CRS ALEXANDRE BORODINE||26000 VALENCE||M|7112B|03|     6|2004|NR|122000||99|P|0|");
    }

    public static List<String> getEntReferenceNewData() {
        return Arrays.asList("00034981|19951111|CR|||XXXXX|N|" + now.format(formatter) + "|||DIR. CENTRALE SERVICE ESSENCES DES ARMEE|DCSEA||||N|9900|Z|0000Z|||11|1995|||||||||||||||",
            "00221867|20030227|CR|||XXXXX|N|" + now.format(formatter) + "||383127768|LEVY FRANCOIS||1|||Y|1800|K|6420Z|||07|1991|||||||||||||||",
            "00245783|20040207||||XXXXX|N|20230913|ACTI|722023728|GROUPE MORGAN SERVICES|||||N|5710|N|7820Z|19720101||01|1972|31|225|2020|||||ETI|||||N||");
    }

    public static List<String> getGroupCCIALOrgData() {
        return Arrays.asList("049262|ANTARGAZ|010238|EAJR39|00000269|" + now.format(formatter) + "|20040929|",
            "094121|AKERS FRANCE|017703|EBJF61|00000305|" + now.format(formatter) + "|20090327|");
    }

    public static List<String> getRefRceData() {
        return Arrays.asList("ASSO_GRPT|F|Fédération|20210324|",
                "ASSO_GRPT|S|Simple|20210324|");
    }

    public static List<String> getEtaMeOrgNewData() {
        return Arrays.asList("00000001|T|EAY|017526|EAYF01|20230217|GP|||20230223|");
    }

    public static List<String> getEntMeOrgData() {
        return Arrays.asList("00000001|EN|20230217|GP|||017526|EAYF01|20230221");
    }

    public static List<String> getGroupCCIALCompData() {
        return Arrays.asList("049262|ANTARGAZ|010238|EAJR39|00000269|" + now.format(formatter) + "|20040929|",
                "094121|AKERS FRANCE|017703|EBJF61|00000305|" + now.format(formatter) + "|20090327|",
                "036164|SOTIS|010199|EAXR34|00000370|20230913|20040209|");
    }

    public static List<String> getEntMeCompData() {
        return Arrays.asList("00000154|F|F4|F4|WA|WA|20210305");
    }

    public static List<String> getGroupeCcialConstitData() {
        return Arrays.asList("053986|20230110|00000195");
    }

    public static List<String> getEntScoringNoteortData() {
        return Arrays.asList("00000076|1||12|20210705");
    }

    public static List<String> getEntEcoNewData() {
        return Arrays.asList("00000566|80|0|1|20141231|12|20151124");
    }

    public static List<String> getEtaReferenceNewFullData() {
        return Arrays.asList(
            "00021752|00823921|20021123|CR|||"+ now.format(formatter) +"|ACTI|178104311|00265|Y|IEN 1ER DEGRE MAZAMET|27||RUE|MEYER|81200|||MAZAMET|XXXXX|76|81|163|2|99|81|4|01||41||DIRECTION DES SERVICES DEPARTEMENTAUX|||27 Rue MEYER||81200 MAZAMET|FRANCE|O|8412Z|03|8|2019||20011005|||||||O||||||||",
            "00000735|00020554|19930416|CR|||" + now.format(formatter) + "|ACTI|054806542|00954|Y|SOCIETE MARSEILLAISE DE CREDIT|  29||AV|CARNOT|06500|0300|MENTON|XXXXX|93|06|083|SOCIETE MARSEILLAISE DE CREDIT|SMC||29 AV CARNOT||06500 MENTON||K|6419Z|02|3|2011||011900|NR|99|P|0|",
            "00020554|00211493|19930609|CR|||" + before.format(formatter) + "|ACTI|213501158|00017|Y|COMMUNE DE FOUGERES|2||RUE|PORTE ST-LEONARD|35300|35301|1520|FOUGERES|XXXXX|53|35|115|1|97|35|4|01||42||COMMUNE DE FOUGERES|COMMUNE DE FOUGERES||2  RUE PORTE ST-LEONARD|35301|35300 FOUGERES cedex ||O|8411Z|32|375|2020||19830301|||||||O||||||||",
            "00008499|00011461|20020131|CR|||" + now.format(formatter) + "|ACTI|353276637|00017|Y|R.B.S.I.|||RUE|DE LA GARE|57385||TETING-SUR-NIED|XXXXX|44|57|668|R.B.S.I.|||Rue DE LA GARE||57385 TETING-SUR-NIED|FRANCE|C|2219Z|12|35|2020||111989|||||",
            "00064841|00904892|20030412|CR|||" + before.format(formatter) + "|ACTI|383474814|00068|Y|CENTRE DE FORMATION|5||RUE|GABRIEL CLERC|31700||BLAGNAC|XXXXX|76|31|069|AIRBUS|||5 Rue GABRIEL CLERC||31700 BLAGNAC|FRANCE|P|8559B|32|375|2020||012002|||||",
            "00024238|10731417|20101009|CR|||" + now.format(formatter) + "|ACTI|319488409|00503|Y|T-SYSTEMS FRANCE|110||RUE|AMBROISE CROIZAT|93200||SAINT-DENIS|XXXXX|11|93|066|T-SYSTEMS FRANCE|T SYSTEMS||110 Rue AMBROISE CROIZAT||93200 SAINT-DENIS|FRANCE|J|6201Z|00|1|2019||102010|||||",
            "90024238|10731417|20101009|CR|||" + now.format(formatter) + "|ACTI|319488409|00503|Y|T-SYSTEMS FRANCE|110||RUE|AMBROISE CROIZAT|93200||SAINT-DENIS|XXXXX|11|93|066|T-SYSTEMS FRANCE|T SYSTEMS||110 Rue AMBROISE CROIZAT||93200 SAINT-DENIS|FRANCE|J|6201Z|00|1|2019||102010|||||",
            "00024239|10731418|20101009|CR|||20230812|INAC|319488409|00503|Y|T-SYSTEMS FRANCE|110||RUE|AMBROISE CROIZAT|93200||SAINT-DENIS|XXXXX|11|93|066|T-SYSTEMS FRANCE|T SYSTEMS||110 Rue AMBROISE CROIZAT||93200 SAINT-DENIS|FRANCE|J|6201Z|00|1|2019||102010|||||",
            "90024238|10731419|20101009|CR|||20230812|INAC|319488409|00503|Y|T-SYSTEMS FRANCE|110||RUE|AMBROISE CROIZAT|93200||SAINT-DENIS|XXXXX|11|93|066|T-SYSTEMS FRANCE|T SYSTEMS||110 Rue AMBROISE CROIZAT||93200 SAINT-DENIS|FRANCE|J|6201Z|00|1|2019||102010|||||");
    }

    public static List<String> getEntReferenceNewFullData() {
        return Arrays.asList("00020554|19930416|CR|||XXXXX|Y|" + now.format(formatter) + "|ACTI|213501158|COMMUNE DE FOUGERES|||||N|7210|O|8411Z|19830301||03|1983|32|375|2020|||||ETI|||||N||",
            "00008499|20020131|CR|||XXXXX|N|" + before.format(formatter) + "|ACTI|353276637|R.B.S.I.|||||N|5710|C|2219Z|19891101||11|1989|12|35|2020|||||PME|||||N||",
            "00064841|20010507|CR|||XXXXX|N|" + now.format(formatter) + "|ACTI|383474814|AIRBUS|||||N|5710|C|3030Z|19911018||10|1991|52|7500|2020|||||GE|||||N||",
            "00024238|19930416|CR|||XXXXX|N|" + before.format(formatter) + "|ACTI|319488409|T-SYSTEMS FRANCE|||||N|5710|J|6203Z|19800815||08|1980|22|150|2020|||||ETI|||||N||",
            "00024239|19930416|CR|||XXXXX|N|20230812|ACTI|319488409|T-SYSTEMS FRANCE|||||N|5710|J|6203Z|19800815||08|1980|22|150|2020|||||ETI|||||N||",
            "90024238|19930416|CR|||XXXXX|N|20230812|ACTI|319488409|T-SYSTEMS FRANCE|||||N|5710|J|6203Z|19800815||08|1980|22|150|2020|||||ETI|||||N||");
    }

    public static List<String> getGroupCCIALOrgFullData() {
        return Arrays.asList("049060|VILLE DE FOUGERES|010207|EAMR21|00020554|" + now.format(formatter) + "|20040927|",
            "090732|R.B.S.I.|026615|CFXFX2|00008499|" + now.format(formatter) + "|20080117|",
            "000064|AIRBUS|025523|EBR013|00064841|" + before.format(formatter) + "|19950920|",
            "095432|T-SYSTEMS|012818|RLCAB1|00024238|" + now.format(formatter) + "|20100105|",
            "095432|T-SYSTEMS|012818|RLCAB1|90024238|" + now.format(formatter) + "|20100105|",
            "095433|T-SYSTEMS|012818|RLCAB1|00024238|20230812|20100105|");
    }
}