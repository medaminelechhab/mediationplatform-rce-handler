package com.MyProject.mediationplatformrcehandler.service.utils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;

@Slf4j
public class Utils {

    public static void cleanDir(String dir) {
        log.info("Cleaning tmp dir {} ...", dir);
        File[] tmpFiles = new File(dir).listFiles();
        for (int i = 0; tmpFiles!=null && i < tmpFiles.length; i++) {
            try {
                tmpFiles[i].delete();
            } catch (Exception e) {
                log.warn("Can't delete {} : {}", tmpFiles[i], e.getMessage());
            }
        }
    }

    public static int calculateOptimalThreads(int dataSize, int sublistSize) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int baseThreads = Math.min(availableProcessors, 16); // Cap at 16 threads

        if (dataSize < sublistSize) {
            return Math.max(1, baseThreads / 4); // Use fewer threads for small datasets
        } else if (dataSize < 100000) {
            return Math.max(2, baseThreads / 2);
        } else {
            return baseThreads;
        }
    }

    public static String mapToString(Map<String, String> map, String title) {
        String mapAsString = map.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));

        return title + ":\n" + mapAsString;
    }

    public static String listToString(List<String> list, String title) {
        String listAsString = list.stream()
                .collect(Collectors.joining("\n"));

        return title + ":\n" + listAsString;
    }

    public static String extractJobExecutionInfo(JobExecution executions) {
        return  "\n Status:" + executions.getStatus() +
                "\n StartTime:" + Objects.requireNonNull(executions.getStartTime()) +
                "\n CreateTime:" + executions.getCreateTime();
    }
}
