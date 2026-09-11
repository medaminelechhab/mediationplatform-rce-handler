package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALComp;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALCompRepository;
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
class GroupCCIALCompServiceTest {

    private GroupCCIALCompService groupCCIALCompService;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    GroupCCIALCompRepository groupCCIALCompRepo;

    private GroupCCIALComp groupCCIALComp1;

    private GroupCCIALComp groupCCIALComp2;
    @Mock
    DozerBeanMapper dozerBeanMapper;

    @BeforeEach
    void initialize() {
        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        groupCCIALCompService = new GroupCCIALCompService(template, groupCCIALCompRepo, dozerBeanMapper);

        groupCCIALComp1 = GroupCCIALComp.builder()
                .idGroup("10")
                .modificationDate("20240101")
                .build();

        groupCCIALComp2 = GroupCCIALComp.builder()
                .idGroup("11")
                .build();
    }

    @Test
    @DisplayName("Refresh GroupCCIALComp in DB OK")
    void testRefreshGroupCCIALCompOK() {

        var groupCCIALComps = List.of(groupCCIALComp1, groupCCIALComp2);

        groupCCIALCompService.saveAll(groupCCIALComps, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }
    @Test
    @DisplayName("Update GroupCCIALComp in DB OK")
    void testUpdateGroupCCIALCompOK() {

        var groupCCIALComps = List.of(groupCCIALComp1, groupCCIALComp2);

        groupCCIALCompService.saveAll(groupCCIALComps, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh GroupCCIALComp in DB KO")
    void testRefreshGroupCCIALCompKO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var groupCCIALComps = List.of(groupCCIALComp1);

        IllegalStateException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> groupCCIALCompService.saveAll(groupCCIALComps, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));
    }
}