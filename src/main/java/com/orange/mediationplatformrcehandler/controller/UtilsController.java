package com.MyProject.mediationplatformrcehandler.controller;

import com.MyProject.mediationplatformrcehandler.service.utils.EmailUtils;
import com.MyProject.mediationplatformrcehandler.service.utils.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.MyProject.mediationplatformrcehandler.service.utils.Utils.*;

@RestController
@RequiredArgsConstructor
public class UtilsController {
    
    private final StatsService statsService;
    private final JobExplorer jobExplorer;

    /**
     * 
     * @return String : A message of the execution result
     */
    @GetMapping("/status")
    public Object status() {
        List<String> stats = statsService.getStats();
        Map<String, String> metrics = statsService.getMetrics();

        // Add the latest job execution
        Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions("firstJob");
        runningJobExecutions.forEach(j -> metrics.put("Instance  " + j.getJobId().toString(), extractJobExecutionInfo(j)));

        //Add used memory
        metrics.put("Total memory", String.valueOf(Runtime.getRuntime().totalMemory()));
        metrics.put("Free memory", String.valueOf(Runtime.getRuntime().freeMemory()));
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        metrics.put("Used memory", String.valueOf(usedMemory));

        String statsString = listToString(stats, "======= Stats");
        String metricsString = mapToString(metrics, "====== Metrics");

        return metricsString + "\n" + statsString;
    }

}
