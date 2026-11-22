package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TurretSubsystem {
    private Servo turretL, turretR;
    private Limelight3A limelight;
    private final ElapsedTime timer = new ElapsedTime();

    // ---- Servo / manual tunables ----
    private static final double START_POS = 0.5;   // turret forward
    private static final double MIN_POS = 0.1;
    private static final double MAX_POS = 0.9;
    private static final double SPEED = 0.5;       // manual: position units/sec at full trigger
    private static final double DEADZONE = 0.05;
    private static final double TURN_SIGN = 1;     // flip to -1 if right trigger turns it left
    private static final Servo.Direction DIR_L = Servo.Direction.FORWARD;
    private static final Servo.Direction DIR_R = Servo.Direction.FORWARD;

    // ---- Auto-aim tunables ----
    // Index 0 = audience side, index 1 = side opposite the audience
    private static final int[][] RED_CLUSTERS = {{34, 37}, {30, 33}};
    private static final int[][] BLUE_CLUSTERS = {{38, 41}, {42, 45}};
    private boolean blueAlliance = false;
    private int basket = 0;       // 0 = audience side, 1 = opposite side
    private boolean lastY = false;
    private static final int PIPELINE = 0;
    private static final double DEG_PER_POS = 262.5;   // turret degrees per 1.0 of servo travel (350 x 30/40)
    private static final double TRACK_SIGN = 1;        // flip to -1 if it turns away from the tag
    private static final double TARGET_OFFSET_DEG = 0; // aim offset if the camera isn't lined up with the shooter
    private static final double DEADBAND_DEG = 0.5;
    private static final double MAX_RATE_DPS = 90;     // cap on tracking speed (turret deg/sec)

    private double kP = 6.0;    // turret deg/sec per degree of error
    private double kI = 0.0;
    private double kD = 0.1;

    // ---- State ----
    private double target = START_POS;
    private double integral = 0, lastError = 0;
    private boolean hasLast = false;
    private boolean autoAim = false;
    private boolean lastA = false;

    public void init(HardwareMap hw) {
        turretL = hw.get(Servo.class, "turretL");
        turretR = hw.get(Servo.class, "turretR");
        turretL.setDirection(DIR_L);
        turretR.setDirection(DIR_R);
        turretL.setPosition(target);
        turretR.setPosition(target);

        limelight = hw.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(PIPELINE);
        limelight.setPollRateHz(100);
        limelight.start();

        timer.reset();
    }

    public void setAlliance(boolean blue) {
        blueAlliance = blue;
        basket = 0;
    }

    public void update(Gamepad gamepad, Telemetry telemetry) {
        double dt = Math.min(timer.seconds(), 0.1);
        timer.reset();
        // Y swaps between the two baskets on your alliance
        if (gamepad.y && !lastY) basket = 1 - basket;
        lastY = gamepad.y;

        int[] range = (blueAlliance ? BLUE_CLUSTERS : RED_CLUSTERS)[basket];
        int minId = range[0], maxId = range[1];

        // Average tx over the visible tags in the cluster
        Double tx = null;
        int tagCount = 0;
        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid()) {
            double sum = 0;
            for (LLResultTypes.FiducialResult f : result.getFiducialResults()) {
                int id = f.getFiducialId();
                if (id >= minId && id <= maxId) {
                    sum += f.getTargetXDegrees();
                    tagCount++;
                }
            }
            if (tagCount > 0) tx = sum / tagCount;
        }

        // Triggers: manual turret control (right = right, left = left)
        double manual = gamepad.right_trigger - gamepad.left_trigger;
        boolean manualActive = Math.abs(manual) >= DEADZONE;

        // A toggles auto-aim on/off
        if (gamepad.a && !lastA) autoAim = !autoAim;
        lastA = gamepad.a;

        boolean tracking = autoAim && tx != null && !manualActive;

        if (manualActive) {
            target += manual * SPEED * dt * TURN_SIGN;
        } else if (tracking) {
            double error = tx - TARGET_OFFSET_DEG;
            double rate = 0;  // commanded turret rate, deg/sec
            if (Math.abs(error) > DEADBAND_DEG) {
                integral += error * dt;
                double derivative = hasLast && dt > 0 ? (error - lastError) / dt : 0;
                rate = kP * error + kI * integral + kD * derivative;
                rate = Math.max(-MAX_RATE_DPS, Math.min(MAX_RATE_DPS, rate));
            } else {
                integral = 0;
            }
            target += (rate / DEG_PER_POS) * dt * TRACK_SIGN;
            lastError = error;
            hasLast = true;
        }

        if (!tracking) {
            integral = 0;
            hasLast = false;
        }

        target = Math.max(MIN_POS, Math.min(MAX_POS, target));
        turretL.setPosition(target);
        turretR.setPosition(target);

        telemetry.addData("Turret target", "%.3f", target);
        telemetry.addData("Tags seen", tagCount);
        telemetry.addData("tx (deg)", tx == null ? "none" : String.format("%.2f", tx));
        telemetry.addData("Auto-aim", autoAim ? (tracking ? "TRACKING" : "ON (no tag)") : "off");
        telemetry.addData("Target", (blueAlliance ? "BLUE " : "RED ") + minId + "-" + maxId);
    }

    public void stop() {
        limelight.stop();
    }
}