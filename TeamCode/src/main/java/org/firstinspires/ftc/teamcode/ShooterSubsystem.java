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

    // driver = gamepad1 (intake only), operator = gamepad2 (shooter + intake)
    public void update(Gamepad driver, Gamepad operator, Telemetry telemetry) {
        // ---- Shooter: operator only ----
        if (operator.left_bumper && !lastLB) shooterOn = !shooterOn;
        lastLB = operator.left_bumper;

        if (operator.dpad_up && !lastUp) shooterPower += STEP;
        if (operator.dpad_down && !lastDown) shooterPower -= STEP;
        lastUp = operator.dpad_up;
        lastDown = operator.dpad_down;

        // ---- Intake: either controller ----
        boolean rb = driver.right_bumper || operator.right_bumper;
        if (rb && !lastRB) intakeOn = !intakeOn;
        lastRB = rb;

        boolean outtake = driver.x || operator.x;   // held = reverse

        boolean right = driver.dpad_right || operator.dpad_right;
        boolean left = driver.dpad_left || operator.dpad_left;
        if (right && !lastRight) intakePower += STEP;
        if (left && !lastLeft) intakePower -= STEP;
        lastRight = right;
        lastLeft = left;

        shooterPower = Math.max(0, Math.min(1, shooterPower));
        intakePower = Math.max(0, Math.min(1, intakePower));

        intake.setPower(outtake ? -intakePower : (intakeOn ? intakePower : 0.0));

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