/*
 * Copyright (c) 2015 LASA Robotics and Contributors
 * MIT licensed
 */

package com.lasarobotics.tests.camera;

import android.util.Log;

import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.detection.ColorBlobDetector;
import org.lasarobotics.vision.detection.objects.Contour;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.image.Drawing;
import org.lasarobotics.vision.opmode.TestableVisionOpMode;
import org.lasarobotics.vision.opmode.extensions.CameraControlExtension;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.lasarobotics.vision.util.color.Color;
import org.lasarobotics.vision.util.color.ColorGRAY;
import org.lasarobotics.vision.util.color.ColorHSV;
import org.lasarobotics.vision.util.color.ColorRGBA;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.List;

/**
 * Vision OpMode run by the Camera Test Activity
 * Use TestableVisionOpModes in testing apps ONLY (but you can easily convert between opmodes just by changingt t
 */
public class RedDetector extends TestableVisionOpMode {
    //--------------------------------------------------------------------------
    // Vision Global Variables
    //--------------------------------------------------------------------------
    private static final ColorHSV   lowerBoundRed = new ColorHSV((int) (305 / 360.0 * 255.0), (int) (0.200 * 255.0), (int) (0.300 * 255.0));
    private static final ColorHSV   upperBoundRed = new ColorHSV((int) ((360.0 + 5.0) / 360.0 * 255.0), 255, 255);
    private ColorBlobDetector       detectorRed;
    private ColorBlobDetector       detectorBlue;

    @Override
    public void init() {
        super.init();
        detectorRed = new ColorBlobDetector(lowerBoundRed,upperBoundRed);
        Log.d("ColorContour", "Init");
        /**
         * Set the camera used for detection
         * PRIMARY = Front-facing, larger camera
         * SECONDARY = Screen-facing, "selfie" camera :D
         **/
        this.setCamera(Cameras.PRIMARY);

        /**
         * Set the maximum frame size
         * Larger = sometimes more accurate, but also much slower
         * After this method runs, it will set the "width" and "height" of the frame
         **/
        this.setFrameSize(new Size(900, 900));

        /**
         * Enable extensions. Use what you need.
         * If you turn on the BEACON extension, it's best to turn on ROTATION too.
         */
        enableExtension(Extensions.BEACON);         //Beacon detection
        enableExtension(Extensions.ROTATION);       //Automatic screen rotation correction
        enableExtension(Extensions.CAMERA_CONTROL); //Manual camera control

        /**
         * Set the beacon analysis method
         * Try them all and see what works!
         */
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);

        /**
         * Set color tolerances
         * 0 is default, -1 is minimum and 1 is maximum tolerance
         */
        beacon.setColorToleranceRed(0);
        beacon.setColorToleranceBlue(0);

        /**
         * Debug drawing
         * Enable this only if you're running test app - otherwise, you should turn it off
         * (Although it doesn't harm anything if you leave it on, only slows down image processing)
         */
        beacon.enableDebug();

        /**
         * Set the rotation parameters of the screen
         *
         * First, tell the extension whether you are using a secondary camera
         * (or in some devices, a front-facing camera that reverses some colors).
         *
         * If you have a weird phone, you can set the "zero" orientation here as well.
         *
         * For TestableVisionOpModes, changing other settings may break the app. See other examples
         * for normal OpModes.
         */
        rotation.setIsUsingSecondaryCamera(false);
        rotation.disableAutoRotate();
        rotation.setActivityOrientationFixed(ScreenOrientation.LANDSCAPE);
        //rotation.setZeroOrientation(ScreenOrientation.LANDSCAPE_REVERSE);

        /**
         * Set camera control extension preferences
         *
         * Enabling manual settings will improve analysis rate and may lead to better results under
         * tested conditions. If the environment changes, expect to change these values.
         */
        cameraControl.setColorTemperature(CameraControlExtension.ColorTemperature.AUTO);
        cameraControl.setAutoExposureCompensation();
    }

    @Override
    public void loop() {
        super.loop();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public Mat frame(Mat rgba, Mat gray) {

        Log.d("ColorContour", "Entering Try");
        try
        {
            //Process the frame for the color blobs
            detectorRed.process(rgba);
            //Get the list of contours
            List<Contour> contoursRed = detectorRed.getContours();
            Log.d("ColorContour", String.valueOf(contoursRed.size()));
            if (contoursRed.size() > 0)
            {
                Contour bigRed = contoursRed.get(0);
                for (int i = 1; i < contoursRed.size(); i++) {
                    if (bigRed.area() < contoursRed.get(i).area()) {
                        bigRed = contoursRed.get(i);
                    }
                }
                //Display analysis method
//                Drawing.drawText(rgba, "BigRed Center: " + String.valueOf(bigRed.center().x),
//                        Drawing.Anchor.BOTTOMLEFT, 1.0f, new ColorRGBA("#FFC107"));
                Log.d("ColorContour", "Red " + String.valueOf(bigRed.center().x));
                Drawing.drawText(rgba, "Red " + String.valueOf(bigRed.center().x),
                        new Point(100, 100), 1.0f, new ColorGRAY(255));
                Drawing.drawContour(rgba,bigRed, new ColorRGBA(255,0,0));
            }
            else
            {
                //Display analysis method
                Drawing.drawText(rgba, "BigRed Center: None",
                        new Point(width - 300, 40), 1.0f, new ColorRGBA("#FFC107"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return rgba;
    }
}