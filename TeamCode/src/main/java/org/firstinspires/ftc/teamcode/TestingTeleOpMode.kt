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
package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.atomicrobotics3805.cflib.Constants
import org.atomicrobotics3805.cflib.driving.drivers.MecanumDrive
import org.atomicrobotics3805.cflib.driving.localizers.TwoWheelOdometryLocalizer
import org.atomicrobotics3805.cflib.opmodes.TeleOpMode
import org.atomicrobotics3805.cflib.utilCommands.TelemetryCommand

/**
 * This class is an example of how you can create an TeleOp OpMode. Everything is handled by the
 * TeleOpMode parent class, so all you have to do is pass in the constructor parameters.
 */
@TeleOp(name = "Testing TeleOp OpMode")
class TestingTeleOpMode : TeleOpMode(
    TestingControls,
    Constants.Color.UNKNOWN,
    mainRoutine = { TelemetryCommand(100.0, "Woohoo!")},
    drive = MecanumDrive(
        TestingMecanumDriveConstants,
        TwoWheelOdometryLocalizer(TestingOdometryConstants)
    ),
    subsystems = arrayOf(TestingClaw)
)