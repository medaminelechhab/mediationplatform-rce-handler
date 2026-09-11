package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.rce.EntEcoNew;
import com.MyProject.mediationplatformrcehandler.repository.EntEcoNewRepository;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class EntEcoNewServiceTest {

    private EntEcoNewService entEcoNewService;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    EntEcoNewRepository entEcoNewRepo;

    EntEcoNew entEcoNew1;

    EntEcoNew entEcoNew2;

    @Mock
    DozerBeanMapper dozerBeanMapper;

    @BeforeEach
    void initialize() {
        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);

        when(bulkOperations.execute()).thenReturn(result);

        entEcoNewService = new EntEcoNewService(template, entEcoNewRepo, dozerBeanMapper);

        entEcoNew1 = EntEcoNew.builder()
                .id("10")
                .modificationDate("20240101")
                .build();

        entEcoNew2 = EntEcoNew.builder()
                .id("11")
                .build();
    }

    @Test
    @DisplayName("Refresh EntEcoNew in DB OK")
    void testRefreshEntEcoNewOK() {

        var entEcoNews = List.of(entEcoNew1, entEcoNew2);

        entEcoNewService.saveAll(entEcoNews, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }

    @Test
    @DisplayName("Refresh EntEcoNew in DB KO")
    void testRefreshEntEcoNewKO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var entEcoNews = List.of(entEcoNew1);

        RuntimeException exp = Assertions.assertThrows(
                RuntimeException.class,
                () -> entEcoNewService.saveAll(entEcoNews, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));
    }
}
