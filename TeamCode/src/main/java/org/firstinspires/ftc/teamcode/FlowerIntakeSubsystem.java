package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class FlowerIntakeSubsystem {
    private Servo flower;
    private final ElapsedTime timer = new ElapsedTime();

    // ---- Tunables ----
    private static final double BOTTOM_POS = 0.040;       // flower fully down
    private static final double DEFAULT_TOP_POS = 0.46;   // flower fully up at every init (calibrate)
    private static final double TOP_STEP = 0.01;          // change per stick-click press
    private static final double SPEED = 0.8;              // position units/sec when moving between up and down
    private static final Servo.Direction DIR = Servo.Direction.REVERSE;

    private double target = BOTTOM_POS;   // where the servo is being commanded now
    private boolean goingUp = false;
    private double topPos;
    private boolean lastY = false, lastA = false;
    private boolean lastPlus = false, lastMinus = false;

    public void init(HardwareMap hw) {
        flower = hw.get(Servo.class, "flowerL");
        flower.setDirection(DIR);

        // Resets every init: flower starts at the bottom, top limit back to the default
        topPos = DEFAULT_TOP_POS;
        target = BOTTOM_POS;
        goingUp = false;
        flower.setPosition(target);
        timer.reset();
    }

    public void update(Gamepad gamepad, Telemetry telemetry) {
        double dt = Math.min(timer.seconds(), 0.1);
        timer.reset();

        // Adjust the top limit: right stick click = raise it, left stick click = lower it
        if (gamepad.right_stick_button && !lastPlus) topPos += TOP_STEP;
        if (gamepad.left_stick_button && !lastMinus) topPos -= TOP_STEP;
        lastPlus = gamepad.right_stick_button;
        lastMinus = gamepad.left_stick_button;
        topPos = Math.max(BOTTOM_POS + 0.02, Math.min(1.0, topPos));

        // Triangle (Y) = raise to the top, X / cross (A) = lower to the bottom
        if (gamepad.y && !lastY) goingUp = true;
        if (gamepad.a && !lastA) goingUp = false;
        lastY = gamepad.y;
        lastA = gamepad.a;

        double goal = goingUp ? topPos : BOTTOM_POS;

        // Glide toward the goal at SPEED
        double step = SPEED * dt;
        if (target < goal) target = Math.min(goal, target + step);
        else if (target > goal) target = Math.max(goal, target - step);
        flower.setPosition(target);

        telemetry.addData("Flower", goingUp ? "UP" : "DOWN");
        telemetry.addData("Flower target", "%.3f", target);
        telemetry.addData("Flower top limit", "%.3f", topPos);
    }

    public void stop() {
        // Servo holds its last position
    }
}