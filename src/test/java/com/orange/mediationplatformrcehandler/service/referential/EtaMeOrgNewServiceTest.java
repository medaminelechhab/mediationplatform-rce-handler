package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaMeOrgNew;
import com.MyProject.mediationplatformrcehandler.repository.EtaMeOrgNewRepository;
import org.dozer.DozerBeanMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class EtaMeOrgNewServiceTest {

    private EtaMeOrgNewService etaMeOrgNewService;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    EtaMeOrgNewRepository etaMeOrgNewRepo;

    private EtaMeOrgNew etaMeOrgNew1;

    private EtaMeOrgNew etaMeOrgNew2;
    @Mock
    DozerBeanMapper dozerBeanMapper;

    @BeforeEach
    void initialize() {
        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        etaMeOrgNewService = new EtaMeOrgNewService(template, etaMeOrgNewRepo, dozerBeanMapper);

        etaMeOrgNew1 = EtaMeOrgNew.builder()
                .id("10")
                .modificationDate("20240101")
                .build();

        etaMeOrgNew2 = EtaMeOrgNew.builder()
                .id("11")
                .build();
    }

    @Test
    @DisplayName("Refresh EtaMeOrgNew in DB OK")
    void testRefreshEtaMeOrgNewOK() {

        var etaMeOrgNews = List.of(etaMeOrgNew1, etaMeOrgNew2);

        etaMeOrgNewService.saveAll(etaMeOrgNews, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }

    @Test
    @DisplayName("Update EtaMeOrgNew in DB OK")
    void testUpdateEtaMeOrgNewOK() {

        var etaMeOrgNews = List.of(etaMeOrgNew1, etaMeOrgNew2);

        etaMeOrgNewService.saveAll(etaMeOrgNews, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh EtaMeOrgNew in DB KO") //TODO: need to get that test KO
    void testRefreshEtaMeOrgNewKO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var etaMeOrgNews = List.of(etaMeOrgNew1);

        etaMeOrgNewService.saveAll(etaMeOrgNews, true);
        /*
        IllegalStateException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> etaMeOrgNewService.saveAll(etaMeOrgNews, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));*/
    }
}