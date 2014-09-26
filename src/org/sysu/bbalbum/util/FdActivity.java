package org.sysu.bbalbum.util;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import org.sysu.bbalbum.util.DetectUtil;
import org.sysu.bbalbum.util.R;

import android.os.Environment; 
public class FdActivity extends Activity  {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    
    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;
    private MenuItem 			   mItemTest;
    private DetectUtil             detectUtil;
    
    private String[]               mDetectorName;


   // private CameraBridgeViewBase   mOpenCvCameraView;
    private ImageView  originImageView;
    private ImageView  detectedImageView;
    private Button 	   detectBtn;
    /*
     * context 
     * dirName dirname in the asset 
     *  targetPath root absolute path of the target folder
     */
    public static boolean copyAssetData(Context context, String dirName, String targetPath) {
	    try {
	    	AssetManager assetManager = context.getAssets();
	    	String identiString = "helloworld";
	    	File file = new File(targetPath + File.separator + dirName);
	    	if(!file.exists() && !file.isDirectory())
	            file.mkdirs();
	    	String[] strings = assetManager.list(dirName);
	    	for (String string2 : strings) {
	    		//Log.e("tag", string2);
	    		InputStream inputStream = assetManager.open(dirName+File.separator+string2);
		        FileOutputStream output = new FileOutputStream(file + File.separator+ identiString + string2);//.substring(string2.length()-4, string2.length()));
		        
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
    public void onCreate(Bundle savedInstanceState) throws OutOfMemoryError {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.face_detect_surface_view);
        
        this.detectBtn = (Button)findViewById(R.id.detect_btn);
        this.originImageView = (ImageView)findViewById(R.id.origin_image);
        this.detectedImageView = (ImageView)findViewById(R.id.detected_image);
        
        this.detectUtil = new DetectUtil(this);
        
        mDetectorName[this.detectUtil.JAVA_DETECTOR] = "Java";
        mDetectorName[this.detectUtil.NATIVE_DETECTOR] = "Native (tracking)";
        
        this.originImageView.setImageDrawable(this.getResources().getDrawable(R.drawable.lena));

        this.detectedImageView.setImageDrawable(this.getResources().getDrawable(R.drawable.lena));
        
        this.detectBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				detectUtil.testDetectEffect();
				
			}
        	
        });
             
    }
    private static int computeInitialSampleSize(BitmapFactory.Options options,int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
 
        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
 
        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
         
          
    }  
    public static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,maxNumOfPixels);
 
        int roundedSize;
        if (initialSize <= 8 ) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
 
        return roundedSize;
    }
    @Override
    public void onPause()
    {
        super.onPause();
//        if (mOpenCvCameraView != null)
//            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        
    }

    public void onDestroy() {
        super.onDestroy();
//        mOpenCvCameraView.disableView();
    }

 
   

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemTest   = menu.add("Face size 10%");
        
        mItemType   = menu.add(mDetectorName[this.detectUtil.mDetectorType]);
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
            this.detectUtil.setMinFaceSize(0.1f);
            
        }
        return true;
    }

   
    
    
}
