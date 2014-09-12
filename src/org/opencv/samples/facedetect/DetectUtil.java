package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import org.opencv.objdetect.CascadeClassifier;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;

public class DetectUtil extends Activity{
	private static final String TAG = "DetectionUtil";

    private File                   mCascadeFile;
    private Mat                    mRgba;
    private Mat                    mGray;
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    
    private int                    mDetectorType       = JAVA_DETECTOR;
    
    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetector;

    private CascadeClassifier      mJavaEyeDetector;
    private DetectionBasedTracker  mNativeEyeDetector;

	public DetectUtil(){
		this.loadDetector();
        Log.i(TAG, "Instantiated new " + this.getClass());
	}
	
	
	public boolean detectEye(Mat can_face){
		return true;
	}
	
	public Mat detectImage(String imagePath){
		
		Mat inputFrame = Highgui.imread(imagePath);
		inputFrame.copyTo(mRgba);
		//mGray = inputFrame.gray();
		//mRgba = inputFrame.rgba();
		Imgproc.cvtColor(inputFrame, mGray, Imgproc.COLOR_RGB2GRAY, 0);
		
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 1, 1, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }
        
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++){
        	//TODO: detect eyes here
        	Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        
        }
        return mRgba;
	}
	public void detectBatchImages(){
		
	}
	public boolean loadDetctorFrom(int resouceID,String xmlfileName,CascadeClassifier mmJavaDetector,DetectionBasedTracker mmNativeDetector){
		try {
            //load cascade file from application resources
            InputStream is = this.getResources().openRawResource(resouceID);
            File cascadeDir = this.getDir("cascade", Context.MODE_PRIVATE);
            
            mCascadeFile = new File(cascadeDir, xmlfileName);
            
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mmJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            mmNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
            cascadeDir.delete();
            
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
            return false;
        }

	}
	public void loadDetector(){
		String xmlFile = "lbpcascade_frontalface.xml";
		this.loadDetctorFrom(R.raw.lbpcascade_frontalface, xmlFile, mJavaDetector, mNativeDetector);
		//TODO: load eye detector
	}
	
	private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }
	
	private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }
	static {
    	
    	if ( OpenCVLoader.initDebug()){  
    		System.loadLibrary("detection_based_tracker");
    	}else{   
    		Log.e(TAG, "Init failed" );
        
    	}
    }
}
