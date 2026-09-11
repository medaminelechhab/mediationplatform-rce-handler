package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.database.RefRCEData;
import com.MyProject.mediationplatformrcehandler.model.rce.RefRCE;
import com.MyProject.mediationplatformrcehandler.repository.RefRCERepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefRCEServiceTest {

    private RefRCEService refRCEService;

    
    @Mock
    private BulkOperations bulkOperations;

    @Mock
    RefRCERepository refRCERepository;
    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    private BulkWriteResult result;

    RefRCEData refRCEData;

    RefRCE refRCE;

    @BeforeEach
    public void init() {
        refRCEService = new RefRCEService(refRCERepository, mongoTemplate);

        refRCEData = RefRCEData.builder()
                .referenceName("EDG_TEST")
                .code("TEST")
                .label("TEST_LABEL")
                .build();

        refRCE = RefRCE.builder()
                .referenceName("EDG_TEST")
                .code("TEST")
                .label("TEST_LABEL")
                .build();
    }

    @Test
    @DisplayName("Refresh REF-RCE in DB OK")
    void testRefreshGroupCCIALCompOK() {

        when(mongoTemplate.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        List<RefRCE> refs = new ArrayList<>();
        refs.add(new RefRCE());
        refs.add(new RefRCE());
        refs.add(new RefRCE());

        refRCEService.saveAll(refs, true);

        verify(mongoTemplate).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(1)).insert(any(AuditMetadata.class));
    }

    @Test
    @DisplayName("Refresh REF-RCE in DB KO")
    void testRefreshGroupCCIALCompKo() {

        when(mongoTemplate.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        List<RefRCE> refs = new ArrayList<>();
        refs.add(new RefRCE());
        refs.add(new RefRCE());
        refs.add(new RefRCE());

        IllegalStateException exp = Assertions.assertThrows(
            IllegalStateException.class,
            () -> refRCEService.saveAll(refs, true),
            "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));
    }

    @Test
    @DisplayName("When retrieve all ref-rce called, we must return all documents from collection")
    void retrieveAllReferencesRCE_mustRetrieveExistentReferencesRCE() {
        // Given
        Mockito.when(refRCERepository.findAll()).thenReturn(List.of(refRCEData));

        // When
        var referencesRCE = refRCEService.retrieveAllReferencesRCE();

        // Then
        verify(refRCERepository, times(1)).findAll();
        assertEquals(1, referencesRCE.size());
        assertEquals("TEST", referencesRCE.get(0).getCode());
        assertEquals("EDG_TEST", referencesRCE.get(0).getReferenceName());
    }
}
