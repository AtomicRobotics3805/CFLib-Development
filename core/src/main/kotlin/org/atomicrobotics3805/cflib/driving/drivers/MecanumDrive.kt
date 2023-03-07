
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




package org.atomicrobotics3805.cflib.driving.drivers

import com.acmerobotics.roadrunner.drive.DriveSignal
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.kinematics.Kinematics
import com.acmerobotics.roadrunner.kinematics.MecanumKinematics
import com.acmerobotics.roadrunner.trajectory.constraints.AngularVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.MinVelocityConstraint
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DcMotor.RunMode
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior
import org.atomicrobotics3805.cflib.Command
import org.atomicrobotics3805.cflib.driving.DriverControlled
import org.atomicrobotics3805.cflib.driving.MecanumDriveConstants
import org.atomicrobotics3805.cflib.driving.localizers.SubsystemLocalizer
import org.atomicrobotics3805.cflib.hardware.MotorEx
import java.util.*

/**
 * This object controls the movement of a mecanum drivetrain. It's pretty complicated, so it might
 * be tough to understand everything at first. The normal wrapping & spacing rules aren't always
 * followed because they can make the file more difficult to understand.
 *
 * If you want to have multiple Mecanum Drives in the same project, you don't need to make another
 * copy of this object. You just need to make another object that inherits DriveConstants and set
 * that as the one used in the Constants file.
 */
@Suppress("unused")
class MecanumDrive(val mecanumDriveConstants: MecanumDriveConstants,
                   localizer: SubsystemLocalizer,
                   startPose: () -> Pose2d = { Pose2d() }
) : Driver(mecanumDriveConstants, localizer, startPose) {

    // this constraint is used when building trajectories to determine how fast the robot will go
    override val velConstraint: MinVelocityConstraint
        get() = MinVelocityConstraint(listOf(
            AngularVelocityConstraint(mecanumDriveConstants.MAX_ANG_VEL),
            MecanumVelocityConstraint(mecanumDriveConstants.MAX_VEL, mecanumDriveConstants.TRACK_WIDTH)
        ))

    // drive motors, battery voltage sensor, IMU, and the OpMode's hardwareMap
    private val leftFront = MotorEx(mecanumDriveConstants.LEFT_FRONT_NAME, mecanumDriveConstants.MOTOR_TYPE, mecanumDriveConstants.TOTAL_GEAR_RATIO)
    private val leftBack = MotorEx(mecanumDriveConstants.LEFT_BACK_NAME, mecanumDriveConstants.MOTOR_TYPE, mecanumDriveConstants.TOTAL_GEAR_RATIO)
    private val rightFront = MotorEx(mecanumDriveConstants.RIGHT_FRONT_NAME, mecanumDriveConstants.MOTOR_TYPE, mecanumDriveConstants.TOTAL_GEAR_RATIO)
    private val rightBack = MotorEx(mecanumDriveConstants.RIGHT_BACK_NAME, mecanumDriveConstants.MOTOR_TYPE, mecanumDriveConstants.TOTAL_GEAR_RATIO)
    private lateinit var motors: List<MotorEx>

    override val rawExternalHeading: Double
        get() = imu.angularOrientation.firstAngle.toDouble()
    // To work around an SDK bug, use -zRotationRate in place of xRotationRate
    // and -xRotationRate in place of zRotationRate (yRotationRate behaves as
    // expected). This bug does NOT affect orientation.
    //
    // See https://github.com/FIRST-Tech-Challenge/FtcRobotController/issues/251 for details.
    override val externalHeadingVelocity: Double
        get() = -imu.angularVelocity.xRotationRate.toDouble()

    /**
     * Allows the drivers to control the drivetrain using a gamepad
     * @param gamepad the gamepad that controls the drivetrain
     */
    override fun driverControlled(gamepad: Gamepad): Command = DriverControlled(
        gamepad, listOf(this), true, (mecanumDriveConstants as MecanumDriveConstants).POV,
        mecanumDriveConstants.REVERSE_STRAFE, mecanumDriveConstants.REVERSE_STRAIGHT, mecanumDriveConstants.REVERSE_TURN)

    /**
     * Initializes the drivetrain. This includes initializing the IMU, motor, and the battery
     * voltage sensor.
     */
    override fun initialize() {
        super.initialize()
        // initializes the motors
        mecanumDriveConstants as MecanumDriveConstants
        leftFront.initialize()
        leftBack.initialize()
        rightBack.initialize()
        rightFront.initialize()
        motors = listOf(leftFront, leftBack, rightBack, rightFront)
        // sets the achieveableMaxRPMFraction for each motor to 1.0
        for (motor in motors) {
            val motorConfigurationType = motor.motor.motorType.clone()
            motorConfigurationType.achieveableMaxRPMFraction = 1.0
            motor.motor.motorType = motorConfigurationType
        }
        // sets the RunMode for each motor
        if (mecanumDriveConstants.IS_RUN_USING_ENCODER) {
            for (motor in motors) {
                motor.motor.mode = RunMode.STOP_AND_RESET_ENCODER
                motor.motor.mode = RunMode.RUN_USING_ENCODER
            }
            // sets the motors' PIDFCoefficients
            setPIDFCoefficients(mecanumDriveConstants.MOTOR_VEL_PID)
        }
        else {
            for (motor in motors) {
                motor.motor.mode = RunMode.RUN_WITHOUT_ENCODER
            }
        }
        // sets the zero power behavior for each motor
        for (motor in motors) {
            motor.motor.zeroPowerBehavior = ZeroPowerBehavior.BRAKE
        }
        // reverses motors if necessary
        leftBack.motor.direction = mecanumDriveConstants.LEFT_BACK_DIRECTION
        leftFront.motor.direction = mecanumDriveConstants.LEFT_FRONT_DIRECTION
        rightBack.motor.direction = mecanumDriveConstants.RIGHT_BACK_DIRECTION
        rightFront.motor.direction = mecanumDriveConstants.RIGHT_FRONT_DIRECTION
    }

    /**
     * Returns a list of how far each wheel has turned in inches
     * @return how far each wheel has turned in inches
     */
    fun getWheelPositions(): List<Double> {
        val wheelPositions: MutableList<Double> = ArrayList()
        for (motor in motors) {
            wheelPositions.add(mecanumDriveConstants.encoderTicksToInches(motor.motor.currentPosition.toDouble()))
        }
        return wheelPositions
    }

    /**
     * Returns a list of how fast each wheel is turning in inches per second
     * @return how fast each wheel is turning in inches per second
     */
    fun getWheelVelocities(): List<Double> {
        val wheelVelocities: MutableList<Double> = ArrayList()
        for (motor in motors) {
            wheelVelocities.add(mecanumDriveConstants.encoderTicksToInches(motor.motor.velocity))
        }
        return wheelVelocities
    }

    /**
     * Sets power to each of the motors
     * @param frontLeft the power for the front left motor
     * @param backLeft the power for the back left motor
     * @param backRight the power for the back right motor
     * @param frontRight the power for the front right motor
     */
    private fun setMotorPowers(frontLeft: Double, backLeft: Double, backRight: Double, frontRight: Double) {
        leftFront.motor.power = frontLeft
        leftBack.motor.power = backLeft
        rightBack.motor.power = backRight
        rightFront.motor.power = frontRight
    }

    /**
     * Sets the robot's drive signal. This drive signal includes a target velocity & acceleration.
     * @param driveSignal the target velocity and acceleration of the robot
     */
    override fun setDriveSignal(driveSignal: DriveSignal) {
        val velocities = MecanumKinematics.robotToWheelVelocities(
            driveSignal.vel,
            mecanumDriveConstants.TRACK_WIDTH,
            mecanumDriveConstants.TRACK_WIDTH,
            (mecanumDriveConstants as MecanumDriveConstants).LATERAL_MULTIPLIER
        )
        val accelerations = MecanumKinematics.robotToWheelAccelerations(
            driveSignal.accel,
            mecanumDriveConstants.TRACK_WIDTH,
            mecanumDriveConstants.TRACK_WIDTH,
            mecanumDriveConstants.LATERAL_MULTIPLIER
        )
        val powers = Kinematics.calculateMotorFeedforward(
            velocities,
            accelerations,
            mecanumDriveConstants.kV,
            mecanumDriveConstants.kA,
            mecanumDriveConstants.kStatic
        )
        setMotorPowers(powers[0], powers[1], powers[2], powers[3])
    }

    /**
     * Sets the robot's drive power as a Pose2d, which has x, y, and heading components.
     * @param drivePower the target forwards/backwards, left/right, and turn speeds for the robot
     */
    override fun setDrivePower(drivePower: Pose2d) {
        val powers = MecanumKinematics.robotToWheelVelocities(
            drivePower,
            1.0,
            1.0,
            (mecanumDriveConstants as MecanumDriveConstants).LATERAL_MULTIPLIER
        )
        setMotorPowers(powers[0], powers[1], powers[2], powers[3])
    }

    /**
     * Sets the PIDF coefficients for the built-in velocity control on the motors. Adjusts the f
     * value based on the battery voltage.
     * @param coefficients the PIDF coefficients to set the motors to
     */
    fun setPIDFCoefficients(coefficients: PIDFCoefficients) {
        val compensatedCoefficients = PIDFCoefficients(
            coefficients.p, coefficients.i, coefficients.d,
            coefficients.f * 12 / batteryVoltageSensor.voltage
        )
        for (motor in motors) {
            motor.motor.setPIDFCoefficients(RunMode.RUN_USING_ENCODER, compensatedCoefficients)
        }
    }

    fun setMode(mode: RunMode) {
        for (motor in motors) {
            motor.motor.mode = mode
        }
    }
}