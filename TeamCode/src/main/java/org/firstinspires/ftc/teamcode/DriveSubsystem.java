package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class DriveSubsystem {
    private DcMotor leftFront, leftBack, rightFront, rightBack;

    public void init(HardwareMap hw) {
        leftFront = hw.get(DcMotor.class, "leftFront");
        leftBack = hw.get(DcMotor.class, "leftBack");
        rightFront = hw.get(DcMotor.class, "rightFront");
        rightBack = hw.get(DcMotor.class, "rightBack");

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void update(Gamepad gamepad, Telemetry telemetry) {
        final double strafeSpeed = 0.5;

        double drive = -gamepad.left_stick_y * 0.7;
        double strafe = -gamepad.left_stick_x * 0.5;
//        if (gamepad.dpad_left) {
//            strafe = strafeSpeed;
//        } else if (gamepad.dpad_right) {
//            strafe = -strafeSpeed;
//        }
        double twist = gamepad.right_stick_x * 0.5;

        double[] speeds = {
                (drive - strafe + twist), // front-left
                (drive + strafe - twist), // front-right
                (drive + strafe + twist), // back-left
                (drive - strafe - twist)  // back-right
        };

        // Normalize if any value is outside -1..1
        double max = 0;
        for (double s : speeds) max = Math.max(max, Math.abs(s));
        if (max > 1) {
            for (int i = 0; i < speeds.length; i++) speeds[i] /= max;
        }

        leftFront.setPower(speeds[0]);
        rightFront.setPower(speeds[1]);
        leftBack.setPower(speeds[2]);
        rightBack.setPower(speeds[3]);

        telemetry.addData("drive", "%.2f", drive);
        telemetry.addData("strafe", "%.2f", strafe);
        telemetry.addData("twist", "%.2f", twist);
    }

    public void stop() {
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftBack.setPower(0);
        rightBack.setPower(0);
    }
}