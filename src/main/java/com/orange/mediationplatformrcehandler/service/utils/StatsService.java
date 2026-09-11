package com.MyProject.mediationplatformrcehandler.service.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class StatsService {

    private List<String> stats = new ArrayList<>();
    private Map<String, String> metrics = new ConcurrentHashMap<>();

    public void putMetric(String key, String value) {
        metrics.put(key, value);
    }

    public void addStat(String value) {
        stats.add(value);
    }

    public void reset() {
        this.metrics = new HashMap<>();
        this.stats = new ArrayList<>();
    }
}
