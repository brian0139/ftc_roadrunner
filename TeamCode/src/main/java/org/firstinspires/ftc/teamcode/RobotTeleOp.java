package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "RobotTeleOp")
public class RobotTeleOp extends LinearOpMode {
    private final DriveSubsystem drive = new DriveSubsystem();
    private final ShooterSubsystem shooter = new ShooterSubsystem();
    private final TurretSubsystem turret = new TurretSubsystem();
    private final ShooterStopperSubsystem stopper = new ShooterStopperSubsystem();



    @Override
    public void runOpMode() {
        drive.init(hardwareMap);
        shooter.init(hardwareMap);
        turret.init(hardwareMap);
        stopper.init(hardwareMap);

        telemetry.addLine("Robot Ready.");
        telemetry.update();
        boolean blue = false;
        while (!isStarted() && !isStopRequested()) {
            if (gamepad1.b) blue = false;   // red
            if (gamepad1.x) blue = true;    // blue
            telemetry.addData("Alliance", blue ? "BLUE" : "RED");
            telemetry.addLine("B = red, X = blue");
            telemetry.update();
        }
        turret.setAlliance(blue);
        while (opModeIsActive()) {
            drive.update(gamepad1, telemetry);
            shooter.setFeeding(stopper.isOpen());
            shooter.update(gamepad1, telemetry);
            turret.update(gamepad1, telemetry);
            stopper.update(gamepad1, telemetry);
            telemetry.update();
        }

        drive.stop();
        shooter.stop();
    }
}