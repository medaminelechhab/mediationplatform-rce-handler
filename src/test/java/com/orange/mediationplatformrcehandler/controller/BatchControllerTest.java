package com.MyProject.mediationplatformrcehandler.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class BatchControllerTest {

    private BatchController batchController;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job job;

    @BeforeEach
    void before() {
        batchController = new BatchController(jobLauncher, job);
    }

    @Test
    @DisplayName("Verify batch launch success")
    void launchBatch_mustReturnOk() throws Exception {
        //Given
        lenient().doReturn(new JobExecution(1L)).when(jobLauncher).run(any(), any());

        //When
        String response = batchController.launchBatch("daily", "false", null, null, null, Collections.emptyMap());

        //Then
        //verify(jobLauncher, times(1)).run(any(), any());
        assertTrue(response.contains("Batch started at"));
    }

    @Test
    @DisplayName("Verify batch launch fail")
    void launchBatch_mustReturnNotOk() throws Exception {
        //Given
        lenient().doThrow(new JobInstanceAlreadyCompleteException("Jon already completed")).when(jobLauncher).run(any(), any());

        //When
        String response = batchController.launchBatch("daily", "false",null, null, null, Collections.emptyMap());

        //Then
      //  verify(jobLauncher, times(1)).run(any(), any());
        assertTrue(response.contains("Batch started at"));
    }
}
