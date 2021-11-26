package com.translater

import com.github.rvesse.airline.SingleCommand
import com.translater.collect.properties.CollectProperties
import com.translater.common.TaskType
import com.translater.common.factory.TaskFactory
import com.translater.common.model.ExecutionTask
import com.translater.common.model.Task
import com.translater.format.properties.FormatProperties
import com.translater.generate.properties.GenerateProperties
import com.translater.translate.TranslateProperties
import com.translater.translate.yandex.properties.YandexPartConnectionProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
	value = [CollectProperties::class, FormatProperties::class, GenerateProperties::class,
		TranslateProperties::class, YandexPartConnectionProperties::class])
class Application(
	val taskFactory: TaskFactory
) : CommandLineRunner {
	override fun run(vararg args: String?) {
		val parser: SingleCommand<Task> = SingleCommand.singleCommand(Task::class.java)
		val taskForRun: Task = parser.parse(*args)

		val taskRunner: ExecutionTask = taskFactory.getTaskRunner(TaskType.valueOf(taskForRun.name!!))
		taskRunner.start()
	}
}

fun main(args: Array<String>) {
	runApplication<Application>(*args)

}
