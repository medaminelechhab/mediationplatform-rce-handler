package com.MyProject.mediationplatformrcehandler.controller;

import com.MyProject.mediationplatformrcehandler.service.utils.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class UtilsControllerTest {

    private UtilsController utilsController;

    @Mock
    private StatsService statsService;

    @Mock
    JobExplorer jobExplorer;

    @BeforeEach
    void before() {
        utilsController = new UtilsController(statsService, jobExplorer);
    }

    @Test
    @DisplayName("Verify status ok")
    void sendEmail_mustReturnOk() {
        //Given
        when(statsService.getStats()).thenReturn(new ArrayList<>());

        //When
        String response = (String) utilsController.status();

        //Then
        verify(statsService, times(1)).getStats();
        verify(statsService, times(1)).getMetrics();
        assertNotNull(response);
        assertTrue(response.contains("Stats"));
        assertTrue(response.contains("Metrics"));
    }
}
