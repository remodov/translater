package ru.translater.repeare;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.translater.common.ExecutionTask;

@Slf4j
@RequiredArgsConstructor
@Component
public class RepairTask implements ExecutionTask {
    private final RepairProperties repairProperties;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public void start() {
        log.info("Start Repair task.");
        log.info("End Repair task.");
    }
}
