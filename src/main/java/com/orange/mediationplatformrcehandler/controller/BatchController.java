package com.MyProject.mediationplatformrcehandler.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement.TypeManagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private final JobLauncher jobLauncher;

    private final Job job;

    private final List<String> whiteList = List.of("sender","creationDate.lt","creationDate.gt",
            "offset", "tag", "recipient", "limit", "role");

    /**
     *
     * @return String : A message of the execution result
     */
    @GetMapping("/launch/{mode}")
    public String launchBatch(@PathVariable String mode, @RequestParam(required = false) String updateCustomerLinks,
                              @RequestParam(required = false) String filteredGroups,
                              @RequestParam(required = false) String filteredEntreprises,
                              @RequestParam(required = false) String filteredEtablissements,
                              @RequestParam(required = false) Map<String, String> params) {

        if (!mode.toUpperCase().equals(TypeManagement.SUNDAY.toString())
                && !mode.toUpperCase().equals(TypeManagement.DAILY.toString())){
            return "Nothing started, mode variable must be specified in url path (DAILY or SUNDAY)";
        }

        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        params.forEach((p,v) -> {
            if (whiteList.contains(p))
                parametersBuilder.addString("custom_" + p, v);
        });

        JobParameters customParams = parametersBuilder.toJobParameters();
        log.info("Custom params : {}", customParams);

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder()
                .addDate("timestamp", new Date())
                .addString("forceRun", "true")
                .addString("typeManagement", mode)
                .addString("updateCustomerLinks", updateCustomerLinks!=null?updateCustomerLinks.toLowerCase():"true")
                .addString("filteredGroups", filteredGroups!=null?filteredGroups:"")
                .addString("filteredEntreprises", filteredEntreprises!=null?filteredEntreprises:"")
                .addString("filteredEtablissements", filteredEtablissements!=null?filteredEtablissements:"")
                .addJobParameters(customParams);
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();

        TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.execute(new Runnable() {
            public void run() {
                try {
                    jobLauncher.run(job, jobParameters);
                } catch (Exception e){
                    throw new IllegalStateException("Error while processing batch. Reason : " + e.getMessage());
                }
            }
        });

        return "<b style=\"color:green;font-size:26\">OK, Batch started at " + LocalDateTime.now() + ". Please check logs or mail for status.</b>";

    }
}
