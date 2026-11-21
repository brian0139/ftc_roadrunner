package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ShooterSubsystem {
    private DcMotor intake, shooter;
    private VoltageSensor battery;

    private boolean shooterOn = false;
    private boolean intakeOn = false;
    private boolean feeding = false;
    private boolean lastLB = false, lastRB = false;
    private boolean lastUp = false, lastDown = false;
    private boolean lastRight = false, lastLeft = false;

    private double shooterPower = 0.8;
    private double intakePower = 0.75;
    private static final double STEP = 0.05;
    private static final double REF_VOLTAGE = 12.5;  // battery reading when full
    private static final double IDLE_SCALE = 0.9;    // flywheel runs at 90% until a ball is feeding

    public void init(HardwareMap hw) {
        intake = hw.get(DcMotor.class, "intake");
        shooter = hw.get(DcMotor.class, "shooter");
        shooter.setDirection(DcMotorSimple.Direction.REVERSE);
        battery = hw.voltageSensor.iterator().next();
    }

    public void setFeeding(boolean feeding) {
        this.feeding = feeding;
    }

    public void update(Gamepad gamepad, Telemetry telemetry) {
        // Left bumper toggles the shooter
        if (gamepad.left_bumper && !lastLB) shooterOn = !shooterOn;
        lastLB = gamepad.left_bumper;

        // Right bumper toggles the intake
        if (gamepad.right_bumper && !lastRB) intakeOn = !intakeOn;
        lastRB = gamepad.right_bumper;

        // X (square) held = outtake (intake in reverse)
        boolean outtake = gamepad.x;

        // Shooter speed: dpad up/down
        if (gamepad.dpad_up && !lastUp) shooterPower += STEP;
        if (gamepad.dpad_down && !lastDown) shooterPower -= STEP;
        lastUp = gamepad.dpad_up;
        lastDown = gamepad.dpad_down;

        // Intake speed: dpad right/left
        if (gamepad.dpad_right && !lastRight) intakePower += STEP;
        if (gamepad.dpad_left && !lastLeft) intakePower -= STEP;
        lastRight = gamepad.dpad_right;
        lastLeft = gamepad.dpad_left;

        shooterPower = Math.max(0, Math.min(1, shooterPower));
        intakePower = Math.max(0, Math.min(1, intakePower));

        // Outtake overrides the intake while held, then the intake resumes if it was on
        double intakeOut = outtake ? -intakePower : (intakeOn ? intakePower : 0.0);
        intake.setPower(intakeOut);

        double scale = feeding ? 1.0 : IDLE_SCALE;
        double compensated = shooterPower * scale * REF_VOLTAGE / battery.getVoltage();
        shooter.setPower(shooterOn ? Math.min(1.0, compensated) : 0.0);

        telemetry.addData("Shooter on", shooterOn);
        telemetry.addData("Shooter power", "%.2f", shooterPower);
        telemetry.addData("Intake", outtake ? "OUTTAKE" : (intakeOn ? "ON" : "off"));
        telemetry.addData("Intake power", "%.2f", intakePower);
    }

    public void stop() {
        intake.setPower(0);
        shooter.setPower(0);
    }
}