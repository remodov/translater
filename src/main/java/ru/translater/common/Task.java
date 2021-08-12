package ru.translater.common;

import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import lombok.Data;

import javax.inject.Inject;

@Data
@Command(name = "task", description = "Task for execution")
public class Task {
    @Inject
    private HelpOption<Task> help;

    @Option(name = {"-n", "--name"}, title = "Name", arity = 1, description = "An option that takes an argument")
    private String name;
}
