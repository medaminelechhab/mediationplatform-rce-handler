package com.MyProject.mediationplatformrcehandler.service;
import com.MyProject.mediationplatformrcehandler.model.rce.*;
import com.MyProject.mediationplatformrcehandler.service.referential.*;
import lombok.RequiredArgsConstructor;

import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PersistenceService {
    private final GroupCCIALOrgService groupCCIALOrgService;
    private final GroupCCIALCompService groupCCIALCompService;
    private final GroupCCIALConstitService groupCCIALConstitService;
    private final EntMeOrgService entMeOrgService;
    private final EntMeCompService entMeCompService;
    private final EntRefXL2Service entRefXl2Service;
    private final RefRCEService refRCEService;
    private final EntScoringNoteortService entScoringNoteortService;
    private final EtaRefXL2Service etaRefXL2Service;
    private final EntEcoNewService entEcoNewService;
    private final EtaMeOrgNewService etaMeOrgNewService;

    public void refresh(List<GroupCCIALOrg> groupCCIALOrgs,
                                    List<GroupCCIALComp> groupCCIALComps,
                                    List<GroupCCIALConstit> groupCCIALConstits,
                                    List<EntMeOrg> entMeOrgs,
                                    List<EntMeComp> entMeComps,
                                    List<EntRefXL> entRefXl2s,
                                    List<RefRCE> refRCES,
                                    List<EntScoringNoteort> entScoringNoteorts,
                                    List<EtaRefXL2> etaRefXl2s,
                                    List<EntEcoNew> entEcoNews,
                                    List<EtaMeOrgNew> etaMeOrgNews) {
        groupCCIALConstitService.saveAll(groupCCIALConstits, false);
        groupCCIALOrgService.saveAll(groupCCIALOrgs, false);
        groupCCIALCompService.saveAll(groupCCIALComps, false);
        entMeOrgService.saveAll(entMeOrgs, false);
        entMeCompService.saveAll(entMeComps, false);
        entRefXl2Service.saveAll(entRefXl2s, false);
        refRCEService.saveAll(refRCES, false);
        entScoringNoteortService.saveAll(entScoringNoteorts, false);
        etaRefXL2Service.saveAll(etaRefXl2s, false);
        entEcoNewService.saveAll(entEcoNews, false);
        etaMeOrgNewService.saveAll(etaMeOrgNews, false);
    }
    public void saveAll(List<GroupCCIALOrg> groupCCIALOrgs, List<GroupCCIALComp> groupCCIALComps,
            List<GroupCCIALConstit> groupCCIALConstits, List<EntMeOrg> entMeOrgs, List<EntMeComp> entMeComps,
            List<EntRefXL> entRefXl2s, List<RefRCE> refRCES, List<EntScoringNoteort> entScoringNoteorts,
            List<EtaRefXL2> etaRefXl2s, List<EntEcoNew> entEcoNews, List<EtaMeOrgNew> etaMeOrgNews,
            boolean purgeFirst) {
        entRefXl2Service.saveAll(entRefXl2s, purgeFirst);
        etaRefXL2Service.saveAll(etaRefXl2s, purgeFirst);
        entEcoNewService.saveAll(entEcoNews, purgeFirst);
        groupCCIALOrgService.saveAll(groupCCIALOrgs, purgeFirst);
        groupCCIALConstitService.saveAll(groupCCIALConstits, purgeFirst);
        groupCCIALCompService.saveAll(groupCCIALComps, purgeFirst);
        entMeOrgService.saveAll(entMeOrgs, purgeFirst);
        entMeCompService.saveAll(entMeComps, purgeFirst);
        refRCEService.saveAll(refRCES, purgeFirst);
        entScoringNoteortService.saveAll(entScoringNoteorts, purgeFirst);
        etaMeOrgNewService.saveAll(etaMeOrgNews, purgeFirst);
    }
}
