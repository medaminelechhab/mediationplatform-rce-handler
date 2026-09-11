package com.MyProject.mediationplatformrcehandler.service;

import com.MyProject.mediationplatform.common.model.customerlinks.rce.Account;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.MediationplatformRceHandlerApplication;
import com.MyProject.mediationplatformrcehandler.configuration.BatchConfiguration;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import com.MyProject.mediationplatformrcehandler.service.efiles.EFilesService;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBatchTest
@SpringBootTest
@SpringJUnitConfig({MediationplatformRceHandlerApplication.class, BatchConfiguration.class})
@EnabledIf(expression = "#{environment['spring.profiles.active'] == 'integration'}", loadContext = true)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class JobLauncherIntTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;
  @Autowired
  private EFilesService eFilesService;
  @Autowired
  private RCEService rceService;
  @Value("${clinksplatform.customerlinks.rce.role:recipient}")
  private String role;

  @Value("#{${clinksplatform.efiles.connection.query-params}}")
  private HashMap<String, Object> eFilesParams;

  @BeforeEach
  void setUp() {
    Assumptions.assumeTrue(isEFilesDataExist(), "No files found for today's date");
  }

  private boolean isEFilesDataExist() {
    var files = eFilesService.getFiles(eFilesParams, null).block();
    List<String> operationIds = files.stream()
        .map(EFile::getOperationId)
        .toList();
    if (operationIds.size() > 0) {
      return true;
    }
    return false;
  }

  @Test
  @DisplayName("GIVEN we have valid files WHEN jobLaunched THEN map Group, Enterprise and Establishment to CL")
  void shouldReadFromEFilesAndMapToCL() throws Exception {

    // Given
    var jobParameters = new JobParametersBuilder()
        .addString("typeManagement", FilesManagement.TypeManagement.DAILY.toString())
        .toJobParameters();

    // When
    JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

    // Then
    assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

    // Verify creation use case into Customer Links - Group
    var createdGroupe = rceService.getAccountByRceID("Groupe", "012345").block();
    Assertions.assertNotNull(createdGroupe.getRecords());
    Account createdGroupeAccount = createdGroupe.getRecords().get(0);
    assertEquals("groupe_creation_test",
        createdGroupeAccount.getName());

      // Verify creation use cases into Customer Links - Enterprise
    var createdEnterprise = rceService.getAccountByRceID("Entreprise", "01234567").block();
    Assertions.assertNotNull(createdEnterprise.getRecords());
    Account createdEnterpriseAccount = createdEnterprise.getRecords().get(0);
    assertEquals("entreprise_creation_test",
        createdEnterpriseAccount.getName());

       // Verify creation use cases into Customer Links - Establishment
    var createdEstablishment = rceService.getAccountByRceID("Etablissement", "76543210").block();
    Assertions.assertNotNull(createdEstablishment.getRecords());
    Account createdEstablishmentAccount = createdEstablishment.getRecords().get(0);
    assertEquals("etablissement_creation_test",
        createdEstablishmentAccount.getName());
  }
}
