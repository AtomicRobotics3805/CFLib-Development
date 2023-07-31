/*
    Copyright (c) 2022 Atomic Robotics (https://atomicrobotics3805.org)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package org.atomicrobotics3805.cflib.subsystems

import com.acmerobotics.roadrunner.control.PIDCoefficients
import com.acmerobotics.roadrunner.control.PIDFController
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.ElapsedTime
import com.qualcomm.robotcore.util.Range
import org.atomicrobotics3805.cflib.Command
import org.atomicrobotics3805.cflib.CommandScheduler
import org.atomicrobotics3805.cflib.Constants
import org.atomicrobotics3805.cflib.utilCommands.TelemetryCommand
import com.qualcomm.robotcore.util.RobotLog
import org.atomicrobotics3805.cflib.hardware.MotorEx
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * This class rotates a motor to a certain position. It proportionally slows down as it nears that
 * position. It starts slowing down 1 / kP ticks away from its target position.
 *
 * @param motor the motor to move
 * @param targetPosition where the motor should move to
 * @param speed how fast it should move there
 * @param requirements any Subsystems used by this command
 * @param interruptible whether this command can be interrupted or not
 * @param minError minimum error
 * @param kP multiplied by the error and speed to get the
 */
@Suppress("MemberVisibilityCanBePrivate")
open class MotorToPosition(
    protected val motor: MotorEx,
    protected val targetPosition: Int,
    protected var speed: Double,
    override val requirements: List<Subsystem> = arrayListOf(),
    override val interruptible: Boolean = true,
    protected val minError: Int = 15,
    protected val coefficients: PIDCoefficients = PIDCoefficients(0.005, 0.0, 0.0),
    protected val finish: Boolean = true
) : Command() {

    protected val timer = ElapsedTime()
    protected var lastTime: Double = 0.0
    protected val positions: MutableList<Int> = mutableListOf()
    protected val savesPerSecond = 3.0
    protected var saveTimes: MutableList<Double> = mutableListOf()
    protected val minimumChangeForStall = 20.0
    protected var controller = PIDFController(coefficients)
    protected var direction: Double = 0.0
    override val _isDone: Boolean
        get() = abs(targetPosition - motor.currentPosition) < minError && finish



    /**
     * Sets the motor's mode to RUN_USING_ENCODER, sets the error to the difference between the target and current
     * positions, and sets the direction to the sign of the error
     */
    override fun start() {
        motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        controller.targetPosition = targetPosition.toDouble()
    }

    /**
     * Updates the error and direction, then calculates and sets the motor power
     */
    override fun execute() {
        val power = controller.update(motor.currentPosition.toDouble()) * speed
        motor.power = Range.clip(power, -min(speed, 1.0), min(speed, 1.0))
        lastTime = timer.seconds()
        //cancelIfStalled()
    }

    /**
     * Stops the motor
     */
    override fun end(interrupted: Boolean) {
        motor.power = speed
        motor.mode = DcMotor.RunMode.RUN_TO_POSITION
        motor.targetPosition = motor.currentPosition
    }

    /**
     * Starts by determining whether a stall check has been performed in the past 1 / savesPerSecond seconds. If not,
     * It then compares the speed from the previous check to the current speed. If there's a change of at least
     * minimumChangeForStall times, then the motor is stalled. It sends out a telemetry message and cancels the command.
     */
    fun cancelIfStalled() {
        val lastTime = if (saveTimes.size == 0) 0.0 else saveTimes.last()
        val roundedLastTime = (lastTime * savesPerSecond).roundToInt() / savesPerSecond
        if (timer.seconds() - roundedLastTime < 1 / savesPerSecond) {
            if (positions.size > 1) {
                val lastSpeed = abs(positions[positions.size - 2] - positions[positions.size - 1])
                val currentSpeed = abs(positions[positions.size - 1] - motor.currentPosition)
                if (currentSpeed == 0 || lastSpeed / currentSpeed >= minimumChangeForStall) {
                    CommandScheduler.scheduleCommand(
                        TelemetryCommand(3.0, "Motor " + motor.name.invoke() + " Stalled!")
                    )
                    isDone = true
                }
            }
            saveTimes.add(timer.seconds())
            positions.add(motor.currentPosition)
        }

    }
}