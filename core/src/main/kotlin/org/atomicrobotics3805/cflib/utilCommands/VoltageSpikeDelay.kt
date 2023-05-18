package org.atomicrobotics3805.cflib.utilCommands

import com.qualcomm.robotcore.util.ElapsedTime
import org.atomicrobotics3805.cflib.Command
import org.atomicrobotics3805.cflib.Constants
import kotlin.math.abs

class VoltageSpikeDelay(
    // The amount of time ago that it checks the voltage of
    val jamTime: Double = 0.1,
    // The voltage must drop by at least this amount in jamTime seconds
    val jamRate: Double = 2.0 * jamTime,
    // The voltage must be below this amount
    val jamMaxVoltage: Double = 11.0
) : Command() {

    override val _isDone
        get() = jammed

    // first is voltage, second is timestamp
    val voltages = ArrayList<Pair<Double, Double>>()
    val timer = ElapsedTime()
    var jammed = false

    override fun start() {
        timer.reset()
    }

    override fun execute() {
        val voltage = Constants.drive.batteryVoltageSensor.voltage
        val currentTime = timer.seconds()
        var closestTime = Double.MAX_VALUE
        var closestVoltage = 0.0
        for (pair in voltages.reversed()) {
            if (abs(currentTime - jamTime - pair.second) < closestTime) {
                closestTime = abs(pair.second - currentTime)
                closestVoltage = pair.first
            }
            else {
                break
            }
        }
        if (closestVoltage < jamMaxVoltage && closestVoltage - voltage > jamRate) {
            jammed = true
        }
        voltages.add(Pair(voltage, currentTime))
    }
}