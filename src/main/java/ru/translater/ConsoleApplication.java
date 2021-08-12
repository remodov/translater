package ru.translater;

import com.github.rvesse.airline.SingleCommand;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.translater.common.ExecutionTask;
import ru.translater.common.Task;
import ru.translater.common.TaskFactory;
import ru.translater.common.TaskType;


@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ConsoleApplication implements CommandLineRunner {
    private final TaskFactory taskFactory;

    public static void main(String[] args) {
        log.info("STARTING THE APPLICATION");
        SpringApplication.run(ConsoleApplication.class, args);
        log.info("APPLICATION FINISHED");
    }

    @SneakyThrows
    @Override
    public void run(String... args) {
        SingleCommand<Task> parser = SingleCommand.singleCommand(Task.class);
        Task taskForRun = parser.parse(args);

        ExecutionTask taskRunner = taskFactory.getTaskRunner(TaskType.valueOf(taskForRun.getName()));
        taskRunner.start();
    }
}
