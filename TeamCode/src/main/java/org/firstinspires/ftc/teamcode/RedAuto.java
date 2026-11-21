package org.firstinspires.ftc.teamcode;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.InstantAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.SleepAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@Autonomous(name = "Red Auto", group = "Auto")
public class RedAuto extends LinearOpMode {
    private DcMotor intake, shooter;
    private Servo stopper;

    // Tunables (match what you use in teleop)
    private static final double SHOOTER_POWER = 0.8;
    private static final double INTAKE_POWER = 0.75;
    private static final double CLOSED_POS = 0.2;   // stopper blocks balls
    private static final double OPEN_POS = 0.6;     // stopper lets balls through

    @Override
    public void runOpMode() {
        intake = hardwareMap.get(DcMotor.class, "intake");
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        stopper = hardwareMap.get(Servo.class, "stopper");
        shooter.setDirection(DcMotorSimple.Direction.REVERSE);
        stopper.setPosition(CLOSED_POS);

        Pose2d start = new Pose2d(70 - (15.5 / 2), -23, Math.toRadians(180));
        MecanumDrive drive = new MecanumDrive(hardwareMap, start);

        TrajectoryActionBuilder path = drive.actionBuilder(start)
                .stopAndAdd(shoot(0.75, 1.25))   // was waitSeconds(2): spin up, then feed
                .splineToLinearHeading(new Pose2d(54.6, -70 + (15.5 / 2), Math.toRadians(0)), Math.toRadians(0))
                .strafeToLinearHeading(new Vector2d(70 - 13.667 / 2, -70 + 15.5 / 2), Math.toRadians(0))
                .stopAndAdd(intakeBalls(1))       // was waitSeconds(1)
                .strafeToLinearHeading(new Vector2d(47, -46), Math.toRadians(180))
                .splineToLinearHeading(new Pose2d(-38, -22, Math.toRadians(0)), Math.toRadians(0))
                .stopAndAdd(shoot(0, 2))          // was waitSeconds(2): shooter already spinning
                .strafeToLinearHeading(new Vector2d(-49, -70 + 15.5 / 2), Math.toRadians(90));

        waitForStart();
        if (isStopRequested()) return;

        Actions.runBlocking(new SequentialAction(
                path.build(),
                new InstantAction(() -> {
                    shooter.setPower(0);
                    intake.setPower(0);
                    stopper.setPosition(CLOSED_POS);
                })
        ));
    }

    // Spin the shooter up, open the stopper, run the intake to feed, then close it again
    private Action shoot(double spinUpSeconds, double feedSeconds) {
        return new SequentialAction(
                new InstantAction(() -> shooter.setPower(SHOOTER_POWER)),
                new SleepAction(spinUpSeconds),
                new InstantAction(() -> {
                    stopper.setPosition(OPEN_POS);
                    intake.setPower(INTAKE_POWER);
                }),
                new SleepAction(feedSeconds),
                new InstantAction(() -> {
                    intake.setPower(0);
                    stopper.setPosition(CLOSED_POS);
                })
        );
    }

    // Run the intake with the stopper closed
    private Action intakeBalls(double seconds) {
        return new SequentialAction(
                new InstantAction(() -> intake.setPower(INTAKE_POWER)),
                new SleepAction(seconds),
                new InstantAction(() -> intake.setPower(0))
        );
    }
}