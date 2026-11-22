package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class HoodSubsystem {
    private Servo hood;
    private final ElapsedTime timer = new ElapsedTime();

    // Tunables
    private static final double START_POS = 0.05;   // hood position at init
    private static final double MIN_POS = 0.05;     // soft limits, set from your real hood stops
    private static final double MAX_POS = 0.7;
    private static final double SPEED = 0.8;       // position units/sec at full stick
    private static final double DEADZONE = 0.05;
    private static final double STICK_SIGN = 1;    // flip to -1 if pushing up lowers the hood
    private static final Servo.Direction DIR = Servo.Direction.FORWARD;

    private double target = START_POS;

    public void init(HardwareMap hw) {
        hood = hw.get(Servo.class, "hood");
        hood.setDirection(DIR);
        hood.setPosition(target);
        timer.reset();
    }

    public void update(Gamepad gamepad, Telemetry telemetry) {
        double dt = Math.min(timer.seconds(), 0.1);
        timer.reset();

        // Stick up = hood up (stick Y is negative when pushed up)
        double input = -gamepad.left_stick_y;
        if (Math.abs(input) < DEADZONE) input = 0;

        target += input * SPEED * dt * STICK_SIGN;
        target = Math.max(MIN_POS, Math.min(MAX_POS, target));
        hood.setPosition(target);

        telemetry.addData("Hood target", "%.3f", target);
    }

    public void stop() {
        // Servo holds its last position
    }
}