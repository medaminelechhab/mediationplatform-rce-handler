package com.MyProject.mediationplatformrcehandler.service;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.EntEcoNewData;
import com.MyProject.mediationplatformrcehandler.model.database.EntMeCompData;
import com.MyProject.mediationplatformrcehandler.model.database.EntMeOrgData;
import com.MyProject.mediationplatformrcehandler.model.database.EntScoringNoteortData;
import com.MyProject.mediationplatformrcehandler.model.database.EtaMeOrgNewData;
import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALCompData;
import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALConstitData;
import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALOrgData;
import com.MyProject.mediationplatformrcehandler.model.rce.EntEcoNew;
import com.MyProject.mediationplatformrcehandler.model.rce.EntMeComp;
import com.MyProject.mediationplatformrcehandler.model.rce.EntMeOrg;
import com.MyProject.mediationplatformrcehandler.model.rce.EntRefXL;
import com.MyProject.mediationplatformrcehandler.model.rce.EntScoringNoteort;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaMeOrgNew;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaRefXL2;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALComp;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALConstit;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALOrg;
import com.MyProject.mediationplatformrcehandler.model.rce.RefRCE;
import com.MyProject.mediationplatformrcehandler.repository.EntEcoNewRepository;
import com.MyProject.mediationplatformrcehandler.repository.EntMeCompRepository;
import com.MyProject.mediationplatformrcehandler.repository.EntMeOrgRepository;
import com.MyProject.mediationplatformrcehandler.repository.EntRefXl2Repository;
import com.MyProject.mediationplatformrcehandler.repository.EntScoringNoteortRepository;
import com.MyProject.mediationplatformrcehandler.repository.EtaMeOrgNewRepository;
import com.MyProject.mediationplatformrcehandler.repository.EtaRefXl2Repository;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALCompRepository;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALConstitRepository;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALOrgRepository;
import com.MyProject.mediationplatformrcehandler.repository.RefRCERepository;
import com.MyProject.mediationplatformrcehandler.service.referential.*;
import org.dozer.DozerBeanMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class BusinessServicesTest {
    
    PersistenceService persistenceService;
    @MockitoBean
    RefRCEService refRceService;
    @MockitoBean EtaMeOrgNewService etaMeOrgNewService ;
    @MockitoBean EntEcoNewService entEcoNewService ;
    @MockitoBean EntScoringNoteortService entScoringNoteortService ;
    @MockitoBean EntMeCompService entMeCompService ;
    @MockitoBean EntMeOrgService entMeOrgService ;
    @MockitoBean GroupCCIALConstitService groupCCIALConstitService ;
    @MockitoBean GroupCCIALCompService groupCCIALCompService ;
    @MockitoBean GroupCCIALOrgService groupCCIALOrgService ;
    @MockitoBean EntRefXL2Service entRefXl2Service;
    @MockitoBean EtaRefXL2Service etaRefXl2Service;
    
    @Mock BulkOperations bulkOps;
    @Mock BulkWriteResult result;
    @Mock MongoMappingContext mappingContext;
    @Mock MongoConverter mongoConverter;
    @Mock MongoTemplate mongoTemplate ;
    
    @Mock RefRCERepository refRCERepository;
    @Mock EtaMeOrgNewRepository etaMeOrgNewRepository;
    @Mock EntEcoNewRepository entEcoNewRepository;
    @Mock EntScoringNoteortRepository entScoringNoteortRepository;
    @Mock EntMeCompRepository entMeCompRepository;
    @Mock EntMeOrgRepository entMeOrgRepository;
    @Mock GroupCCIALConstitRepository groupCCIALConstitRepository;
    @Mock GroupCCIALCompRepository groupCCIALCompRepository;
    @Mock GroupCCIALOrgRepository groupCCIALOrgRepository;
    @Mock RefRCERepository rceRepository;
    @Mock EntRefXl2Repository entRefXl2Repository;
    @Mock EtaRefXl2Repository etaRefXl2Repository;

    EtaMeOrgNew etaMeOrgNew;
    EntEcoNew entEcoNew;
    EntScoringNoteort entScoringNoteort;
    EntMeComp entMeComp;
    EntMeOrg entMeOrg;
    GroupCCIALConstit groupCCIALConstit;
    GroupCCIALComp groupCCIALComp;
    GroupCCIALOrg groupCCIALOrg;
    RefRCE refRCE;
    EtaMeOrgNewData etaMeOrgNewData ;
    EntEcoNewData entEcoNewData ;
    EntScoringNoteortData entScoringNoteortData ;
    EntMeCompData entMeCompData ;
    EntMeOrgData entMeOrgData ;
    GroupCCIALConstitData groupCCIALConstitData ;
    GroupCCIALCompData groupCCIALCompData ;
    GroupCCIALOrgData groupCCIALOrgData ;
    
    List<EtaMeOrgNewData> l_etaMeOrgNewData = new ArrayList<>();
    List<EntEcoNewData> l_entEcoNewData = new ArrayList<>();
    List<EntScoringNoteortData> l_entScoringNoteortData = new ArrayList<>();
    List<EntMeCompData> l_entMeCompData = new ArrayList<>();
    List<EntMeOrgData> l_entMeOrgData = new ArrayList<>();
    List<GroupCCIALConstitData> l_groupCCIALConstitData = new ArrayList<>();
    List<GroupCCIALCompData> l_groupCCIALCompData = new ArrayList<>();
    List<GroupCCIALOrgData> l_groupCCIALOrgData = new ArrayList<>();
    List<EtaMeOrgNew> l_etaMeOrgNew = new ArrayList<>();
    List<EntEcoNew> l_entEcoNew = new ArrayList<>();
    List<EntScoringNoteort> l_entScoringNoteort = new ArrayList<>();
    List<EntMeComp> l_entMeComp = new ArrayList<>();
    List<EntMeOrg> l_entMeOrg = new ArrayList<>();
    List<GroupCCIALConstit> l_groupCCIALConstit = new ArrayList<>();
    List<GroupCCIALComp> l_groupCCIALComp = new ArrayList<>();
    List<GroupCCIALOrg> l_groupCCIALOrg = new ArrayList<>();
    List<EtaRefXL2> l_etaRefXl2 = new ArrayList<>();
    List<RefRCE> l_refRce = new ArrayList<>();
    List<EntRefXL> l_entRefXl2 = new ArrayList<>();

    @BeforeEach
    public void init() {

        for (int index = 0; index < 100; index++) {
            l_etaMeOrgNewData.add(EtaMeOrgNewData.builder().id(index + "0000").idEts(index + "0000").build());
            l_entEcoNewData.add(EntEcoNewData.builder().idEnt(index + "0000").ca(index + "0000").build());
            l_entScoringNoteortData.add(EntScoringNoteortData.builder().id(index + "0000").deffJugement(index + "0000").build());
            l_entMeCompData.add(EntMeCompData.builder().idEnt(index + "0000").codeMacroSec(index + "0000").build());
            l_entMeOrgData.add(EntMeOrgData.builder().id(index + "0000").dattachement(index + "0000").build());
            l_groupCCIALConstitData.add(GroupCCIALConstitData.builder().idGroup(index + "0000").idENT(index + "0000").build());
            l_groupCCIALCompData.add(GroupCCIALCompData.builder().idGroup(index + "0000").reserve(index + "0000").build());
            l_groupCCIALOrgData.add(GroupCCIALOrgData.builder().idGroup(index + "0000").idEntMm(index + "0000").build());
        }

        for (int index = 0; index < 100; index++) {
            l_etaMeOrgNew.add(EtaMeOrgNew.builder().id(index + "0000").idETA(index + "0000").build());
            l_entEcoNew.add(EntEcoNew.builder().id(index + "0000").ca(index + "0000").build());
            l_entScoringNoteort.add(EntScoringNoteort.builder().id(index + "0000").deffJugement(index + "0000").build());
            l_entMeComp.add(EntMeComp.builder().id(index + "0000").codeMacroSec(index + "0000").build());
            l_entMeOrg.add(EntMeOrg.builder().idEnt(index + "0000").id(index + "0000").dattachement(index + "0000").build());
            l_groupCCIALConstit.add(GroupCCIALConstit.builder().idGroup(index + "0000").idENT(index + "0000").build());
            l_groupCCIALComp.add(GroupCCIALComp.builder().idGroup(index + "0000").reserve(index + "0000").build());
            l_groupCCIALOrg.add(GroupCCIALOrg.builder().idGroup(index + "0000").idEntMm(index + "0000").build());
        }

        entEcoNewService = new EntEcoNewService(mongoTemplate, entEcoNewRepository, new DozerBeanMapper());
        entMeOrgService = new EntMeOrgService(mongoTemplate, entMeOrgRepository, new DozerBeanMapper());
        entScoringNoteortService = new EntScoringNoteortService(mongoTemplate, entScoringNoteortRepository, new DozerBeanMapper());
        entMeCompService = new EntMeCompService(mongoTemplate, entMeCompRepository, new DozerBeanMapper());

        etaMeOrgNewService = new EtaMeOrgNewService(mongoTemplate, etaMeOrgNewRepository, new DozerBeanMapper());
        groupCCIALCompService = new GroupCCIALCompService(mongoTemplate, groupCCIALCompRepository, new DozerBeanMapper());
        groupCCIALConstitService = new GroupCCIALConstitService(mongoTemplate, groupCCIALConstitRepository, new DozerBeanMapper());
        groupCCIALOrgService = new GroupCCIALOrgService(mongoTemplate,groupCCIALOrgRepository, new DozerBeanMapper());
        refRceService = new RefRCEService(refRCERepository, mongoTemplate);
        entRefXl2Service = new EntRefXL2Service(mongoTemplate, entRefXl2Repository, new DozerBeanMapper());
        etaRefXl2Service = new EtaRefXL2Service(etaRefXl2Repository, mongoTemplate, new DozerBeanMapper());

        etaMeOrgNewService = new EtaMeOrgNewService(mongoTemplate, etaMeOrgNewRepository, new DozerBeanMapper());
        groupCCIALCompService = new GroupCCIALCompService(mongoTemplate, groupCCIALCompRepository, new DozerBeanMapper());
        groupCCIALConstitService = new GroupCCIALConstitService(mongoTemplate, groupCCIALConstitRepository, new DozerBeanMapper());
        groupCCIALOrgService = new GroupCCIALOrgService(mongoTemplate,groupCCIALOrgRepository, new DozerBeanMapper());
    }

    @Test
    @DisplayName("Get all from repository")
    void testGetAll() {

        Mockito.when(etaMeOrgNewRepository.findAll()).thenReturn(l_etaMeOrgNewData);
        var list1 = etaMeOrgNewService.getAll();
        Mockito.when(entEcoNewRepository.findAll()).thenReturn(l_entEcoNewData);
        var list2 = entEcoNewService.getAll();
        Mockito.when(entMeOrgRepository.findAll()).thenReturn(l_entMeOrgData);
        var list3 = entMeOrgService.getAll();
        Mockito.when(entScoringNoteortRepository.findAll()).thenReturn(l_entScoringNoteortData);
        var list4 = entScoringNoteortService.getAll();
        Mockito.when(entMeCompRepository.findAll()).thenReturn(l_entMeCompData);
        var list5 = entMeCompService.getAll();
        Mockito.when(groupCCIALCompRepository.findAll()).thenReturn(l_groupCCIALCompData);
        var list6 = groupCCIALCompService.getAll();
        Mockito.when(groupCCIALConstitRepository.findAll()).thenReturn(l_groupCCIALConstitData);
        var list7 = groupCCIALConstitService.getAll();
        Mockito.when(groupCCIALOrgRepository.findAll()).thenReturn(l_groupCCIALOrgData);
        var list8 = groupCCIALOrgService.getAll();

        assertTrue(!list1.isEmpty());
        assertTrue(!list2.isEmpty());
        assertTrue(!list3.isEmpty());
        assertTrue(!list4.isEmpty());
        assertTrue(!list5.isEmpty());
        assertTrue(!list6.isEmpty());
        assertTrue(!list7.isEmpty());
        assertTrue(!list8.isEmpty());
        assertEquals("00000", list1.get(0).getId());
        assertEquals("00000", list2.get(0).getId());
        assertEquals("00000", list3.get(0).getId());
        assertEquals("00000", list4.get(0).getId());
        assertEquals("00000", list5.get(0).getId());
        assertEquals("00000", list6.get(0).getIdGroup());
        assertEquals("00000", list6.get(0).getReserve());
        assertEquals("00000", list7.get(0).getIdENT());
        assertEquals("00000", list7.get(0).getIdGroup());
        assertEquals("00000", list8.get(0).getIdGroup());
        assertEquals("00000", list8.get(0).getIdEntMm());

        assertEquals("00000", list1.get(0).getIdETA());
        assertEquals("00000", list2.get(0).getCa());
        assertEquals("00000", list3.get(0).getDattachement());
        assertEquals("00000", list4.get(0).getDeffJugement());
    }

    @Test
    @DisplayName("Get all by ids from repository")
    void testGetAllByIds() {

        Mockito.when(etaMeOrgNewRepository.findByIdEtsIn(any())).thenReturn(l_etaMeOrgNewData);
        Mockito.when(entEcoNewRepository.findByIdEntIn(any())).thenReturn(l_entEcoNewData);
        Mockito.when(entMeOrgRepository.findByIdEntIn(any())).thenReturn(l_entMeOrgData);
        Mockito.when(entScoringNoteortRepository.findByIdEntIn(any())).thenReturn(l_entScoringNoteortData);
        Mockito.when(entMeCompRepository.findByIdEntIn(any())).thenReturn(l_entMeCompData);
        Mockito.when(groupCCIALCompRepository.findByIdGroupIn(any())).thenReturn(l_groupCCIALCompData);
        Mockito.when(groupCCIALConstitRepository.findByIdENTIn(any())).thenReturn(l_groupCCIALConstitData);
        
        List<String> etaRefXlIds = List.of("0000");
        List<String> entRefXlIds = List.of("0000");
        List<String> groupIds = List.of("0000");
        var etaMeOrgNews = etaMeOrgNewService.getAllByIds(etaRefXlIds);
        var entEcoNews = entEcoNewService.getAllByIds(entRefXlIds);
        var entScoringNoteorts = entScoringNoteortService.getAllByIds(entRefXlIds);
        var entMeComps = entMeCompService.getAllByIds(entRefXlIds);
        var entMeOrgs = entMeOrgService.getAllByIds(entRefXlIds);
        var groupCCIALConstits = groupCCIALConstitService.getAllByEntIds(groupIds);
        var groupCCIALComps = groupCCIALCompService.getAllByIds(groupIds);
        assertTrue(!etaMeOrgNews.isEmpty());
        assertTrue(!entEcoNews.isEmpty());
        assertTrue(!entScoringNoteorts.isEmpty());
        assertTrue(!entMeComps.isEmpty());
        assertTrue(!entMeOrgs.isEmpty());
        assertTrue(!groupCCIALConstits.isEmpty());
        assertTrue(!groupCCIALComps.isEmpty());

    }

    @Mock
	DbRefResolver dbRefResolver;
    @Mock
	private MongoOperations template;

    @Test
    @DisplayName("Refresh db OK")
    void testRefreshAnSave() {

        //when(mongoConverter.getMappingContext()).then(ignoredInvocation -> mappingContext);
        //when(mongoTemplate.getConverter()).thenReturn(mongoConverter);

        MappingContext<MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = new MongoMappingContext();
		MappingMongoConverter mongoConverter = spy(new MappingMongoConverter(dbRefResolver, mappingContext));
		when(template.getConverter()).thenReturn(mongoConverter);
        when(mongoTemplate.bulkOps(any(), any(Class.class))).thenReturn(bulkOps);
        when(bulkOps.execute()).thenReturn(result);

        persistenceService = new PersistenceService(groupCCIALOrgService, groupCCIALCompService, groupCCIALConstitService, entMeOrgService, entMeCompService, entRefXl2Service, refRceService, entScoringNoteortService, etaRefXl2Service, entEcoNewService, etaMeOrgNewService);
        persistenceService.refresh(l_groupCCIALOrg, l_groupCCIALComp, l_groupCCIALConstit, l_entMeOrg, l_entMeComp, l_entRefXl2, l_refRce, l_entScoringNoteort, l_etaRefXl2, l_entEcoNew, l_etaMeOrgNew);
        persistenceService.saveAll(l_groupCCIALOrg, l_groupCCIALComp, l_groupCCIALConstit, l_entMeOrg, l_entMeComp, l_entRefXl2, l_refRce, l_entScoringNoteort, l_etaRefXl2, l_entEcoNew, l_etaMeOrgNew, false);

    }
}
