package com.MyProject.mediationplatformrcehandler.configuration;

import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.mapper.EntrepriseMapper;
import com.MyProject.mediationplatformrcehandler.mapper.EstablishMapper;
import com.MyProject.mediationplatformrcehandler.mapper.GroupeMapper;
import com.MyProject.mediationplatformrcehandler.service.AccountTasklet;
import com.MyProject.mediationplatformrcehandler.service.CacheService;
import com.MyProject.mediationplatformrcehandler.service.PersistenceService;
import com.MyProject.mediationplatformrcehandler.service.efiles.EFilesService;
import com.MyProject.mediationplatformrcehandler.service.efiles.FileContentService;
import com.MyProject.mediationplatformrcehandler.service.referential.*;
import com.MyProject.mediationplatformrcehandler.service.utils.EmailUtils;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.service.utils.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {

    private final RCEService rceService;
    private final StatsService statsService;
    private GroupeMapper groupeMapper;
    private EntrepriseMapper entrepriseMapper;
    private EstablishMapper establishMapper;
    private final FileContentService fileContentService;
    private final EFilesService eFilesService;
    private final ErrorHandler errorHandler;
    private final EmailUtils emailUtils;
    private final PersistenceService persistenceService;
    private final RefRCEService refRCEService;
    private final EntRefXL2Service entRefXl2Service;
    private final EtaMeOrgNewService etaMeOrgNewService;
    private final EntEcoNewService entEcoNewService;
    private final EntScoringNoteortService entScoringNoteortService;
    private final EntMeCompService entMeCompService;
    private final EntMeOrgService entMeOrgService;
    private final GroupCCIALConstitService groupCCIALConstitService;
    private final GroupCCIALCompService groupCCIALCompService;
    private final GroupCCIALOrgService groupCCIALOrgService;
    private final EtaRefXL2Service etaRefXl2Service;
    private final CacheService cacheService;


    @Bean
    public Job taskletJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("firstJob", jobRepository).start(step(jobRepository, transactionManager)).build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("Step 1", jobRepository)
                .tasklet(tasklet(), transactionManager)
                .build();
    }

    @Bean
    public AccountTasklet tasklet() {
        return new AccountTasklet(statsService, groupeMapper, entrepriseMapper, establishMapper, rceService, fileContentService, eFilesService, errorHandler, emailUtils, persistenceService,
                refRCEService, entRefXl2Service, etaMeOrgNewService, entEcoNewService, entScoringNoteortService, entMeCompService, entMeOrgService, groupCCIALConstitService,
                groupCCIALCompService, groupCCIALOrgService, etaRefXl2Service, cacheService);
    }
}
