package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaRefXL2;
import com.MyProject.mediationplatformrcehandler.repository.EtaRefXl2Repository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class EtaRefXL2ServiceTest {

    private EtaRefXL2Service etaRefXL2Service;

    @Mock
    private EtaRefXl2Repository repository;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    private EtaRefXL2 etaRefXL21;

    private EtaRefXL2 etaRefXL22;

    DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();

    @BeforeEach
    void initialize() {
        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        etaRefXL2Service = new EtaRefXL2Service(repository, template, dozerBeanMapper);

        etaRefXL21 = EtaRefXL2.builder()
                .idETA("10")
                .idENT("100")
                .build();

        etaRefXL22 = EtaRefXL2.builder()
                .idETA("11")
                .build();
    }

    @Test
    @DisplayName("Refresh EtaRefXL2 in DB OK")
    void testRefreshEtaRefXL2OK() {

        var etaRefXL2s = List.of(etaRefXL21, etaRefXL22);

        etaRefXL2Service.saveAll(etaRefXL2s, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }
    @Test
    @DisplayName("Update EtaRefXL2 in DB OK")
    void testUpdateEtaRefXL2OK() {

        var etaRefXL2s = List.of(etaRefXL21, etaRefXL22);

        etaRefXL2Service.saveAll(etaRefXL2s, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh EtaRefXL2 in DB KO") //TODO: need to get that test KO
    void testRefreshEtaRefXL2KO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var etaRefXL2s = List.of(etaRefXL21);

        etaRefXL2Service.saveAll(etaRefXL2s, true);
        /*IllegalStateException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> etaRefXL2Service.saveAll(etaRefXL2s, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));*/
    }
}