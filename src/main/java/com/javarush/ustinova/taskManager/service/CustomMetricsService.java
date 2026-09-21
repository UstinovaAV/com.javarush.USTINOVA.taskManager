package com.javarush.ustinova.taskManager.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class CustomMetricsService {
    private final Counter customCounter;

    public CustomMetricsService(MeterRegistry registry){
        customCounter = registry.counter(
                "custom.metric.counter",
                "action", "performAction",
                "component", "CustomMetricsService",
                "status", "success"
        );
    }
    public void performAction(){
        customCounter.increment();
    }
}
