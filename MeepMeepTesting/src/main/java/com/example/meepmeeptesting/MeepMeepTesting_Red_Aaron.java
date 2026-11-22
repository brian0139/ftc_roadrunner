package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;

import org.rowlandhall.meepmeep.MeepMeep;
import org.rowlandhall.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark;
import org.rowlandhall.meepmeep.roadrunner.DefaultBotBuilder;
import org.rowlandhall.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MeepMeepTesting_Red_Aaron {
    public static void main(String[] args) throws IOException {
        // Declare a MeepMeep instance
        // With a field size of 800 pixels
        MeepMeep meepMeep = new MeepMeep(800);

        // Load the custom BioBuzz field image from src/main/resources
        Image field = ImageIO.read(MeepMeepTesting_Red_Aaron.class.getResourceAsStream("/biobuzz_field.png"));

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                .setDimensions(13.667,15.5)
                // Required: Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                // Option: Set theme. Default = ColorSchemeRedDark()
                .setColorScheme(new ColorSchemeRedDark())
                .followTrajectorySequence(drive ->
                                drive.trajectorySequenceBuilder(new Pose2d(70-(15.5/2), -23, Math.toRadians(180)))
                                        .waitSeconds(2)
                                        //drive out and align back
                                        .splineToLinearHeading(new Pose2d(70-15.5,-23,Math.toRadians(270)),Math.toRadians(270))
                                        .lineToLinearHeading(new Pose2d((70-13.667/2), -23, Math.toRadians(270)))

                                        //intake ball
                                        .lineToLinearHeading(new Pose2d((70-13.667/2),(-70+15.5/2),Math.toRadians(270)))
                                        .waitSeconds(1)

                                        //shoot
                                        .lineToLinearHeading(new Pose2d(47,-12,Math.toRadians(180))) //shoot
//                                .splineToLinearHeading(new Pose2d(-38,-22,Math.toRadians(0)),Math.toRadians(0))
                                        .waitSeconds(2)

                                        //go back to flower position
                                        .lineToLinearHeading(new Pose2d(24,(-70+15.5/2+8),Math.toRadians(270))) //flower position
                                        .waitSeconds(2)

                                        //shoot
                                        .lineToLinearHeading(new Pose2d(47,-12,Math.toRadians(180))) //shoot
                                        .waitSeconds(2)

                                        //drive to other side flower
                                        .splineToLinearHeading(new Pose2d(37,-47,Math.toRadians(180)),Math.toRadians(180))
                                        .lineToLinearHeading(new Pose2d(-34,-47,Math.toRadians(180)))
                                        .splineToLinearHeading(new Pose2d(-70+15.5/2+8,-24,Math.toRadians(180)),Math.toRadians(180))
                                        .waitSeconds(2)

                                        .lineToLinearHeading(new Pose2d(-42,-24,Math.toRadians(180)))

                                        //shoot
                                        .splineToLinearHeading(new Pose2d(-48,-12,Math.toRadians(0)),Math.toRadians(0))
                                        .waitSeconds(2)

                                        .build()
                );

        // Set field image
        meepMeep.setBackground(field)
                .setDarkMode(true)
                // Background opacity from 0-1
                .setBackgroundAlpha(1f)
                .addEntity(myBot)
                .start();
    }
}