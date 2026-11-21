package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ShooterStopperSubsystem {
    private Servo stopper;
    private boolean open = false;
    private boolean lastB = false;

    // Tunables: find these by testing
    private static final double CLOSED_POS = 0.15;  // blocks balls before the flywheel
    private static final double OPEN_POS = 0.3;    // lets balls through

    public void init(HardwareMap hw) {
        stopper = hw.get(Servo.class, "stopper");
        stopper.setPosition(CLOSED_POS);
    }

    public void update(Gamepad gamepad, Telemetry telemetry) {
        // B (circle) toggles the stopper once per press
        if (gamepad.b && !lastB) open = !open;
        lastB = gamepad.b;

        stopper.setPosition(open ? OPEN_POS : CLOSED_POS);
        telemetry.addData("Stopper", open ? "OPEN" : "CLOSED");
    }

    public boolean isOpen() {
        return open;
    }

    public void stop() {
        // Servo holds its last position
    }
}