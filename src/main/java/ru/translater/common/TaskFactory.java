package ru.translater.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.translater.collect.CollectTask;
import ru.translater.format.FormatTask;
import ru.translater.generate.GenerateTask;
import ru.translater.translate.TranslateTask;

@RequiredArgsConstructor
@Component
public class TaskFactory {
    private final CollectTask collectTask;
    private final FormatTask formatTask;
    private final GenerateTask generateTask;
    private final TranslateTask translateTask;

    public ExecutionTask getTaskRunner(TaskType taskType) {
        switch (taskType) {
            case COLLECT:
                return collectTask;
            case FORMAT:
                return formatTask;
            case TRANSLATE:
                return translateTask;
            case GENERATE:
                return generateTask;
        }

        throw new IllegalArgumentException("TaskType: " + taskType.name() + " not found!");
    }
}
