package com.MyProject.mediationplatformrcehandler.configuration;

import com.MyProject.mediationplatformrcehandler.service.BackupService;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Date;

@Configuration
@EnableScheduling
@Slf4j
@AllArgsConstructor
public class ScheduledConfig {

    private static final String TYPE_MANAGEMENT = "typeManagement";

    private final JobLauncher jobLauncher;
    private final BackupService backupService;

    private final Job job;

    @Scheduled(cron = "${clinksplatform.customerlinks.rce.batch.daily-cron}", zone = "Europe/Paris")
    public void launchDailyBatch() throws Exception {
        log.info("DAILY BATCH launched at: {}", LocalDateTime.now());
        jobLauncher.run(job, new JobParametersBuilder()
                .addString(TYPE_MANAGEMENT, FilesManagement.TypeManagement.DAILY.toString())
                .addDate("timestamp", new Date())
                .toJobParameters());
    }

    @Scheduled(cron = "${clinksplatform.customerlinks.rce.batch.weekly-cron}", zone = "Europe/Paris")
    public void launchWeeklyBatch() throws Exception {
        log.info("WEEKLY BATCH launched at: {}", LocalDateTime.now());
        jobLauncher.run(job, new JobParametersBuilder()
                .addString(TYPE_MANAGEMENT, FilesManagement.TypeManagement.SUNDAY.toString())
                .addDate("timestamp", new Date())
                .toJobParameters());
    }

    @Scheduled(cron = "${clinksplatform.customerlinks.rce.rce-backup-cron}", zone = "Europe/Paris")
    public void launchBackupProcess() throws Exception {
        log.info("Backup Process Started at: {}", LocalDateTime.now());
        backupService.startBackupProcess();
        log.info("Backup Process completed successfully");
    }
}
