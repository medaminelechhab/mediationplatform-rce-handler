package com.MyProject.mediationplatformrcehandler.utils;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.Account;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Body;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.enums.AccountType;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Agence;
import com.MyProject.mediationplatformrcehandler.model.rce.*;
import com.MyProject.mediationplatformrcehandler.service.CacheService;
import com.MyProject.mediationplatformrcehandler.service.referential.GroupCCIALOrgService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

@Slf4j
public record AccountMapper(List<GroupCCIALOrg> groupCCIALOrgs,
                            List<GroupCCIALComp> groupCCIALComps,
                            List<GroupCCIALConstit> groupCCIALConstits,
                            List<EntMeOrg> entMeOrgs,
                            List<EntMeComp> entMeComps,
                            List<EntRefXL> entRefXLS,
                            List<RefRCE> refRCES,
                            List<EntScoringNoteort> entScoringNoteorts,
                            List<EtaRefXL2> etaRefXL2s,
                            List<EntEcoNew> entEcoNews,
                            List<EtaMeOrgNew> etaMeOrgNews,
                            RCEService rceService,
                            Map<String, String> segmentationList,
                            List<HashMap<String, String>> agenceList,
                            Map<String, String> edgCodes,
                            CacheService cacheService,
                            GroupCCIALOrgService groupCCIALOrgService
) {

    private static final String SEGMENTATION_DCE = "SEGMENTATION_DCE";
    private static final String INSEE_NAF = "INSEE_NAF";
    private static String virtualDEFUser = "Virtual_user DEF_";
    private static List<String> noNeedSmbCodeSegmentation = Arrays.asList("CFX", "EAJ", "EAF", "EAL", "ECP");
    private static List<Agence> agences = new ArrayList<>();
    private static String ownerIdForDIVOP = "0051v000009mGAUAA2";
    private static String ownerIdForDGC = "0051v000009nndRAAQ";
    private static String SUPER_QD = "SuperQD";

    public Optional<String> getParentIdOfEnterpriseFromGroupCCIALConstit(String rceId) {
        return groupCCIALConstits.stream()
                .filter(groupCCIALConstit -> groupCCIALConstit.getIdENT()!=null && groupCCIALConstit.getIdENT().equalsIgnoreCase(rceId))
                .map(GroupCCIALConstit::getIdGroup)
                .findFirst();
    }


    public Optional<String> getScoreLabel(String scoreNote) {
        return Optional.ofNullable(filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase("SCORING") && refRCE.getCode().equalsIgnoreCase(scoreNote))
                .orElse(new RefRCE())
                .getLabel());
    }

    public Optional<String> getScoreNote(String entrepriseID) {
        return entScoringNoteorts.stream()
                .filter(entMeComp -> entMeComp.getId()!=null && entMeComp.getId().equals(entrepriseID))
                .map(EntScoringNoteort::getNoteScoring).findFirst();
    }

    public Optional<String> getNAFEntreprise(EntRefXL entRefXL) {

        var label = filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase(INSEE_NAF) && refRCE.getCode().equalsIgnoreCase(entRefXL.getCodNaf()))
                .orElse(new RefRCE())
                .getLabel();
        return Optional.of(entRefXL.getCodNaf() + ' ' + label);
    }

    public Optional<String> getApen31Entreprise(EntRefXL entRefXL) {

        var label = filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase("INSEE_NAF31") && refRCE.getCode().equalsIgnoreCase(entRefXL.getCodNaf31()))
                .orElse(new RefRCE())
                .getLabel();
        return Optional.of(entRefXL.getCodNaf31() + ' ' + label);
    }

    public Optional<String> getLegalStructureEntreprise(String codeCatj) {
        return Optional.ofNullable(filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase("INSEE_CATEGORIE_JURIDIQUE")
                && refRCE.getCode().equalsIgnoreCase(codeCatj)).orElse(new RefRCE())
                .getLabel());
    }


    public Optional<String> getRceSegmentationLocalEntreprise(String localCodeSegment) {
        return Optional.ofNullable(filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase(SEGMENTATION_DCE)
                && refRCE.getCode().equalsIgnoreCase(localCodeSegment)).orElse(new RefRCE())
                .getLabel());
    }

    public Optional<String> getCodeSegLocEntreprise(String entrepriseID) {

        return entMeComps.stream()
                .filter(entMeComp -> entMeComp.getId()!=null && entMeComp.getId().equals(entrepriseID))
                .map(EntMeComp::getCodeSegemntationDCELoc).findFirst();
    }

    public Optional<String> getRceSegmentationNationalEntreprise(String nationalCodeSegment) {

        return Optional.ofNullable(filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase(SEGMENTATION_DCE)
                && refRCE.getCode().equalsIgnoreCase(nationalCodeSegment)).orElse(new RefRCE())
                .getLabel());
    }

    public Optional<String> getNationalCodeSegmentEntreprise(String entrepriseID) {

        return entMeComps.stream()
                .filter(entMeComp -> entMeComp.getId()!=null && entMeComp.getId().equals(entrepriseID))
                .map(EntMeComp::getCodeSegemntationDCE).findFirst();

    }

    public Optional<String> getCodeEDGEntreprise(String entrepriseID) {

        return entMeOrgs.stream()
                .filter(entMeOrg -> entMeOrg.getIdEnt()!=null && entMeOrg.getIdEnt().equals(entrepriseID))
                .map(entreprise -> entreprise.getAliasEDSVente().substring(0, 3)).findFirst();

    }


    public boolean getEntrepriseStatus(String entrepriseID) {
        var insee = filterEntReferenceNew(e -> e.getId()!=null && e.getId().equalsIgnoreCase(entrepriseID))
                .map(EntRefXL::getStatusInsee);
        if (insee.isEmpty())
            return false;
        return checkInactive(insee.get());

    }

    public boolean headQuarterAccountValue(String siege) {
        return siege.equalsIgnoreCase("Y");

    }

    public Optional<EntMeComp> filterEntMeCompById(String entrepriseId) {
        return entMeComps.stream()
            .filter(e -> e.getId()!=null && e.getId().equalsIgnoreCase(entrepriseId))
            .findFirst();
    }

    public Optional<RefRCE> getSectorization(Constants.Sectorisation typeOfSectorization, Predicate<RefRCE> p) {
        return filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase(typeOfSectorization.toString()) && p.test(refRCE));
    }

    public Optional<LocalDate> getDateDetachment(EntRefXL entRefXl2) {
        var dateString = entMeOrgs.stream()
                .filter(entMeOrg -> entMeOrg.getIdEnt()!=null && entMeOrg.getIdEnt().equalsIgnoreCase(entRefXl2.getId()))
                .map(EntMeOrg::getDDetachement)
                .findFirst();


        return parseDate(dateString.orElse(null));
    }

    public Optional<LocalDate> parseDate(String dateString) {
        if (dateString == null || dateString.isBlank())
            return Optional.empty();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return Optional.of(date);
    }

    public String getBillingCountry(EtaRefXL2 etaRefXL2) {

        if (etaRefXL2!=null && etaRefXL2.getSiege()!=null && etaRefXL2.getSiege().equalsIgnoreCase("Y")) {
            return filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase("PAYS")
                && refRCE.getCode().equalsIgnoreCase(etaRefXL2.getCountryCode())).orElse(new RefRCE()).getLabel();
        }

        return null;

    }


    public String getBillingStreet(EtaRefXL2 etaRefXL2) {

        if (etaRefXL2!=null && etaRefXL2.getSiege()!=null && etaRefXL2.getSiege().equalsIgnoreCase("Y")) {
            StringBuilder builder = new StringBuilder();
            builder.append(etaRefXL2.getL4Normalisee())
                    .append(System.lineSeparator())
                    .append(etaRefXL2.getL3Normalisee());

            return builder.toString();
        }

        return null;
    }

    public Optional<EtaRefXL2> filterEtaReferenceNew(Predicate<EtaRefXL2> p) {
        return etaRefXL2s.stream()
                .filter(p)
                .findFirst();
    }

    public String getGroupeNic(EtaRefXL2 etaRefXL2) {

        if (etaRefXL2!=null && etaRefXL2.getSiege()!=null && etaRefXL2.getSiege().equalsIgnoreCase("Y")) {
            return etaRefXL2.getNic();
        } else {
            return null;
        }
    }

    public String getNaf(EntRefXL entRefXl) {

        var libelle = filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase(INSEE_NAF)
            && refRCE.getCode().equalsIgnoreCase(entRefXl.getCodNaf()));

        if (libelle.isEmpty() || libelle.get().getLabel()==null)
            return entRefXl.getCodNaf();

        return String.join(" ", entRefXl.getCodNaf(), libelle.get().getLabel());
    }

    public String getApen31(EntRefXL entRefXl) {
        var libelle = filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase("INSEE_NAF31")
            && refRCE.getCode().equalsIgnoreCase(entRefXl.getCodNaf31()));

        if (libelle.isEmpty() || libelle.get().getLabel()==null)
            return entRefXl.getCodNaf31();

        return String.join(" ", entRefXl.getCodNaf31(), libelle.get().getLabel());
    }

    public String getLegalStructure(EntRefXL entRefXl) {

        return filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase("INSEE_CATEGORIE_JURIDIQUE")
            && refRCE.getCode().equalsIgnoreCase(entRefXl.getCodeCatj()))
                .orElse(new RefRCE()).getLabel();
    }

    public Optional<String> getCodeSegNat(String groupeId) {
        return groupCCIALComps.stream()
            .filter(groupCCIALComp -> groupCCIALComp.getIdGroup()!=null && groupCCIALComp.getIdGroup().equalsIgnoreCase(groupeId))
            .map(GroupCCIALComp::getSegmentationDCECode)
            .findFirst();
    }

    public Optional<String> getRceSegmentationNational(String groupeId) {
        var groupCCIALComp = fillerGroupCCIALComp(o -> o.getIdGroup().equalsIgnoreCase(groupeId)).orElse(null);

        if (groupCCIALComp == null)
            return Optional.empty();
        return Optional.ofNullable(filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase(SEGMENTATION_DCE)
            && refRCE.getCode().equalsIgnoreCase(groupCCIALComp.getSegmentationDCECode()))
            .orElse(new RefRCE()).getLabel());
    }

    public Optional<String> getRceSegmentationLocal(String groupeId) {
        var groupCCIALComp = fillerGroupCCIALComp(o -> o.getIdGroup()!=null && o.getIdGroup().equalsIgnoreCase(groupeId))
            .orElse(null);

        if (groupCCIALComp == null)
            return Optional.empty();
        return Optional.ofNullable(filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase(SEGMENTATION_DCE)
            && refRCE.getCode().equalsIgnoreCase(groupCCIALComp.getSegmentationDCECodeLoc()))
            .orElse(new RefRCE()).getLabel());
    }


    public Optional<GroupCCIALComp> fillerGroupCCIALComp(Predicate<GroupCCIALComp> p) {
        return groupCCIALComps.stream().filter(p)
                .findFirst();
    }

    public Optional<String> getGroupEdgLabel(String codeEDG) {
        return Optional.ofNullable(filterRefRCE(refRCE -> refRCE.getReferenceName().equalsIgnoreCase("EDG_FT")
                && codeEDG.equalsIgnoreCase(refRCE.getCode()))
                .orElse(new RefRCE()).getLabel());
    }

    public Optional<String> getCodeEDG(String idETA) {

        return etaMeOrgNews.stream().filter(etaMeOrgNew -> etaMeOrgNew.getId()!=null && etaMeOrgNew.getId().equals(idETA))
                .map(establishment -> establishment.getAliasEDSVente().substring(0, 3)).findFirst();

    }


    public Optional<String> getDecisionDegree(String idETA) {
        return etaMeOrgNews.stream().
                filter(etaMeOrgNew -> etaMeOrgNew.getId()!=null && etaMeOrgNew.getId().equals(idETA)).
                map(EtaMeOrgNew::getCodeDGDEC).findFirst();
    }

    public Optional<String> getCountry(String postalCodeCountry) {
        return Optional.ofNullable(filterRefRCE(refRCE -> refRCE.getCode().equals(postalCodeCountry) && refRCE.getReferenceName().equals("PAYS"))
                .orElse(new RefRCE()).getLabel());

    }


    public Optional<String> getEdgLabel(String codeEDG) {
        return Optional.ofNullable(filterRefRCE(refRCE ->
                refRCE.getReferenceName().equalsIgnoreCase("EDG_FT")
                        && refRCE.getCode().equalsIgnoreCase(codeEDG))
                .orElse(new RefRCE()).getLabel());
    }


    public boolean getGroupStatus(EntRefXL entRefXl2) {

        var insee = entRefXl2.getStatusInsee();

        if (insee==null || insee.isEmpty())
            return false;

        return checkInactive(insee);
    }


    public Boolean getEstablishementStatus(String statusInsee) {

        return checkInactive(statusInsee);
    }

    private boolean checkInactive(String statusInsee) {
        return statusInsee.equalsIgnoreCase("INAC") || statusInsee.equalsIgnoreCase("RADI");
    }

    public Optional<String> handleDecisionDegreeLabel(String decisionDegree) {

        if (decisionDegree == null) {
            return Optional.empty();
        }

        return etaMeOrgNews.stream().map(etaMeOrgNew -> switch (decisionDegree) {
            case "D" -> "Totalement decideur";
            case "H" -> "Partiellement decideur";
            case "I" -> "Inconnu";
            case "T" -> "Non decideur";
            default -> throw new IllegalArgumentException("Invalid decision degree : " + decisionDegree);
        }).findFirst();
    }

    public String getAccountOwner(EtaRefXL2 etaRefXL2, EtaMeOrgNew etaMeOrgNew, Body<Account> account) {

        String ownerId = getOwnerId(account);

        //Récup des infos commerciales
        String aliasEDSVente = etaMeOrgNew.getAliasEDSVente();
        String codeEDS = aliasEDSVente.substring(0,3);
        Agence agence = getAgenceSmbCode(codeEDS);
        EntMeComp entMeComp = filterEntMeCompById(etaRefXL2.getIdENT()).orElse(new EntMeComp());

        boolean edgUnchanged = account != null && account.getRecords() != null
                    && !account.getRecords().isEmpty() && account.getRecords().get(0).getCodeEdg() != null
                    && account.getRecords().get(0).getCodeEdg().equals(codeEDS);

        if (ownerId != null && edgUnchanged){
            return ownerId;
        } else {
            return getAccountOwner(agence, codeEDS, entMeComp.getCodeSegemntationDCE(), etaRefXL2);
        }
    }

    public String getAccountOwner(EntRefXL entRefXL, Body<Account> account) {
        String ownerId = getOwnerId(account);

        //Initialisations
        String codeEDS = null;
        boolean edgUnchanged = false;
        EntMeComp entMeComp = null;
        Agence agence = null;

        //Chercher l'account dans entMeOrg pour des infos commerciales
        Optional<EntMeOrg> correspondingEntMeOrg = entMeOrgs.stream()
                .filter(entMeOrg -> entRefXL.getId().equalsIgnoreCase(entMeOrg.getIdEnt()))
                .findFirst();

        //Detrminer si le code EDG a changé
        if(correspondingEntMeOrg.isPresent()) {
            String aliasEDSVente = correspondingEntMeOrg.get().getAliasEDSVente();
            codeEDS = aliasEDSVente.substring(0, 3);
            agence = getAgenceSmbCode(codeEDS);
            entMeComp = filterEntMeCompById(entRefXL.getId()).orElse(new EntMeComp());

            edgUnchanged = account != null && account.getRecords() != null
                    && !account.getRecords().isEmpty() && account.getRecords().get(0).getCodeEdg() != null
                    && account.getRecords().get(0).getCodeEdg().equals(codeEDS);
        } else {
            log.error("Unable to get instance of EntMeOrg for entreprise : {} ", entRefXL.getId());
            throw new IllegalStateException("Unable to get instance of EntMeOrg for entreprise : " + entRefXL.getId());
        }

        //Si l'owner est pas virtual ET que le code EDG n'a pas changé -> on laisse le même owner que CL
        if (ownerId != null && edgUnchanged){
            return ownerId;
        } else {
            return getAccountOwner(agence, codeEDS, entMeComp.getCodeSegemntationDCE(), entRefXL);
        }
    }

    public String getAccountOwner(GroupCCIALOrg groupCCIALOrg, Body<Account> account) {
        String ownerId = getOwnerId(account);

        String aliasEDSVente = groupCCIALOrg.getAliasEDSVente();
        String codeEDS = aliasEDSVente.substring(0,3);
        Agence agence = getAgenceSmbCode(codeEDS);

        GroupCCIALComp groupComp = fillerGroupCCIALComp(groupCCIALComp -> groupCCIALComp.getIdGroup()!= null && groupCCIALComp.getIdGroup().equalsIgnoreCase(groupCCIALOrg.getIdGroup()))
                .orElse(new GroupCCIALComp());

        boolean edgUnchanged = account != null && account.getRecords() != null
                && !account.getRecords().isEmpty() && account.getRecords().get(0).getCodeEdg() != null
                && account.getRecords().get(0).getCodeEdg().equals(codeEDS);

        if (ownerId != null && edgUnchanged){
            return ownerId;
        } else {
        return getAccountOwner(agence, codeEDS, groupComp.getSegmentationDCECode(), groupCCIALOrg);
      }
    }

  private String getOwnerId(Body<Account> account) {
        if (account != null
            && account.getRecords() != null
            && !account.getRecords().isEmpty()) {
            return  account.getRecords().get(0).getOwnerId();
        }
        return null;
    }

    private <T> String getAccountOwner(Agence agence, String codeEDS, String segmentationCode, T obj) {
        switch (agence.getCommercialSegment()) {
            case "EN":

                // If code EDG is changed, calculate username and retrieve the account owner
                String smbCode = agence.getSmbCode();
                String segCode = getSegmentationSmbCode(segmentationCode);
                StringBuilder username = new StringBuilder();
                username.append(virtualDEFUser.concat(smbCode));

                if(!noNeedSmbCodeSegmentation.contains(codeEDS)) {
                    username.append("_".concat(segCode));
                }

                if (cacheService.containsUsername(username.toString())) {
                    return cacheService.getUserIdByUsername(username.toString());
                }
                var records = rceService.getIdUserByUsername(username.toString()).getRecords();
                if (records.isEmpty() || records.get(0) == null) {
                    log.error("No userId found for username {} ", username.toString());
                    throw new IllegalStateException("No userId found for username : " + username.toString()
                        + ", agence=" + agence + ", codeEDS=" + codeEDS + ", segmentationCode=" + segmentationCode);
                }
                cacheService.addUsernameAndUserId(username.toString(), records.get(0).getId());
                return records.get(0).getId();
            case "GC":
                // For DGC establishments, retrieve the owner from the parent
                if (obj instanceof EtaRefXL2 etaRefXL2) {
                    return getOwnerForDgcEstablishment(etaRefXL2);
                }

                // For DGC enterprise linked to DGC group, retrieve the owner from the parent
                if (obj instanceof EntRefXL entRefXL) {
                    var ownerId = getOwnerForDgcEnterprise(entRefXL);
                    if (!StringUtils.isEmpty(ownerId))
                        return ownerId;
                }

                // For DGC Group, DGC Entr without group, and DGC Entr linked to a group DEF or DIVOP : Retrieve owner by alias
                if(!edgCodes.keySet().contains(codeEDS))
                    throw new IllegalStateException("Invalid code EDS : "+ codeEDS);
                String alias = edgCodes.get(codeEDS);
                return getUserIdByAlias(alias);
            case "OP":
                return ownerIdForDIVOP;
            default:
                throw new IllegalStateException("Invalid commercial segment : "+ agence.getCommercialSegment());
        }
    }

    private String getOwnerForDgcEnterprise(EntRefXL entRefXL) {
        var idGroup = getParentIdOfEnterpriseFromGroupCCIALConstit(entRefXL.getId());
        if (!idGroup.isEmpty()) {

            GroupCCIALOrg groupCCIALOrg = groupCCIALOrgs.stream()
                        .filter(group -> group.getIdGroup().equals(idGroup.get())).findFirst()
                        .orElseGet(() -> groupCCIALOrgService.getByIdGroup(idGroup.get()));

            if (groupCCIALOrg != null && groupCCIALOrg.getAliasEDSVente() != null) {
                String groupEDSCode = groupCCIALOrg.getAliasEDSVente().substring(0,3);
                Agence groupAgence = getAgenceSmbCode(groupEDSCode);
                String commercialSegment = groupAgence.getCommercialSegment();

                if ("GC".equals(commercialSegment))
                    return getOwnerForEnterpriseLinkedToDgcGroup(idGroup.get());
            }
        }
        return null;
    }

    private String getOwnerForEnterpriseLinkedToDgcGroup(String idGroup) {
        return rceService.getAccountByRceID(AccountType.GROUPE.getType(), idGroup)
                     .flatMap(account -> {
                         if (account.getRecords().isEmpty()) {
                           return Mono.empty();
                         }
                         return Mono.just(account.getRecords().get(0).getOwnerId());
                     }).block();
    }

    private String getOwnerForDgcEstablishment(EtaRefXL2 etaRefXL2) {
        if (!etaRefXL2.getIdENT().isEmpty()) {
            return rceService.getAccountByRceID(AccountType.ENTREPRISE.getType(), etaRefXL2.getIdENT())
                .flatMap(accountBody -> {
                    if (accountBody.getRecords().isEmpty()) {
                        log.warn("Enterprise not found with RCE ID : {} when retrieving account owner for Establishment : {}. Using default DGC user : {} ", etaRefXL2.getIdENT(), etaRefXL2.getIdETA(), ownerIdForDGC);
                        return Mono.just(ownerIdForDGC);
                    }
                    return Mono.just(accountBody.getRecords().get(0).getOwnerId());
                }).block();
        }
        return ownerIdForDGC;
    }

    private String getUserIdByAlias(String alias) {
        if (cacheService.containsAlias(alias)) {
            return cacheService.getUserIdByAlias(alias);
        }

        var userRecords  = rceService.getIdUserByAlias(alias).getRecords();
        if (!userRecords.isEmpty() && userRecords.get(0) != null) {
            cacheService.addAliasAndUserId(alias, userRecords.get(0).getId());
            return userRecords.get(0).getId();
        } else {
            if (cacheService.containsAlias(SUPER_QD)) {
                return cacheService.getUserIdByAlias(SUPER_QD);
            }

            userRecords = rceService.getIdUserByAlias(SUPER_QD).getRecords();
            if (!userRecords.isEmpty() && userRecords.get(0) != null) {
                cacheService.addAliasAndUserId(SUPER_QD, userRecords.get(0).getId());
                return userRecords.get(0).getId();
            } else {
                log.error("No userId found for alias {} ", SUPER_QD);
                throw new IllegalStateException("No userId found for alias : " + SUPER_QD);
            }
        }
    }

    private String getSegmentationSmbCode(String code) {
        if(code == null || code.isEmpty() || code.isBlank()) {
            return "Other";
        }
      if (segmentationList.containsKey(code)) {
          return segmentationList.get(code);
      } else {
          throw new IllegalArgumentException("No segmentation found with the code : " + code);
      }
    }

    private Agence getAgenceSmbCode(String codeEDS) {
        // If the cache is empty, we retrieve the agencies from the config
        if (agences.isEmpty()) {
            agenceList.forEach(agence -> agences.add(Agence.builder()
                    .code(agence.get("code"))
                    .smbCode(agence.get("smbCode"))
                    .commercialSegment(agence.get("commercialSegment"))
                    .build()));
        }
        return agences.stream()
            .filter(ag -> ag.getCode().equals(codeEDS)).findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("No agence found with the code : " + codeEDS));
    }

    public Optional<EntRefXL> filterEntReferenceNew(Predicate<EntRefXL> predicate) {
        return entRefXLS.stream().filter(predicate).findFirst();
    }

    private Optional<RefRCE> filterRefRCE(Predicate<RefRCE> predicate) {
        return refRCES.stream().filter(predicate).findFirst();
    }
}
