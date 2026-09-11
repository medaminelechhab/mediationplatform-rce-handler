package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.database.EntRefXl2Data;
import com.MyProject.mediationplatformrcehandler.model.rce.EntRefXL;
import com.MyProject.mediationplatformrcehandler.repository.EntRefXl2Repository;
import org.dozer.DozerBeanMapper;
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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class EntRefXl2ServiceTest {

    EntRefXL2Service entRefXl2Service;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    EntRefXl2Repository entRefXl2Repo;

    EntRefXl2Data entRefXl2Data;

    EntRefXL entRefXL;

    EntRefXL entRefXL2;

    @BeforeEach
     void init() {

        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        entRefXl2Service = new EntRefXL2Service(template, entRefXl2Repo, new DozerBeanMapper());

        entRefXl2Data = EntRefXl2Data.builder()
                .id("000001")
                .siren("siren0001")
                .build();

        entRefXL = EntRefXL.builder()
                .id("000001")
                .siren("siren0001")
                .build();

        entRefXL2 = EntRefXL.builder()
                .id("000002")
                .build();

    }

    @Test
    @DisplayName("When search by rceId then return the DB object")
    void testOK() {

        Mockito.when(entRefXl2Repo.findById(anyString())).thenReturn(Optional.of(entRefXl2Data));

        var entRef = entRefXl2Service.getByIdEnt("00001");

        verify(entRefXl2Repo, times(1)).findById("00001");

        assertEquals(entRef.getSiren(), entRefXl2Data.getSiren());
    }

    @Test
    @DisplayName("When search by rceId then return a null object")
    void testNotfound() {

        Mockito.when(entRefXl2Repo.findById(anyString())).thenReturn(Optional.empty());

        var entRef = entRefXl2Service.getByIdEnt("00001");

        verify(entRefXl2Repo, times(1)).findById("00001");

        assertNull(entRef);
    }

    @Test
    @DisplayName("Refresh EntRefXl2 in DB OK")
    void testRefreshEntRefXl2OK() {

        var entRefXl2s = List.of(entRefXL, entRefXL2);

        entRefXl2Service.saveAll(entRefXl2s, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }
    @Test
    @DisplayName("Update EntRefXl2 in DB OK")
    void testUpdateEntRefXl2OK() {

        var entRefXl2s = List.of(entRefXL, entRefXL2);

        entRefXl2Service.saveAll(entRefXl2s, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh EntRefXl2 in DB KO") //TODO: need to get that test KO
    void testRefreshEntRefXl2KO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var entRefXl2s = List.of(entRefXL);

        entRefXl2Service.saveAll(entRefXl2s, true);
        /*
        IllegalStateException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> entRefXl2Service.saveAll(entRefXl2s, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));*/
    }
}
