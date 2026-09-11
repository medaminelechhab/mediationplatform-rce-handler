package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.rce.EntScoringNoteort;
import com.MyProject.mediationplatformrcehandler.repository.EntScoringNoteortRepository;
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
class EntScoringNoteortServiceTest {

    private EntScoringNoteortService entScoringNoteortService;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    EntScoringNoteortRepository entScoringNoteortRepo;

    private EntScoringNoteort entScoringNoteort1;

    private EntScoringNoteort entScoringNoteort2;
    @Mock
    DozerBeanMapper dozerBeanMapper;

    @BeforeEach
    void initialize() {

        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        entScoringNoteortService = new EntScoringNoteortService(template ,entScoringNoteortRepo, dozerBeanMapper);

        entScoringNoteort1 = EntScoringNoteort.builder()
                .id("10")
                .modificationDate("20240101")
                .build();

        entScoringNoteort2 = EntScoringNoteort.builder()
                .id("11")
                .build();
    }

    @Test
    @DisplayName("Refresh EntScoringNoteort in DB OK")
    void testRefreshEntScoringNoteortOK() {

        var entScoringNoteorts = List.of(entScoringNoteort1, entScoringNoteort2);

        entScoringNoteortService.saveAll(entScoringNoteorts, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }
    @Test
    @DisplayName("Update EntScoringNoteort in DB OK")
    void testUpdateEntScoringNoteortOK() {

        var entScoringNoteorts = List.of(entScoringNoteort1, entScoringNoteort2);

        entScoringNoteortService.saveAll(entScoringNoteorts, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh EntScoringNoteort in DB KO")
    void testRefreshEntScoringNoteortKO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var entScoringNoteorts = List.of(entScoringNoteort1);

        IllegalStateException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> entScoringNoteortService.saveAll(entScoringNoteorts, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));
    }
}