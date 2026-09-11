package com.MyProject.mediationplatformrcehandler.service.referential;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALOrgData;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALOrg;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALOrgRepository;
import java.util.List;
import java.util.Optional;

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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class GroupCCIALOrgServiceTest {

    private GroupCCIALOrgService groupCCIALOrgService;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    GroupCCIALOrgRepository groupCCIALOrgRepo;

    private GroupCCIALOrg groupCCIALOrg1;

    private GroupCCIALOrg groupCCIALOrg2;

    private GroupCCIALOrgData groupCCIALOrgData;


    @BeforeEach
    void initialize() {
        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        groupCCIALOrgService = new GroupCCIALOrgService(template, groupCCIALOrgRepo, new DozerBeanMapper());

        groupCCIALOrg1 = GroupCCIALOrg.builder()
                .idGroup("10")
                .modificationDate("20240101")
                .build();

        groupCCIALOrg2 = GroupCCIALOrg.builder()
                .idGroup("11")
                .build();

        groupCCIALOrgData = GroupCCIALOrgData.builder()
            .idGroup("10")
            .build();
    }

    @Test
    @DisplayName("When search by rceId then return the DB object")
    void testOK() {

        Mockito.when(groupCCIALOrgRepo.findByIdGroup(anyString())).thenReturn(Optional.of(groupCCIALOrgData));

        var group = groupCCIALOrgService.getByIdGroup("10");

        verify(groupCCIALOrgRepo, times(1)).findByIdGroup("10");

        assertEquals(group.getIdGroup(), groupCCIALOrgData.getIdGroup());
    }

    @Test
    @DisplayName("When search by rceId then return a null object")
    void testNotfound() {

        Mockito.when(groupCCIALOrgRepo.findByIdGroup(anyString())).thenReturn(Optional.empty());

        var group = groupCCIALOrgService.getByIdGroup("10");

        verify(groupCCIALOrgRepo, times(1)).findByIdGroup("10");

        assertNull(group);
    }

    @Test
    @DisplayName("Refresh GroupCCIALOrg in DB OK")
    void testRefreshGroupCCIALOrgOK() {

        var groupCCIALOrgs = List.of(groupCCIALOrg1, groupCCIALOrg2);

        groupCCIALOrgService.saveAll(groupCCIALOrgs, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }
    @Test
    @DisplayName("Update GroupCCIALOrg in DB OK")
    void testUpdateGroupCCIALOrgOK() {

        var groupCCIALOrgs = List.of(groupCCIALOrg1, groupCCIALOrg2);

        groupCCIALOrgService.saveAll(groupCCIALOrgs, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh GroupCCIALOrg in DB KO")
    void testRefreshGroupCCIALOrgKO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var groupCCIALOrgs = List.of(groupCCIALOrg1);

        IllegalStateException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> groupCCIALOrgService.saveAll(groupCCIALOrgs, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));
    }
}