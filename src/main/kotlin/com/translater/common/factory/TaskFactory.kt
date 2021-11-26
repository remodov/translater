package com.translater.common.factory

import com.translater.collect.service.CollectTaskService
import com.translater.common.TaskType
import com.translater.common.model.ExecutionTask
import com.translater.format.service.FormatTaskService
import com.translater.generate.service.GenerateTaskService
import com.translater.translate.service.TranslateTaskService
import org.springframework.stereotype.Component

@Component
class TaskFactory(
    val collectTaskService: CollectTaskService,
    val formatTaskService: FormatTaskService,
    val generateTaskService: GenerateTaskService,
    val translateTaskService: TranslateTaskService
) {
    fun getTaskRunner(taskType: TaskType): ExecutionTask {
        return when (taskType) {
            TaskType.COLLECT -> collectTaskService
            TaskType.FORMAT-> formatTaskService
            TaskType.GENERATE -> generateTaskService
            TaskType.TRANSLATE -> translateTaskService
            else -> collectTaskService
        }
    }
}