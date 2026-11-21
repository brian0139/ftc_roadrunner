package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class FlowerIntakeSubsystem {
    private CRServo flowerL, flowerR;
    private AnalogInput position;   // analog feedback wire from one of the Axons

    // ---- Tunables ----
    private static final double DEFAULT_STOP_VOLTAGE = 2.0;  // stop point at every init (0 to 3.3 V)
    private static final double VOLTAGE_STEP = 0.05;         // change per button press
    private static final boolean VOLTAGE_RISES_GOING_UP = true; // flip if voltage drops when it moves up
    private static final double MAX_POWER = 0.6;
    private static final double DEADZONE = 0.05;
    private static final double STICK_SIGN = 1;              // flip to -1 if stick up moves it down
    // The two servos face opposite ways, so one usually needs REVERSE. Flip these if they fight.
    private static final DcMotorSimple.Direction DIR_L = DcMotorSimple.Direction.FORWARD;
    private static final DcMotorSimple.Direction DIR_R = DcMotorSimple.Direction.REVERSE;

    private double stopVoltage;
    private boolean lastMinus = false, lastPlus = false;

    public void init(HardwareMap hw) {
        flowerL = hw.get(CRServo.class, "flowerL");
        flowerR = hw.get(CRServo.class, "flowerR");
        position = hw.get(AnalogInput.class, "flowerPos");
        flowerL.setDirection(DIR_L);
        flowerR.setDirection(DIR_R);

        // Resets to the default every time the OpMode is initialized (nothing is saved)
        stopVoltage = DEFAULT_STOP_VOLTAGE;
    }

    public void update(Gamepad gamepad, Telemetry telemetry) {
        // Adjust the stop point: right stick click = raise it, left stick click = lower it
        if (gamepad.right_stick_button && !lastPlus) stopVoltage += VOLTAGE_STEP;
        if (gamepad.left_stick_button && !lastMinus) stopVoltage -= VOLTAGE_STEP;
        lastPlus = gamepad.right_stick_button;
        lastMinus = gamepad.left_stick_button;
        stopVoltage = Math.max(0.1, Math.min(3.2, stopVoltage));

        double v = position.getVoltage();

        // Right stick up = move up, down = move down
        double input = -gamepad.right_stick_y;
        if (Math.abs(input) < DEADZONE) input = 0;
        double power = input * MAX_POWER * STICK_SIGN;

        // Stop moving up once the analog reading reaches the stop point
        boolean atLimit = VOLTAGE_RISES_GOING_UP ? v >= stopVoltage : v <= stopVoltage;
        if (power > 0 && atLimit) power = 0;

        flowerL.setPower(power);
        flowerR.setPower(power);

        telemetry.addData("Flower voltage", "%.3f V", v);
        telemetry.addData("Flower stop point", "%.2f V (%.0f deg)", stopVoltage, stopVoltage / 3.3 * 360);
        telemetry.addData("Flower power", "%.2f", power);
        telemetry.addData("Flower at limit", atLimit);
    }

    public void stop() {
        flowerL.setPower(0);
        flowerR.setPower(0);
    }
}