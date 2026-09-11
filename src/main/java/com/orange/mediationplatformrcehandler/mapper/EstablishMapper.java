package com.MyProject.mediationplatformrcehandler.mapper;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.Account;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Body;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.enums.AccountType;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Attributes;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Establishment;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaMeOrgNew;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaRefXL2;
import com.MyProject.mediationplatformrcehandler.service.referential.EtaMeOrgNewService;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.utils.AccountMapper;
import com.MyProject.mediationplatformrcehandler.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
public class EstablishMapper {
    private AccountMapper accountMapper;

    private RCEService rceService;
    private EtaMeOrgNewService etaMeOrgNewService;

    private ErrorHandler errorHandler;

    private String id18DTO;


    public EstablishMapper(AccountMapper accountMapper, RCEService rceService, ErrorHandler errorHandler, EtaMeOrgNewService etaMeOrgNewService) {
        this.accountMapper = accountMapper;
        this.rceService = rceService;
        this.errorHandler = errorHandler;
        this.etaMeOrgNewService = etaMeOrgNewService;
    }

    public List<Establishment> mapEstablishmentToCL(List<String> filteredEtablissements) {

        ArrayList<Establishment> establishments = new ArrayList<>();
        Establishment.EstablishmentBuilder<?, ?> builder = Establishment.builder();

        accountMapper.etaRefXL2s().stream()
                //Filter those in  filteredGroups or keep all when filteredGroups is empty
                .filter(e -> filteredEtablissements==null || filteredEtablissements.isEmpty() || filteredEtablissements.contains(e.getIdETA()))
                .forEach(etaReferenceNew -> {
            try {
                if (null == etaReferenceNew.getEnseigne() || etaReferenceNew.getEnseigne().isBlank()) {
                    throw new IllegalArgumentException("name can not be null for this establishment : " + etaReferenceNew.getIdETA());
                }
                if (null == etaReferenceNew.getIdETA() || etaReferenceNew.getIdETA().isBlank()) {
                    throw new IllegalArgumentException("id can not be null for this establishment for entreprise : " + etaReferenceNew.getIdENT());
                }
                map(etaReferenceNew, builder);
                Establishment establishment = builder.build();
                establishments.add(establishment);
            } catch (Exception e) {
                log.error("Error mapping Etablissement  : {}, {}", etaReferenceNew.getIdETA(), e.getMessage());
                errorHandler.add(ErrorDTO.builder()
                        .errorMessage("Error mapping ETABLISSEMENT :" + etaReferenceNew.getIdETA() + " [details: "+ e.getMessage() +"]")
                        .errorCode("MAPPING")
                        .clinksId(id18DTO)
                        .rceId(etaReferenceNew.getIdETA())
                        .type(ErrorDTO.AccountType.ETABLISSEMENT)
                        .build());
            }
        });

        return establishments;
    }

    private void map(EtaRefXL2 etaRefXL2, Establishment.EstablishmentBuilder<?, ?> builder) {

        //Find the EtaMeOrg instance : first in list then in DB
        EtaMeOrgNew etaMeOrgNew = accountMapper.etaMeOrgNews().stream()
                    .filter(e -> e.getId().equalsIgnoreCase(etaRefXL2.getIdETA()))
                    .findFirst()
                    .orElseGet(
                        () -> {var e = etaMeOrgNewService.getAllByIds(List.of(etaRefXL2.getIdETA()))
                            .stream().findFirst();
                            if(!e.isPresent()){
                                log.warn("Failed to get instance of etablissement {} in EtaMeOrgNew ", etaRefXL2.getIdETA());
                                throw new IllegalStateException("Failed to get instance of etablissement in EtaMeOrgNew : " + etaRefXL2.getIdETA());
                            }
                            return e.get();
                        }
                    );

        var account = rceService.getAccountByRceID(AccountType.ETABLISSEMENT.getType(), etaRefXL2.getIdETA()).block();

        builder.name(etaRefXL2.getEnseigne())
                .attributes(new Attributes("Account"))
                .accountRecordType("012E0000000RR6wIAG")
                .id(getId18(account))
                .rceID(etaRefXL2.getIdETA())
                .rceAccountType(Constants.ETABLISSEMENT)
                .sirenID(etaRefXL2.getSiren())
                .accountCurrency("EUR")
                .accountSource(Constants.RCE_FRANCE)
                .headQuarterAccount(accountMapper.headQuarterAccountValue(etaRefXL2.getSiege()))
                .accountOwner(accountMapper.getAccountOwner(etaRefXL2, etaMeOrgNew, account))
                .street(etaRefXL2.getL4Normalisee() + System.lineSeparator() + etaRefXL2.getL3Normalisee())
                .city(etaRefXL2.getLibcom())
                .postalCode(etaRefXL2.getPostalCode())
                .country(accountMapper.getCountry(etaRefXL2.getCountryCode()).orElse(null))
                .inactive(accountMapper.getEstablishementStatus(etaRefXL2.getStatutINSEE()))
                .codeEDG(accountMapper.getCodeEDG(etaRefXL2.getIdETA()).orElse(null))
                .labelEDG(accountMapper.getEdgLabel(accountMapper.getCodeEDG(etaRefXL2.getIdETA()).orElse(null)).orElse(null))
                .nic(etaRefXL2.getNic())
                .siret(etaRefXL2.getSiren() + etaRefXL2.getNic())
                .decisionDegree(accountMapper.getDecisionDegree(etaRefXL2.getIdETA()).orElse(null))
                .decisionDegreeLabel(accountMapper.handleDecisionDegreeLabel(accountMapper.getDecisionDegree(etaRefXL2.getIdETA()).orElse(null)).orElse(null))
                .parentAccount(getEtablissmentParentIdFromCustomerLinks(etaRefXL2));

    }

    public String getEtablissmentParentIdFromCustomerLinks(EtaRefXL2 etaRefXL2) {
        if (etaRefXL2.getIdENT() == null || etaRefXL2.getIdENT().isBlank()) return null;
        rceService.getAccountByRceID(Constants.ENTREPRISE, etaRefXL2.getIdENT()).onErrorResume(throwable -> {
            log.info("ParentId not found with EntId {}", etaRefXL2.getIdENT());
            return Mono.empty();
        }).doOnNext(accountBody -> id18DTO = (accountBody == null || accountBody.getRecords().isEmpty())
                ? null
                : accountBody.getRecords().get(0).getId()).block();

        return id18DTO;
    }

    private String getId18(Body<Account> account) {
        if (account != null
            && account.getRecords() != null
            && !account.getRecords().isEmpty()) {
            return  account.getRecords().get(0).getId();
        }
        return null;
    }

}
