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
package org.firstinspires.ftc.teamcode.tuning.constants

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.control.PIDCoefficients
import org.atomicrobotics3805.cflib.driving.DriverControlled
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import org.atomicrobotics3805.cflib.driving.MecanumDriveConstants
import org.atomicrobotics3805.cflib.hardware.MotorEx
import org.atomicrobotics3805.cflib.roadrunner.AxisDirection
import org.atomicrobotics3805.cflib.trajectories.toRadians

/**
 * Contains all the necessary constants for any type of drivetrain as well as the constants
 * necessary for a Mecanum drive specifically. Read the inline comments carefully to understand what
 * each constant means.
 *
 * The current values for these constants are what we used for Trio (our main competition robot)
 * during the 2021-22 Freight Frenzy season.
 */
@Config
@Suppress("ObjectPropertyName")
object TuningMecanumDriveConstants : MecanumDriveConstants {

    // These are motor constants that should be listed online for your motors.
    @JvmField
    var _TICKS_PER_REV = 537.7 // 5203 312 RPM Yellow Jacket
    @JvmField
    var _MAX_RPM = 312.0

    /*
     * Set runUsingEncoder to true to enable built-in hub velocity control using drive encoders.
     * Set this flag to false if drive encoders are not present or an alternative localization
     * method is in use (e.g., dead wheels).
     *
     * If using the built-in motor velocity PID, update motorVeloPID with the tuned coefficients
     * from DriveVelocityPIDTuner.
     */
    @JvmField
    var _MOTOR_VEL_PID = PIDFCoefficients(0.0, 0.0, 0.0, getMotorVelocityF(MAX_RPM / 60 * TICKS_PER_REV))
    @JvmField
    var _IS_RUN_USING_ENCODER = false

    /*
     * These constants are tied to your robot's hardware. You should be able to find them just by
     * looking at the robot. TRACK_WIDTH may require additional tuning, however.
     */
    @JvmField
    var _WHEEL_RADIUS = 2.0 // in
    @JvmField
    var _GEAR_RATIO = 1.0 // output (wheel) speed / input (motor) speed
    @JvmField
    var _TRACK_WIDTH = 18.0 // in, the distance between center of left and right drive wheels

    /*
     * If not using the built-in motor velocity PID, then these coefficients need to be tuned. They
     * control the robot's feedforward movement (its pre-planned motor movements, as opposed to
     * feedback, which adjusts motor movements mid-trajectory)
     */
    @JvmField
    var _kV = 1.0 / (_MAX_RPM * _GEAR_RATIO * 2 * Math.PI * _WHEEL_RADIUS / 60.0)
    @JvmField
    var _kA = 0.0
    @JvmField
    var _kStatic = 0.0

    /*
     * These values are used to generate the trajectories for you robot. To ensure proper operation,
     * the constraints should never exceed ~80% of the robot's actual capabilities. While Road
     * Runner is designed to enable faster autonomous motion, it is a good idea for testing to start
     * small and gradually increase them later after everything is working. The velocity and
     * acceleration values are required, and the jerk values are optional (setting a jerk of 0.0
     * forces acceleration-limited profiling). All distance units are inches & time in seconds. The
     * angular values are in radians.
     */
    @JvmField
    var _MAX_VEL = 30.0
    @JvmField
    var _MAX_ACCEL = 30.0
    @JvmField
    var _MAX_ANG_VEL = 30.0.toRadians
    @JvmField
    var _MAX_ANG_ACCEL = 30.0.toRadians

    /*
     * These values are used solely with Mecanum Drives to adjust the kinematics functions that
     * translate robot velocity to motor speeds. The only way to find these values is to tune and
     * adjust them until they seem about right. They should be close to 1.0.
     * The drift multipliers make the robot move forward/backward or turn left/right when strafing
     * to counteract the drift caused by an off-center center of gravity. The lateral multiplier
     * is meant to increase left/right speed.
     */
    @JvmField
    var _LATERAL_MULTIPLIER = 1.0
    @JvmField
    var _DRIFT_MULTIPLIER = 1.0
    @JvmField
    var _DRIFT_TURN_MULTIPLIER = 1.0
    @JvmField
    var _BACKWARD_DRIFT_MULTIPLIER = 1.0
    @JvmField
    var _RIGHT_DRIFT_MODIFIER = 1.0

    /*
     * These coefficients are used to adjust your location and heading when they don't match up with
     * where you should be. The only way to get these values is through tuning, but generally P=8
     * I=0 and D=0 are reasonable starting points.
     */
    @JvmField
    var _TRANSLATIONAL_PID = PIDCoefficients(8.0, 0.0, 0.0)
    @JvmField
    var _HEADING_PID = PIDCoefficients(8.0, 0.0, 0.0)

    // used during TeleOp to make precise movements
    @JvmField
    var _DRIVER_SPEEDS = listOf(1.0, 0.4)

    // the vertical  direction the hub is mounted
    @JvmField
    var _VERTICAL_AXIS = AxisDirection.POS_Z

    @JvmField
    var _POV = DriverControlled.POV.ROBOT_CENTRIC

    @JvmField
    var _LEFT_FRONT_MOTOR = MotorEx("LF", MotorEx.MotorType.GOBILDA_YELLOWJACKET, 19.2, DcMotorSimple.Direction.FORWARD)
    var _LEFT_BACK_MOTOR = MotorEx("LB", MotorEx.MotorType.GOBILDA_YELLOWJACKET, 19.2, DcMotorSimple.Direction.FORWARD)
    var _RIGHT_FRONT_MOTOR = MotorEx("RF", MotorEx.MotorType.GOBILDA_YELLOWJACKET, 19.2, DcMotorSimple.Direction.REVERSE)
    var _RIGHT_BACK_MOTOR = MotorEx("RB", MotorEx.MotorType.GOBILDA_YELLOWJACKET, 19.2, DcMotorSimple.Direction.REVERSE)

    @JvmField
    var _REVERSE_STRAFE = false
    @JvmField
    var _REVERSE_STRAIGHT = false
    @JvmField
    var _REVERSE_TURN = false


    override val TICKS_PER_REV: Double
        get() = _TICKS_PER_REV
    override val MAX_RPM: Double
        get() = _MAX_RPM
    override val MOTOR_VEL_PID: PIDFCoefficients
        get() = _MOTOR_VEL_PID
    override val IS_RUN_USING_ENCODER: Boolean
        get() = _IS_RUN_USING_ENCODER
    override val kV: Double
        get() = _kV
    override val kA: Double
        get() = _kA
    override val kStatic: Double
        get() = _kStatic
    override val WHEEL_RADIUS: Double
        get() = _WHEEL_RADIUS
    override val GEAR_RATIO: Double
        get() = _GEAR_RATIO
    override val TRACK_WIDTH: Double
        get() = _TRACK_WIDTH
    override val MAX_VEL: Double
        get() = _MAX_VEL
    override val MAX_ACCEL: Double
        get() = _MAX_ACCEL
    override val MAX_ANG_VEL: Double
        get() = _MAX_ANG_VEL
    override val MAX_ANG_ACCEL: Double
        get() = _MAX_ANG_ACCEL
    override val LATERAL_MULTIPLIER: Double
        get() = _LATERAL_MULTIPLIER
    override val BACKWARD_DRIFT_MULTIPLIER: Double
        get() = _BACKWARD_DRIFT_MULTIPLIER
    override val DRIFT_MULTIPLIER: Double
        get() = _DRIFT_MULTIPLIER
    override val DRIFT_TURN_MULTIPLIER: Double
        get() = _DRIFT_TURN_MULTIPLIER
    override val RIGHT_DRIFT_MULTIPLIER: Double
        get() = _RIGHT_DRIFT_MODIFIER
    override val LEFT_FRONT_MOTOR: MotorEx
        get() = _LEFT_FRONT_MOTOR
    override val RIGHT_FRONT_MOTOR: MotorEx
        get() = _RIGHT_FRONT_MOTOR
    override val LEFT_BACK_MOTOR: MotorEx
        get() = _LEFT_BACK_MOTOR
    override val RIGHT_BACK_MOTOR: MotorEx
        get() = _RIGHT_BACK_MOTOR
    override val POV: DriverControlled.POV
        get() = _POV
    override val REVERSE_STRAFE: Boolean
        get() = _REVERSE_STRAFE
    override val REVERSE_STRAIGHT: Boolean
        get() = _REVERSE_STRAIGHT
    override val REVERSE_TURN: Boolean
        get() = _REVERSE_TURN
    override val TRANSLATIONAL_PID: PIDCoefficients
        get() = _TRANSLATIONAL_PID
    override val HEADING_PID: PIDCoefficients
        get() = _HEADING_PID
    override val DRIVER_SPEEDS: List<Double>
        get() = _DRIVER_SPEEDS
    override val VERTICAL_AXIS: AxisDirection
        get() = _VERTICAL_AXIS
}