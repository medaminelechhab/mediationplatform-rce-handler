package com.MyProject.mediationplatformrcehandler.mapper;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.Account;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Body;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.enums.AccountType;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Attributes;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Group;
import com.MyProject.mediationplatformrcehandler.model.rce.*;
import com.MyProject.mediationplatformrcehandler.service.referential.EntRefXL2Service;
import com.MyProject.mediationplatformrcehandler.service.referential.EtaRefXL2Service;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.utils.AccountMapper;
import com.MyProject.mediationplatformrcehandler.utils.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class GroupeMapper {
    private final AccountMapper accountMapper;
    private final RCEService rceService;

    private ErrorHandler errorHandler;

    private final EntRefXL2Service entRefXl2Service;
    private final EtaRefXL2Service etaRefXl2Service;

    public GroupeMapper(AccountMapper accountMapper, RCEService rceService, ErrorHandler errorHandler,
        EntRefXL2Service entRefXl2Service, EtaRefXL2Service etaRefXl2Service) {

        this.accountMapper = accountMapper;
        this.errorHandler = errorHandler;
        this.entRefXl2Service = entRefXl2Service;
        this.etaRefXl2Service = etaRefXl2Service;
        this.rceService = rceService;
    }

    public List<Group> mapGroupToCL(List<String> filteredGroups) {


        ArrayList<Group> groupes = new ArrayList<>();
        var builder = Group.builder();
        accountMapper.groupCCIALOrgs().stream()
                .filter(e -> filteredGroups==null || filteredGroups.isEmpty() || filteredGroups.contains(e.getIdGroup()))
                .forEach(group -> {
            try {
                if (null == group.getNameGroup() || group.getNameGroup().isBlank()) {
                    throw new IllegalArgumentException("name can not be null for this group : " + group.getNameGroup());
                }
                if (null == group.getIdGroup() || group.getIdGroup().isBlank()) {
                    throw new IllegalArgumentException("id can not be null for this group : " + group.getIdGroup());
                }
                map(group, builder);
                groupes.add(builder.build());
            } catch (Exception e) {
                log.error("Error mapping Groupe : {}", group.getIdGroup(), e.getMessage());
                errorHandler.add(ErrorDTO.builder()
                        .errorMessage("Error mapping group :" + group.getIdGroup() + " [details: "+ e.getMessage() +"]")
                        .errorCode("MAPPING")
                        .clinksId(null)
                        .rceId(group.getIdGroup())
                        .type(ErrorDTO.AccountType.GROUPE)
                        .build());
            }
        });
        return groupes;

    }

    private void map(GroupCCIALOrg groupCCIALOrg, Group.GroupBuilder<?, ?> builder) {

        final String idEnt = groupCCIALOrg.getIdEntMm();
        if(idEnt==null || idEnt.isEmpty()){
            log.warn("Entreprise Id is null or empty for group {}", groupCCIALOrg.getIdGroup());
        }

        //Find the entreprise : first in list then in DB
        EntRefXL entRefXl2 = accountMapper.filterEntReferenceNew(entReferenceNew -> entReferenceNew.getId().equals(idEnt))
                        .orElseGet(() -> entRefXl2Service.getByIdEnt(idEnt));

        if(entRefXl2==null){
            log.warn("Failed to get instance of entreprise for group {}", groupCCIALOrg.getIdGroup());
        }

        //Find the HQ etablissement : first in list then in DB
        EtaRefXL2 etaRefXl2 = accountMapper.filterEtaReferenceNew(e -> e.getIdENT().equalsIgnoreCase(idEnt)
                            && e.getSiege()!=null && e.getSiege().equalsIgnoreCase("Y"))
                            .orElseGet(() -> etaRefXl2Service.getHqByIdENT(idEnt));

        if(etaRefXl2==null || etaRefXl2.getSiege()==null || !etaRefXl2.getSiege().equalsIgnoreCase("Y")){
            log.warn("Failed to get instance of HQ etablissement for group {}", groupCCIALOrg.getIdGroup());
        }

        var account = rceService.getAccountByRceID(AccountType.GROUPE.getType(), groupCCIALOrg.getIdGroup()).block();

        //Build the group object
        builder.name(groupCCIALOrg.getNameGroup())
                .attributes(new Attributes("Account"))
                .accountRecordType("012E0000000RR6wIAG")
                .id(getId18(account))
                .rceID(groupCCIALOrg.getIdGroup())
                .rceAccountType(Constants.GROUP)
                .accountCurrency("EUR")
                .accountSource(Constants.RCE_FRANCE)
                .codeEDG(groupCCIALOrg.getAliasEDSVente().substring(0, 3))
                .labelEDG(accountMapper.getGroupEdgLabel(groupCCIALOrg.getAliasEDSVente().substring(0, 3)).orElseThrow(() -> new RuntimeException("AliasEDSVente not found for the groupe " + groupCCIALOrg.getIdGroup())))
                .codeSegNat(accountMapper.getCodeSegNat(groupCCIALOrg.getIdGroup()).orElse(null))
                .rceSegmentationNational(accountMapper.getRceSegmentationNational(groupCCIALOrg.getIdGroup()).orElse(null))
                .codeSegLoc(accountMapper.fillerGroupCCIALComp(groupCCIALComp -> groupCCIALComp.getIdGroup().equalsIgnoreCase(groupCCIALOrg.getIdGroup()))
                    .orElse(new GroupCCIALComp()).getSegmentationDCECodeLoc())
                .rceSegmentationLocal(accountMapper.getRceSegmentationLocal(groupCCIALOrg.getIdGroup()).orElse(null))
                .suppressionDate(accountMapper.parseDate(groupCCIALOrg.getDeletionDate()).orElse(null))
                .accountOwner(accountMapper.getAccountOwner(groupCCIALOrg, account))
                .nic(accountMapper.getGroupeNic(etaRefXl2))
                .street(accountMapper.getBillingStreet(etaRefXl2))
                .city(etaRefXl2!=null?etaRefXl2.getLibcom():"")
                .postalCode(etaRefXl2!=null?etaRefXl2.getPostalCode():"")
                .country(accountMapper.getBillingCountry(etaRefXl2))
                .sirenID(entRefXl2!=null?entRefXl2.getSiren():"")
                .groupSirenId(entRefXl2!=null?entRefXl2.getSiren():"")
                .inactive(accountMapper.getGroupStatus(entRefXl2))
                .legalStructure(accountMapper.getLegalStructure(entRefXl2))
                .apen31(accountMapper.getApen31(entRefXl2))
                .naf(accountMapper.getNaf(entRefXl2))
                .detachmentDate(accountMapper.getDateDetachment(entRefXl2).orElse(null))
                .nationalMacroSectorization(accountMapper.getSectorization(Constants.Sectorisation.MACRO_SECTORISATION,
                                refRCE -> refRCE.getCode().equalsIgnoreCase(accountMapper
                                        .filterEntMeCompById(entRefXl2!=null?entRefXl2.getId():null)
                                        .orElse(new EntMeComp())
                                        .getCodeMacroSec()))
                        .orElse(new RefRCE()).getLabel())
                .nationalMicroSectorization(accountMapper.getSectorization(Constants.Sectorisation.MICRO_SECTORISATION,
                                refRCE -> refRCE.getCode().equalsIgnoreCase(accountMapper
                                        .filterEntMeCompById(entRefXl2!=null?entRefXl2.getId():null)
                                        .orElse(new EntMeComp()).getCodeMicroSec()))
                        .orElse(new RefRCE()).getLabel())
                .localMicroSectorization(accountMapper.getSectorization(Constants.Sectorisation.MICRO_SECTORISATION,
                                refRCE -> refRCE.getCode().equalsIgnoreCase(accountMapper
                                        .filterEntMeCompById(entRefXl2!=null?entRefXl2.getId():null)
                                        .orElse(new EntMeComp()).getCodeMicroSecLoc()))
                        .orElse(new RefRCE()).getLabel());
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
