package org.atomicrobotics3805.cflib.utilCommands

import org.atomicrobotics3805.cflib.Command
import org.atomicrobotics3805.cflib.CommandScheduler

/**
 * This command is used as an "if" or "switch" statement that selects between 1 or more commands, depending on the value of a variable.
 * @param value This is a lambda that returns the value that must be associated with a command for it to be run
 * @param outcomes Connects all the commands to possible conditions of the value.
 */
class OptionCommand(
    private val value: () -> Any,
    private vararg val outcomes: Pair<Any, Command>
) : Command() {
    override val _isDone: Boolean
        get() = true

    override fun start() {
        val invokedValue: Any = value.invoke()
        outcomes.forEach {
            if (invokedValue == it.first) {
                CommandScheduler.scheduleCommand(it.second)
            }
        }
    }
}