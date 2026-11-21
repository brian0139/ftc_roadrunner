package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "RobotTeleOp")
public class RobotTeleOp extends LinearOpMode {
    private final DriveSubsystem drive = new DriveSubsystem();
    private final ShooterSubsystem shooter = new ShooterSubsystem();
    private final TurretSubsystem turret = new TurretSubsystem();
    private final ShooterStopperSubsystem stopper = new ShooterStopperSubsystem();
    private final HoodSubsystem hood = new HoodSubsystem();

    @Override
    public void runOpMode() {
        drive.init(hardwareMap);
        shooter.init(hardwareMap);
        turret.init(hardwareMap);
        stopper.init(hardwareMap);
        hood.init(hardwareMap);


        // Alliance select during init (gamepad2): B = red, X = blue
        boolean blue = false;
        while (!isStarted() && !isStopRequested()) {
            if (gamepad2.b) blue = false;
            if (gamepad2.x) blue = true;
            telemetry.addData("Alliance", blue ? "BLUE" : "RED");
            telemetry.addLine("Gamepad2: B = red, X = blue");
            telemetry.update();
        }
        turret.setAlliance(blue);

        while (opModeIsActive()) {
            drive.update(gamepad1, telemetry);          // gamepad1: driving
            stopper.update(gamepad2, telemetry);        // gamepad2: stopper
            shooter.setFeeding(stopper.isOpen());
            shooter.update(gamepad1, gamepad2, telemetry); // intake: both, shooter: gamepad2
            turret.update(gamepad2, telemetry);
            hood.update(gamepad2, telemetry);  // gamepad2: turret
            telemetry.update();
        }

        drive.stop();
        shooter.stop();
        turret.stop();
    }
}