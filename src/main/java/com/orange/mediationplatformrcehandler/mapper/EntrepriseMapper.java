package com.MyProject.mediationplatformrcehandler.mapper;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.Account;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Body;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.enums.AccountType;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Attributes;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Entreprise;
import com.MyProject.mediationplatformrcehandler.model.rce.EntMeComp;
import com.MyProject.mediationplatformrcehandler.model.rce.EntRefXL;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaRefXL2;
import com.MyProject.mediationplatformrcehandler.model.rce.RefRCE;
import com.MyProject.mediationplatformrcehandler.service.referential.EtaRefXL2Service;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.utils.AccountMapper;
import com.MyProject.mediationplatformrcehandler.utils.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class EntrepriseMapper {

    private AccountMapper accountMapper;
    private RCEService rceService;

    private ErrorHandler errorHandler;

    private final EtaRefXL2Service etaRefXl2Service;
    private static Map<String, Body<Account>> enterpriseParents = new HashMap<>();


    public EntrepriseMapper(AccountMapper accountMapper, 
            RCEService rceService, ErrorHandler errorHandler, EtaRefXL2Service etaRefXl2Service) {
        this.accountMapper = accountMapper;
        this.rceService = rceService;
        this.errorHandler = errorHandler;
        this.etaRefXl2Service = etaRefXl2Service;
    }

    public List<Entreprise> mapEnterpriseToCL(List<String> filteredEntreprises) {

        ArrayList<Entreprise> entreprises = new ArrayList<>();

        accountMapper.entRefXLS().stream()
                //Filter those in  filteredGroups or keep all when filteredGroups is empty
                .filter(e -> filteredEntreprises==null || filteredEntreprises.isEmpty() || filteredEntreprises.contains(e.getId()))
                .forEach(entReferenceNew -> {
            try {

                if (null == entReferenceNew.getNomrs() || entReferenceNew.getNomrs().isBlank()) {
                    throw new IllegalArgumentException("name can not be null for this entreprise : " + entReferenceNew.getId());
                }

                Entreprise.EntrepriseBuilder<?, ?> builder = Entreprise.builder();

                map(entReferenceNew, builder);

                var entrerpise = builder.build();
                entreprises.add(entrerpise);

            } catch (Exception e) {
                log.error("Error mapping entreprise  : {}, {}", entReferenceNew.getId(), e.getMessage());
                errorHandler.add(ErrorDTO.builder()
                        .errorMessage("Error mapping enterprise : " + entReferenceNew.getId()+ " [details: "+ e.getMessage() +"]")
                        .errorCode("MAPPING")
                        .clinksId(null)
                        .rceId(entReferenceNew.getId())
                        .type(ErrorDTO.AccountType.ENTREPRISE)
                        .build());
            }
        });
        enterpriseParents.clear();
        return entreprises;

    }

    private void map(EntRefXL entRefXL, Entreprise.EntrepriseBuilder<?, ?> builder) {
        
        //Find the etablissement : first in list then in DB
        EtaRefXL2 etaRefXL = accountMapper.filterEtaReferenceNew(e -> e.getIdENT().equalsIgnoreCase(entRefXL.getId()) 
                    && e.getSiege().equalsIgnoreCase("Y"))
                    .orElseGet(() -> etaRefXl2Service.getHqByIdENT(entRefXL.getId()));

        if(etaRefXL==null || etaRefXL.getSiege()==null || !etaRefXL.getSiege().equalsIgnoreCase("Y")){
            log.warn("Failed to get instance of HQ etablissement for id entreprise {}", entRefXL.getId());
        }

        var account = rceService.getAccountByRceID(AccountType.ENTREPRISE.getType(), entRefXL.getId()).block();

        //build the Entreprise object
        builder.name(entRefXL.getNomrs())
                .attributes(new Attributes("Account"))
                .accountRecordType("012E0000000RR6wIAG")
                .rceID(entRefXL.getId())
                .rceAccountType(Constants.ENTREPRISE)
                .sirenID(Optional.of(entRefXL.getSiren()).orElseThrow(null))
                .groupSirenID(Optional.of(entRefXL.getSiren()).orElse(null))
                .accountCurrency("EUR")
                .accountSource(Constants.RCE_FRANCE)
                .inactive(accountMapper.getEntrepriseStatus(entRefXL.getId()))
                .codeEDG(accountMapper.getCodeEDGEntreprise(entRefXL.getId()).orElse(null))
                .labelEDG(accountMapper.getEdgLabel(accountMapper.getCodeEDGEntreprise(entRefXL.getId()).orElse(null))
                        .orElse(null))
                .nationalCodeSegment(accountMapper.getNationalCodeSegmentEntreprise(entRefXL.getId()).orElse(null))
                .rceSegmentationNational(accountMapper.getRceSegmentationNationalEntreprise(accountMapper.getNationalCodeSegmentEntreprise(entRefXL.getId()).orElse(null))
                        .orElse(null))
                .codeSegLoc(accountMapper.getCodeSegLocEntreprise(entRefXL.getId()).orElse(null))
                .rceSegmentationLocal(accountMapper.getRceSegmentationLocalEntreprise(accountMapper.getCodeSegLocEntreprise(entRefXL.getId()).orElse(null))
                        .orElse(null))
                .legalStructure(accountMapper.getLegalStructureEntreprise(entRefXL.getCodeCatj()).orElse(null))
                .apen31(accountMapper.getApen31Entreprise(entRefXL).orElse(null))
                .naf(accountMapper.getNAFEntreprise(entRefXL).orElse(null))
                .scoreNote(accountMapper.getScoreNote(entRefXL.getId()).orElse(null))
                .scoreLabel(accountMapper.getScoreLabel(accountMapper.getScoreNote(entRefXL.getId()).orElse(null)).orElse(null))
                .detachmentDate(accountMapper.getDateDetachment(entRefXL).orElse(null))
                .accountOwner(accountMapper.getAccountOwner(entRefXL, account))
                .street(accountMapper.getBillingStreet(etaRefXL))
                .city(etaRefXL != null ? etaRefXL.getLibcom() : "")
                .postalCode(etaRefXL != null ? etaRefXL.getPostalCode() : "")
                .country(accountMapper.getBillingCountry(etaRefXL))
                .nationalMacroSectorization(accountMapper.getSectorization(Constants.Sectorisation.MACRO_SECTORISATION,
                                refRCE -> refRCE.getCode().equalsIgnoreCase(accountMapper.filterEntMeCompById(entRefXL.getId()).orElse(new EntMeComp()).getCodeMacroSec()))
                        .orElse(new RefRCE()).getLabel())
                .nationalMicroSectorization(accountMapper.getSectorization(Constants.Sectorisation.MICRO_SECTORISATION,
                                refRCE -> refRCE.getCode().equalsIgnoreCase(accountMapper.filterEntMeCompById(entRefXL.getId()).orElse(new EntMeComp()).getCodeMicroSec()))
                        .orElse(new RefRCE()).getLabel())
                .localMicroSectorization(accountMapper.getSectorization(Constants.Sectorisation.MICRO_SECTORISATION,
                                refRCE -> refRCE.getCode().equalsIgnoreCase(accountMapper.filterEntMeCompById(entRefXL.getId()).orElse(new EntMeComp()).getCodeMicroSecLoc()))
                        .orElse(new RefRCE()).getLabel());

        startEnterpriseCreationOrUpdate(builder, entRefXL, account);
    }

    private Mono<String> getParentIdForEnterpriseCreation(String rceId) {
        var id = accountMapper.getParentIdOfEnterpriseFromGroupCCIALConstit(rceId);

        if (id.isEmpty()) {
            return Mono.empty();
        }
        var parentId = id.get();

        return getInfoFromClinks(Constants.GROUP, parentId).flatMap(body -> (body == null || body.getRecords().isEmpty())
                ? Mono.empty()
                : Mono.just(body.getRecords().get(0).getId()));


    }


    private Mono<String> getParentIdForEnterpriseUpdate(String rceId) {
        var id = accountMapper.getParentIdOfEnterpriseFromGroupCCIALConstit(rceId);

        if (id.isEmpty()) {
            return Mono.empty(); // entreprise n'a plus de parent.
        }

        var parentId = id.get();

        return getInfoFromClinks(Constants.GROUP, parentId).flatMap(parentBody -> {
            var res = parentBody == null
                    || parentBody.getRecords().isEmpty()
                    || !Constants.RCE_FRANCE.equalsIgnoreCase(parentBody.getRecords().get(0).getAccountSource());

            return res ? Mono.empty() : Mono.just(parentBody.getRecords().get(0).getId());
        });


    }

    private void startEnterpriseCreationOrUpdate(Entreprise.EntrepriseBuilder<?, ?> builder, EntRefXL entRefXL, Body<Account> accountBody) {

        if (accountBody == null || accountBody.getRecords() == null || accountBody.getRecords().isEmpty()) {
            builder.parentAccount(getParentIdForEnterpriseCreation(entRefXL.getId()).block());
        } else {
            builder.id(accountBody.getRecords().get(0).getId());
            builder.parentAccount(getParentIdForEnterpriseUpdate(entRefXL.getId()).block());
        }
    }


    private Mono<Body<Account>> getInfoFromClinks(String type, String rceId) {
        if (enterpriseParents.containsKey(rceId)) {
            log.info("Retrieving parent for enterprise= {} from the cache", rceId);
            return Mono.just(enterpriseParents.get(rceId));
        }
        return rceService.getAccountByRceID(type, rceId)
                .doOnNext(response -> {
                 log.info("Saving parent for enterprise= {} in the cache", rceId);
                 enterpriseParents.put(rceId, response);
                })
                .onErrorResume(throwable -> {
                    log.error("Account Not found for rceId : {}", rceId);
                    return Mono.empty();
                });
    }

}
