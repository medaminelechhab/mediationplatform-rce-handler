package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.rce.EntMeOrg;
import com.MyProject.mediationplatformrcehandler.repository.EntMeOrgRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
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
class EntMeOrgServiceTest {

    private EntMeOrgService entMeOrgService;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    EntMeOrgRepository entMeOrgRepo;

    private EntMeOrg entMeOrg1;

    private EntMeOrg entMeOrg2;


    @BeforeEach
    void initialize() {

        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        entMeOrgService = new EntMeOrgService(template, entMeOrgRepo, new DozerBeanMapper());

        entMeOrg1 = EntMeOrg.builder()
                .id("10")
                .modificationDate("20240101")
                .idEnt("0001")
                .build();

        entMeOrg2 = EntMeOrg.builder()
                .id("11")
                .idEnt("0002")
                .modificationDate("20240101")
                .build();
    }

    @Test
    @DisplayName("Refresh EntMeOrg in DB OK")
    void testRefreshEntMeOrgOK() {

        var entMeOrgs = List.of(entMeOrg1, entMeOrg2);

        entMeOrgService.saveAll(entMeOrgs, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }
    @Test
    @DisplayName("Update EntMeOrg in DB OK")
    void testUpdateEntMeOrgOK() {

        var entMeOrgs = List.of(entMeOrg1, entMeOrg2);

        entMeOrgService.saveAll(entMeOrgs, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh EntMeOrg in DB KO")
    void testRefreshEntMeOrgKO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var entMeOrgs = List.of(entMeOrg1);

        IllegalStateException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> entMeOrgService.saveAll(entMeOrgs, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));
    }
}