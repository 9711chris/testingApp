package com.example.christantia.testingapp;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC4;

public class MainActivity extends AppCompatActivity {
    public static final int MAX_WIDTH = 210;
    public static final int MAX_HEIGHT = 110;
    public static final int RESIZE_VALUE = 5;
    private static final String TAG = "Debug";
    File root = android.os.Environment.getExternalStorageDirectory();
    File file;
    File directory;
    File dir;
    File dir2;
    private String user = "";
    private String trial = "";
    private Paint mFaceLandmardkPaint2;
    private FaceDet faceDet = new FaceDet(Constants.getFaceShapeModelPath());
    private String[][] fullRecords;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mLoaderCallback))
        {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        setContentView(R.layout.activity_main);

        init();

        Log.d(TAG,"External file system root: " + root);

        directory = new File (root.getAbsolutePath() + "/Experiment");

        //take in user input
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user = input.getText().toString();
                dir = new File (directory + "/" + user);
                Log.d(TAG, "Dir: " + dir.toString());
            }
        });

        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("Trial Number");

        // Set up the input
        final EditText input2 = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input2.setInputType(InputType.TYPE_CLASS_TEXT);
        builder2.setView(input2);

        // Set up the buttons
        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                trial = input2.getText().toString();
                dir2 = new File (dir + "/" + trial);
                Log.d(TAG, "Dir2: " + dir2.toString());
                analyzeFiles();
            }
        });
        builder2.show();
        builder.show();

    }

    private void analyzeFiles(){
        List<String> records = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir2 + "/myData.txt"));
            String line;
            while ((line = reader.readLine()) != null)
            {
                records.add(line);
                Log.d(TAG, "Add to records: " + line);
            }
            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", dir2 + "/myData.txt");
            e.printStackTrace();
        }

        fullRecords = new String[records.size()][3];

        for (int i = 1; i < records.size(); i++){
            String[] tmp = lineSplit(records, i);
            for (int j= 0; j < 3; j++) {
                fullRecords[i][j] = tmp[j];
                Log.d(TAG, "fullRecords[" + i + "] :" + fullRecords[i][j]);
            }
        }


        File imgFile = new  File(dir2 + "/" + fullRecords[1][2] + ".png");
        Log.d(TAG, "imgFile : " + imgFile.toString());

        Bitmap bmp = null;
        Bitmap tmprightEye = null;
        Bitmap tmpleftEye = null;
        Bitmap rightEye = null;
        Bitmap leftEye = null;


        if(imgFile.exists()){
            Log.d(TAG, "imgFile exist");

            bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            //ImageView myImage = (ImageView) findViewById(R.id.imageView);
            ImageView myImage2 = (ImageView) findViewById(R.id.imageView2);
            ImageView myImage3 = (ImageView) findViewById(R.id.imageView3);

            //Bitmap bmpRet = setLandmarks(bmp);

            //myImage.setImageBitmap(bmpRet);
            tmprightEye = setRightEyeLandmarks(bmp);
            tmpleftEye = setLeftEyeLandmarks(bmp);
            rightEye = Bitmap.createScaledBitmap(tmprightEye, (int) tmprightEye.getWidth() / RESIZE_VALUE, (int) tmpleftEye.getHeight() / RESIZE_VALUE, false);
            leftEye = Bitmap.createScaledBitmap(tmprightEye, (int) tmprightEye.getWidth() / RESIZE_VALUE, (int) tmpleftEye.getHeight() / RESIZE_VALUE, false);
            myImage2.setImageBitmap(tmprightEye);
            myImage3.setImageBitmap(tmpleftEye);
            //Log.d(TAG, "COOREx: " + fullRecords[1][0] + " " + fullRecords[1][1]);
        }

        Mat mat = new Mat(rightEye.getWidth(), rightEye.getHeight(), CV_8UC4);
        Utils.bitmapToMat(rightEye, mat);
        Mat matRet = new Mat();
        Imgproc.cvtColor(mat, matRet, Imgproc.COLOR_BGRA2GRAY);
        Matrix examEye = new Matrix(matRet.width()*matRet.height(), 1);
        Matrix examCoor = new Matrix(2, 1);
        //Log.d(TAG, "EXAMEye matret height: " + matRet.height());
        for (int i = 0; i < matRet.height(); i++) {
            String line = "";
            for (int j = 0; j < matRet.width(); j++) {
                line += matRet.get(i,j)[0] + " ";
                examEye.set(i, 0, matRet.get(i, j)[0]);
            }
            //Log.d(TAG, "EXAMEye Points: Line " + i + " : " + line);
        }
        examCoor.set(0, 0, Integer.parseInt(fullRecords[1][0]));
        examCoor.set(1, 0, Integer.parseInt(fullRecords[1][1]));
        for (int i = 0; i < 2; i++) {
            String line = "";
            for (int j = 0; j < examCoor.getColumnDimension(); j++) {
                line += examCoor.get(i,j) + " ";
            }
            //Log.d(TAG, "EXAMCoor coordinate: Line " + i + " : " + line);
        }

        //Mat coor = new Mat(2, records.size() - 2, CV_8UC1);
        Matrix coor = new Matrix(2, records.size() - 2);
        //Mat eyes = new Mat();
        Matrix eyes = new Matrix(0, 0);
        int size = 0; int sizeKnown = 0;
        for (int i = 2; i < records.size(); i++){
            for (int j = 0; j < 3; j++){
                switch (j){
                    case 0:case 1:
                        //Log.d(TAG, "FullRecords at coor :" + fullRecords[i][j]);
                        coor.set(j, i - 2, Integer.parseInt(fullRecords[i][j]));
                        break;
                    case 2:
                        File file = new  File(dir2 + "/" + fullRecords[i][j] + ".png");
                        Log.d(TAG, "imgFile : " + file.toString());

                        Bitmap bmpEye = null;
                        Bitmap tmpRight = null;
                        Bitmap tmpLeft = null;
                        Bitmap right = null;
                        Bitmap left = null;

                        if(file.exists()){
                            Log.d(TAG, "file exists");
                            bmpEye = BitmapFactory.decodeFile(file.getAbsolutePath());

                            tmpRight = setRightEyeLandmarks(bmpEye);
                            tmpLeft = setLeftEyeLandmarks(bmpEye);
                            right = Bitmap.createScaledBitmap(tmpRight, (int) tmpRight.getWidth() / RESIZE_VALUE, (int) tmpRight.getHeight() / RESIZE_VALUE, false);
                            left = Bitmap.createScaledBitmap(tmpLeft, (int) tmpLeft.getWidth() / RESIZE_VALUE, (int) tmpLeft.getHeight() / RESIZE_VALUE, false);

                        }

                        Mat mateye = new Mat(right.getWidth(), right.getHeight(), CV_8UC4);
                        Utils.bitmapToMat(right, mateye);
                        Mat mateyeRet = new Mat();
                        Imgproc.cvtColor(mateye, mateyeRet, Imgproc.COLOR_BGRA2GRAY);
                        if (sizeKnown == 0) {
                            size = mateyeRet.width() * mateyeRet.height();
                            //eyes.create(size, records.size() - 2, CV_8UC1);
                            eyes = new Matrix(size, records.size() - 2);
                            sizeKnown = 1;
                        }
                        Log.d(TAG, "matEye mat type: " + mateye.type());
                        Log.d(TAG, "Eyes mat type: ");
                        //mateyeRet.reshape(1, 1);
                        //Log.d(TAG, "mateyeRet height: " + mateyeRet.height());
                        int sizeCounter = 0;
                        for (int k = 0; k < mateyeRet.height(); k++) {
                            String line = "";
                            for (int l = 0; l < mateyeRet.width(); l++) {
                                line += mateyeRet.get(k,l)[0] + " ";
                                eyes.set(sizeCounter, i - 2, mateyeRet.get(k, l)[0]);
                                sizeCounter++;
                            }
                            //Log.d(TAG, "Mat Points: Line " + k + " : " + line);
                        }
                        break;
                    default:
                        Log.d(TAG, "Error in analyzing files");
                }

            }
        }

        Log.d(TAG, "XMat Eyes: ");
        eyes.print(eyes.getColumnDimension(), 1);

        Log.d(TAG, "Pcoordinate: ");
        //coor.print(coor.getColumnDimension(), 1);

        Matrix transposed = eyes.transpose();
        Log.d(TAG, "XMat Transposed: ");
        transposed.print(transposed.getColumnDimension(), 1);

        Matrix inversable = transposed.times(eyes);
        Log.d(TAG, "XMat Inversable: ");
        inversable.print(inversable.getColumnDimension(), 1);

        Matrix inversed = inversable.inverse();
        Log.d(TAG, "inversed: ");
        inversed.print(inversed.getColumnDimension(), 10);
        Log.d(TAG, "MATanalyze: width and height: " + inversable.getRowDimension() + "  " + inversable.getColumnDimension());
        Matrix coResult = (inversed.times(transposed)).times(examEye);
        Log.d(TAG, "coResult: " );
        coResult.print(coResult.getColumnDimension(), 1);
        Matrix pointResult = coor.times(coResult);
        Log.d(TAG, "pointResult: " );
        pointResult.print(pointResult.getColumnDimension(), 1);
        Matrix error = examCoor.minus(pointResult);
        Log.d(TAG, "errorMatrix: " );
        error.print(error.getColumnDimension(), 1);

        Log.d(TAG, "ERROR: Line " + Math.sqrt(Math.pow(error.get(0, 0),2) + Math.pow(error.get(0, 1),2)));
    }

    private Bitmap setLandmarks(Bitmap bmp){
        Bitmap mutableBitmap = Bitmap.createScaledBitmap(bmp, (int) bmp.getWidth() / 2, (int) bmp.getHeight() / 2, false);
        Canvas canvas = new Canvas(mutableBitmap);
        List<VisionDetRet> results = faceDet.detect(mutableBitmap);
        Log.d(TAG, "faces " + results.size());
        for (final VisionDetRet ret : results) {
            Log.d(TAG, "in VisionDet");
            float resizeRatio = 1.0f;
            String label = ret.getLabel(); // If doing face detection, it will be 'Face'
            Log.d(TAG, "label" + label);
            ArrayList<Point> landmarks = ret.getFaceLandmarks();
            Log.d(TAG, "list length" + landmarks.size());
            for (Point point : landmarks) {
                int pointX = (int) (point.x * resizeRatio);
                int pointY = (int) (point.y * resizeRatio);
                //Log.d(TAG, "x, y " + pointX + " " + pointY);
                // Get the point of the face landmarks
                canvas.drawCircle(pointX, pointY, 2, mFaceLandmardkPaint2);
            }
        }
        return mutableBitmap;
    }

    private Bitmap setRightEyeLandmarks(Bitmap bmp){
        Log.d(TAG, "in setEyeLandmark");
        Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        List<VisionDetRet> results = faceDet.detect(mutableBitmap);
        Log.d(TAG, "faces " + results.size());
        int x = 0; int y = 0; int width = 0; int height = 0;
        for (final VisionDetRet ret : results) {
            Log.d(TAG, "in VisionDet");
            float resizeRatio = 1.0f;
            String label = ret.getLabel(); // If doing face detection, it will be 'Face'
            Log.d(TAG, "label" + label);
            ArrayList<Point> landmarks = ret.getFaceLandmarks();
            Log.d(TAG, "list length" + landmarks.size());
            /*for (Point point : landmarks) {
                int pointX = (int) (point.x * resizeRatio);
                int pointY = (int) (point.y * resizeRatio);
                Log.d(TAG, "x, y " + pointX + " " + pointY);
                // Get the point of the face landmarks
                if (point == landmarks.get(36) || point == landmarks.get(37) || point == landmarks.get(38)
                        || point == landmarks.get(39) || point == landmarks.get(40) || point == landmarks.get(41)
                        || point == landmarks.get(42) || point == landmarks.get(43) || point == landmarks.get(44)
                        || point == landmarks.get(45) || point == landmarks.get(46) || point == landmarks.get(47)) {
                    //canvas.drawCircle(pointX, pointY, 2, mFaceLandmardkPaint2);
                    Log.d(TAG, "LANDMARK of eyes " + pointX + ", " + pointY);
                }
                //canvas.drawCircle(pointX, pointY, 2, mFaceLandmardkPaint2);
            }*/
            int a  = (MAX_WIDTH- (landmarks.get(39).x - landmarks.get(36).x))/2;
            x = landmarks.get(36).x - a;
            int ystart;
            int yend;
            if (landmarks.get(37).y < landmarks.get(38).y)
                ystart = landmarks.get(37).y;
            else
                ystart = landmarks.get(38).y - 4;
            if (landmarks.get(40).y > landmarks.get(41).y)
                yend = landmarks.get(40).y;
            else
                yend = landmarks.get(41).y;
            int b  = (MAX_HEIGHT - (yend - ystart))/2;
            y = ystart - b;
        }
        Bitmap bmRet = Bitmap.createBitmap(mutableBitmap, x, y, MAX_WIDTH, MAX_HEIGHT);
        return bmRet;
    }

    private Bitmap setLeftEyeLandmarks(Bitmap bmp){
        Log.d(TAG, "in setEyeLandmark");
        Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        List<VisionDetRet> results = faceDet.detect(mutableBitmap);
        Log.d(TAG, "faces " + results.size());
        int x = 0; int y = 0; int width = 0; int height = 0;
        for (final VisionDetRet ret : results) {
            Log.d(TAG, "in VisionDet");
            float resizeRatio = 1.0f;
            String label = ret.getLabel(); // If doing face detection, it will be 'Face'
            Log.d(TAG, "label" + label);
            ArrayList<Point> landmarks = ret.getFaceLandmarks();
            Log.d(TAG, "list length" + landmarks.size());
            /*for (Point point : landmarks) {
                int pointX = (int) (point.x * resizeRatio);
                int pointY = (int) (point.y * resizeRatio);
                Log.d(TAG, "x, y " + pointX + " " + pointY);
                // Get the point of the face landmarks
                if (point == landmarks.get(36) || point == landmarks.get(37) || point == landmarks.get(38)
                        || point == landmarks.get(39) || point == landmarks.get(40) || point == landmarks.get(41)
                        || point == landmarks.get(42) || point == landmarks.get(43) || point == landmarks.get(44)
                        || point == landmarks.get(45) || point == landmarks.get(46) || point == landmarks.get(47)) {
                    //canvas.drawCircle(pointX, pointY, 2, mFaceLandmardkPaint2);
                    Log.d(TAG, "LANDMARK of eyes " + pointX + ", " + pointY);
                }
                //canvas.drawCircle(pointX, pointY, 2, mFaceLandmardkPaint2);
            }*/

            int a  = (MAX_WIDTH - (landmarks.get(45).x - landmarks.get(42).x))/2;
            x = landmarks.get(42).x - a;
            int ystart;
            int yend;
            if (landmarks.get(43).y < landmarks.get(44).y)
                ystart = landmarks.get(43).y;
            else
                ystart = landmarks.get(44).y;
            if (landmarks.get(47).y > landmarks.get(46).y)
                yend = landmarks.get(47).y;
            else
                yend = landmarks.get(46).y;
            int b  = (MAX_HEIGHT - (yend - ystart))/2;
            y = ystart - b;
        }
        Bitmap bmRet = Bitmap.createBitmap(mutableBitmap, x, y, MAX_WIDTH, MAX_HEIGHT);
        return bmRet;
    }

    private String[] lineSplit(List<String> list, int lineNum){
        String line = list.get(lineNum);
        Log.d(TAG, "Line: " + line);
        return line.split(" ");
}

    private void init(){
        mFaceLandmardkPaint2 = new Paint();
        mFaceLandmardkPaint2.setColor(Color.RED);
        mFaceLandmardkPaint2.setStrokeWidth(2);
        mFaceLandmardkPaint2.setStyle(Paint.Style.STROKE);
    }
}
