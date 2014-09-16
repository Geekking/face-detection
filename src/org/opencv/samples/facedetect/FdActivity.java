package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.opencv.samples.facedetect.DetectUtil;
import android.os.Environment; 
public class FdActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    
    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;
    private MenuItem 			   mItemTest;
    private DetectUtil             detectUtil;
    private Mat                    mRgba;
    private Mat                    mGray;
    
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;

    public static boolean copyAssetData(Context context, String dirName, String targetPath) {
	    try {
	    	AssetManager assetManager = context.getAssets();
	    	String initstrString = "(人教新课标)一年级语文下册课件 两只鸟蛋";
	    	File file = new File(targetPath + File.separator + "yuwen"+File.separator+"1"+File.separator+initstrString);
	    	if(!file.exists() && !file.isDirectory())
	            file.mkdirs();
	    	String[] string = assetManager.list("init");
	    	for (String string2 : string) {
	    		//Log.e("tag", string2);
	    		InputStream inputStream = assetManager.open(dirName+File.separator+string2);
		        FileOutputStream output = new FileOutputStream(file + File.separator+ initstrString + string2.substring(string2.length()-4, string2.length()));
		        
		        byte[] buf = new byte[10240];
		        int count = 0;
		        while ((count = inputStream.read(buf)) > 0) {
		            output.write(buf, 0, count);
		        }
		        output.close();
		        
		        inputStream.close();
			}
	        
	    } catch (IOException e) {
	        
	// TODO Auto-generated catch block
	        e.printStackTrace();
	        return false;
	    }
	    return true;
	}
    public FdActivity() {
        mDetectorName = new String[2];
        
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.face_detect_surface_view);
        
        File rootPath = Environment.getExternalStorageDirectory();
        FdActivity.copyAssetData(this, "init", rootPath + File.separator+"hello");
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        this.detectUtil = new DetectUtil(this);
        
        mDetectorName[this.detectUtil.JAVA_DETECTOR] = "Java";
        mDetectorName[this.detectUtil.NATIVE_DETECTOR] = "Native (tracking)";
        
        mOpenCvCameraView.enableView();
        
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }
   
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
//        mGray = inputFrame.gray();
//        if (mAbsoluteFaceSize == 0) {
//            int height = mGray.rows();
//            if (Math.round(height * mRelativeFaceSize) > 0) {
//                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
//            }
//            this.detectUtil.mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
//        }
//
//        MatOfRect faces = new MatOfRect();
//
//        if (this.detectUtil.mDetectorType == this.detectUtil.JAVA_DETECTOR) {
//            if (this.detectUtil.mJavaDetector != null)
//                this.detectUtil.mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
//                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
//        }
//        else if (this.detectUtil.mDetectorType == this.detectUtil.NATIVE_DETECTOR) {
//            if (this.detectUtil.mNativeDetector != null)
//                this.detectUtil.mNativeDetector.detect(mGray, faces);
//        }
//        else {
//            Log.e(TAG, "Detection method is not selected!");
//        }
//
//        Rect[] facesArray = faces.toArray();
//        for (int i = 0; i < facesArray.length; i++)
//            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[this.detectUtil.mDetectorType]);
        mItemTest   = menu.add("detect local images");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            this.detectUtil.setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            this.detectUtil.setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            this.detectUtil.setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            this.detectUtil.setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (this.detectUtil.mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            this.detectUtil.setDetectorType(tmpDetectorType);
        }else if(item == mItemTest){
        	if (mOpenCvCameraView != null)
                mOpenCvCameraView.disableView();
        	this.detectUtil.testDetectEffect();
        }
        return true;
    }

   
    
    
}
