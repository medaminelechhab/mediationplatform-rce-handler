package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.rce.EntMeComp;
import com.MyProject.mediationplatformrcehandler.repository.EntMeCompRepository;
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
class EntMeCompServiceTest {

    private EntMeCompService entMeCompService;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    EntMeCompRepository entMeCompRepo;

    private EntMeComp entMeComp1;

    private EntMeComp entMeComp2;

    @Mock
    DozerBeanMapper dozerBeanMapper;

    @BeforeEach
    void initialize() {
        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);

        when(bulkOperations.execute()).thenReturn(result);

        entMeCompService = new EntMeCompService(template, entMeCompRepo, dozerBeanMapper);

        entMeComp1 = EntMeComp.builder()
                .id("10")
                .modificationDate("20240101")
                .build();

        entMeComp2 = EntMeComp.builder()
                .id("11")
                .build();
    }

    @Test
    @DisplayName("Refresh EntMeComp in DB OK")
    void testRefreshEntMeCompOK() {

        var entMeComps = List.of(entMeComp1, entMeComp2);

        entMeCompService.saveAll(entMeComps, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }
    @Test
    @DisplayName("Update EntMeComp in DB OK")
    void testUpdateEntMeCompOK() {

        var entMeComps = List.of(entMeComp1, entMeComp2);

        entMeCompService.saveAll(entMeComps, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh EntMeComp in DB KO")
    void testRefreshEntMeCompKO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var entMeComps = List.of(entMeComp1);

        RuntimeException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> entMeCompService.saveAll(entMeComps, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));
    }
}