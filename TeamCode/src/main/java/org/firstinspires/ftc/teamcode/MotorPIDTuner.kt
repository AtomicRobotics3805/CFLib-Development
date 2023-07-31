package org.firstinspires.ftc.teamcode

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.control.PIDCoefficients
import org.atomicrobotics3805.cflib.*
import org.atomicrobotics3805.cflib.subsystems.MotorToPosition
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import org.atomicrobotics3805.cflib.hardware.MotorEx
import org.atomicrobotics3805.cflib.hardware.MotorExGroup

/**
 * Untested
 * Tuning PID Loops in Game Manual 0: https://gm0.org/en/latest/docs/software/concepts/control-loops.html
 */
@Suppress("PropertyName", "PrivatePropertyName")
@TeleOp(name = "Motor PID Tuner", group = "Test")
@Config
class MotorPIDTuner : LinearOpMode() {

    companion object {
        @JvmField
        var COEFFICIENTS = PIDCoefficients(0.005, 0.0, 0.0)

        @JvmField
        var TARGET_POSITION = 40.0 // in

        @JvmField
        var NAME_1 = "lift1"

        @JvmField
        var NAME_2 = "lift2"

        @JvmField
        var PULLEY_WIDTH = 2.0 // in
    }


    private val TARGET_POSITION_TICKS: Int
        get() = motor.inchesToTicks(TARGET_POSITION, PULLEY_WIDTH)
    private val moveMotor: Command
        get() = MotorToPosition(motor, TARGET_POSITION_TICKS, 1.0,
            coefficients = COEFFICIENTS, finish = false)
    private val moveMotorToStart: Command
        get() = MotorToPosition(motor, 0, 0.5, minError = 25)

    private val motor = MotorExGroup(
        MotorEx(NAME_1, MotorEx.MotorType.ANDYMARK_NEVEREST,3.7, DcMotorSimple.Direction.FORWARD),
        MotorEx(NAME_2, MotorEx.MotorType.ANDYMARK_NEVEREST,3.7, DcMotorSimple.Direction.FORWARD))
    lateinit var gamepad: GamepadEx

    override fun runOpMode() {
        Constants.opMode = this
        gamepad = GamepadEx(gamepad1)
        CommandScheduler.registerSubsystems(TelemetryController)
        CommandScheduler.registerGamepads(gamepad)
        TelemetryController.telemetry.addLine("Initializing")
        TelemetryController.telemetry.update()
        var tuningMotor = false
        motor.initialize()
        TelemetryController.telemetry.addLine("Ready to Start")
        TelemetryController.telemetry.update()
        waitForStart()
        while (opModeIsActive()) {
            if (gamepad.a.pressed) {
                CommandScheduler.cancelAll()
                CommandScheduler.scheduleCommand(moveMotor)
                tuningMotor = true
            }
            if (gamepad.b.pressed) {
                CommandScheduler.cancelAll()
                CommandScheduler.scheduleCommand(moveMotorToStart)
                tuningMotor = false
            }
            TelemetryController.telemetry.addData("Current Motor Position", motor.currentPosition)
            if (tuningMotor) {
                TelemetryController.telemetry.addData("Target Motor Position", TARGET_POSITION_TICKS)
                TelemetryController.telemetry.addLine("Press B to move the motor back to its starting position")
            } else {
                TelemetryController.telemetry.addData("Target Motor Position", 0)
                TelemetryController.telemetry.addLine("Press A to move the motor")
            }
            CommandScheduler.run()
        }
    }
}