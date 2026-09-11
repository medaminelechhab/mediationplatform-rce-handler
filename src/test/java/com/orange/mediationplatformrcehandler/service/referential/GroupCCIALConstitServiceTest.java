package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.MyProject.mediationplatformrcehandler.model.database.AuditMetadata;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALConstit;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALConstitRepository;
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
class GroupCCIALConstitServiceTest {

    private GroupCCIALConstitService groupCCIALConstitService;

    @Mock
    private MongoTemplate template;

    @Mock
    private BulkOperations bulkOperations;

    @Mock
    private BulkWriteResult result;

    @Mock
    GroupCCIALConstitRepository groupCCIALConstitRepo;

    private GroupCCIALConstit groupCCIALConstit1;

    private GroupCCIALConstit groupCCIALConstit2;
    @Mock
    DozerBeanMapper dozerBeanMapper;

    @BeforeEach
    void initialize() {
        when(template.bulkOps(any(), any(Class.class))).thenReturn(bulkOperations);
        when(bulkOperations.execute()).thenReturn(result);

        groupCCIALConstitService = new GroupCCIALConstitService(template, groupCCIALConstitRepo, dozerBeanMapper);

        groupCCIALConstit1 = GroupCCIALConstit.builder()
                .idENT("10")
                .modificationDate("20240101")
                .build();

        groupCCIALConstit2 = GroupCCIALConstit.builder()
                .idENT("11")
                .build();
    }

    @Test
    @DisplayName("Refresh GroupCCIALConstit in DB OK")
    void testRefreshGroupCCIALConstitOK() {

        var groupCCIALConstits = List.of(groupCCIALConstit1, groupCCIALConstit2);

        groupCCIALConstitService.saveAll(groupCCIALConstits, true);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).insert(any(AuditMetadata.class));
    }
    @Test
    @DisplayName("Update GroupCCIALConstit in DB OK")
    void testUpdateGroupCCIALConstitOK() {

        var groupCCIALConstits = List.of(groupCCIALConstit1, groupCCIALConstit2);

        groupCCIALConstitService.saveAll(groupCCIALConstits, false);

        verify(template).bulkOps(any(), any(Class.class));
        verify(bulkOperations, times(2)).upsert(any(Query.class), any(Update.class));
    }

    @Test
    @DisplayName("Refresh GroupCCIALConstit in DB KO")
    void testRefreshGroupCCIALConstitKO() {

        when(bulkOperations.execute()).thenThrow(RuntimeException.class);

        var groupCCIALConstits = List.of(groupCCIALConstit1);

        IllegalStateException exp = Assertions.assertThrows(
                IllegalStateException.class,
                () -> groupCCIALConstitService.saveAll(groupCCIALConstits, true),
                "Expected doThing() to throw, but it didn't"
        );

        assertTrue(exp.getMessage().contains("Failed to feed"));
    }
}