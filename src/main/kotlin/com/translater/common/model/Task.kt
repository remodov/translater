package com.translater.common.model

import com.github.rvesse.airline.HelpOption
import com.github.rvesse.airline.annotations.Command
import com.github.rvesse.airline.annotations.Option
import javax.inject.Inject

@Command(name = "task", description = "Task for execution")
data class Task(
    @Inject
    var help: HelpOption<Task?>? = null,

    @Option(name = ["-n", "--name"], title = ["Name"], arity = 1, description = "An option that takes an argument")
    var name: String? = null,
)