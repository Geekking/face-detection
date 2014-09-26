package org.sysu.bbalbum.util;
/*build command`
 * javah -classpath /Users/apple/Documents/develop/mobile/android/adt/sdk/platforms/android-19/android.jar:../bin/classes:/Users/apple/Documents/develop/mobile/3rd/OpenCV-2.4.9-android-sdk/sdk/java/bin/opencv\ library\ -\ 2.4.9.jar org.opencv.samples.facedetect.DetectionBasedTracker
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import android.util.Log;
import android.widget.Toast;

import org.sysu.bbalbum.util.R;

import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;

import android.os.Environment; 

public class DetectUtil {
	private static final String TAG = "DetectionUtil";
	private  Context context;
	
    private File                   mCascadeFile;
    private Mat                    mRgba;
    private Mat                    mGray;
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    
    public int                    mDetectorType       = JAVA_DETECTOR;
    
    private float                  mRelativeFaceSize   = 0.1f;
    private int                    mAbsoluteFaceSize   = 0;

    public CascadeClassifier      mJavaDetector;
    public DetectionBasedTracker  mNativeDetector;

    public CascadeClassifier      mJavaEyeDetector;
    public DetectionBasedTracker  mNativeEyeDetector;
    
    public Size                   mSize = new Size(200,200);

	public DetectUtil(Context mcontext){
		Log.i(TAG, "Instantiated new " + this.getClass());
		this.context = mcontext;
		mRgba = new Mat();
		mGray = new Mat();
		this.loadDetector();
		
        
	}
	public Mat rotateImage(Mat originImage,int rotationAngle){
		double radians = Math.toRadians(rotationAngle);
		double sin = Math.abs(Math.sin(radians));
		double cos = Math.abs(Math.cos(radians));

		int newWidth = (int) (originImage.width() * cos + originImage.height() * sin);
		int newHeight = (int) (originImage.width() * sin + originImage.height() * cos);

		int[] newWidthHeight = {newWidth, newHeight};

		//??????????????????????????????newWidth / newHeight???
		int pivotX = newWidthHeight[0]/2; 
		int pivotY = newWidthHeight[1]/2;

		org.opencv.core.Point center = new org.opencv.core.Point(pivotX, pivotY);
		Size targetSize = new Size(newWidthHeight[0], newWidthHeight[1]);

		//???????????????????????????????????????????????????
		Mat dummy = new Mat(targetSize, originImage.type());
		
		Mat rotMat = Imgproc.getRotationMatrix2D(center, rotationAngle, 1.0);
		
		Imgproc.warpAffine(originImage, dummy, rotMat, originImage.size());
		
		return dummy;
	}
	
	public boolean detectEye(Mat can_face){
		return true;
	}
	public Rect[] genQuaterRect(Mat inputFrame){
		Rect  rects[] = new Rect[5];
		int rows = inputFrame.rows();
		int cols = inputFrame.cols();
		rects[0] = new Rect(new Point(0,0),new Point(rows,cols));
		rects[1] = new Rect(new Point(0,0),new Point(rows / 2,cols / 2));
		rects[2] = new Rect(new Point(0,cols / 2),new Point(rows / 2,cols));
		rects[3] = new Rect(new Point(rows / 2,0),new Point(rows ,cols / 2));
		rects[4] = new Rect(new Point(rows / 2 ,cols / 2),new Point(rows ,cols));
		
		return rects;
	}
	public List<Mat> detectFaceOnMat(Mat mat){
		List<Mat> detectedFaces = new ArrayList<Mat>();
		
		File rootPath = Environment.getExternalStorageDirectory();

		File facesDir =new File(rootPath + File.separator + "detected_faces");
		if(!facesDir.exists() && !facesDir.isDirectory()){
			facesDir.mkdir();
		}
		
		if (mAbsoluteFaceSize == 0) {
            int height = mat.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }
        MatOfRect faces = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
        	
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mat, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), mGray.size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null){
	        	mNativeDetector.detect(mat, faces);
            }
        }else {
            Log.e(TAG, "Detection method is not selected!");
        }
        
        Rect[] facesArray = faces.toArray();
        Log.i(TAG,"detected faces : " + facesArray.length);
        
        if(facesArray.length > 0){
        	for (int i = 0; i < facesArray.length; i++){
        		//TODO: detect eyes here
        		
        		Rect roi = new Rect(facesArray[i].tl(),facesArray[i].br());
        		Mat can_faces = new Mat(mat,roi);
        		
        		Mat tmp  = new Mat();
        		Imgproc.resize(can_faces, tmp, new Size(36,56));
        		
        		Highgui.imwrite(facesDir.getAbsolutePath() + File.separator +"faces_"+ i + ".jpg",tmp);
        		
        		Mat changed = new Mat(tmp.rows(), tmp.cols(), CvType.CV_64F);
        		tmp.convertTo(changed, CvType.CV_64F, 1.0 / 255);
        		detectedFaces.add(changed);
        	}
        }
        return detectedFaces;
	}
	public List<Mat> detectImage(String imagePath){
		File imgFile = new  File(imagePath);
		if(!imgFile.exists()){
			Log.e(TAG,"file not found at " + imgFile.getAbsolutePath());
			return null;
		}
		Mat inputFrame = Highgui.imread(imgFile.getAbsolutePath(),1);
		Log.i(TAG,"detecting images at " + imgFile.getAbsolutePath());
		
		List<Mat> detectedFaces = new ArrayList<Mat>();
		
		
		
		int deltaAngle = 180;
		int numRotates = 180 / deltaAngle;
		
		inputFrame.copyTo(mRgba);
		Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY,1);
		
		Rect[] cropROIs = this.genQuaterRect(mGray);
		
		for(int ci = 0;ci < cropROIs.length ; ci++ ){
			Mat cropImg;
			if(ci ==0){
				cropImg = mGray;
			}else{
				cropImg = new Mat(mGray,cropROIs[ci]);
				break;
			}
			Imgproc.resize(cropImg, mGray, this.mSize);
			
			detectedFaces.addAll(this.detectFaceOnMat(mGray));
			if(detectedFaces.size() == 0){
				for(int i = 1 ; i < numRotates; i++){
					if(i != 0 ){
						detectedFaces.addAll(this.detectFaceOnMat(this.rotateImage(mGray, i * deltaAngle)));
					}
					if(detectedFaces.size() > 0){
				        return detectedFaces;
					}
				}
				for(int i = -1 ; i >= -numRotates ; i--){
					if(i != 0 ){
						detectedFaces.addAll(this.detectFaceOnMat(this.rotateImage(mGray, i * deltaAngle)));
					}
					if(detectedFaces.size() > 0){
				        return detectedFaces;
					}
				}
			}else{
				break;
			}
	        
        }
        return detectedFaces;
	}
	public Mat detectImageAndDraw(String imagePath){
		File imgFile = new  File(imagePath);
		if(!imgFile.exists()){
			Log.e(TAG,"file not found at " + imgFile.getAbsolutePath());
			return null;
		}
		Mat inputFrame = Highgui.imread(imgFile.getAbsolutePath(),1);
		Log.i(TAG,"detecting images at " + imgFile.getAbsolutePath());
		
		File parentFold = (new File(imagePath)).getParentFile() ;
		File rotateFolder = new File(parentFold + File.separator + "rotated");
		if(!rotateFolder.exists() && !rotateFolder.isDirectory())
			rotateFolder.mkdirs();
//		File grayFold = new File(parentFold + File.separator + "grayed");
//		if(!grayFold.exists() && !grayFold.isDirectory())
//			grayFold.mkdirs();
		
		int rotateAngle = 0;
		int deltaAngle = 900;
		List<Mat> detectedFaces = new ArrayList<Mat>();
		while(true){
			if(Math.abs(rotateAngle) >= 360){
				break;
			}
			if(rotateAngle != 0){
				 this.mRgba = this.rotateImage(inputFrame, rotateAngle);
				 
				 String savePath = rotateFolder.getAbsolutePath() + File.separator + "rotated_"+ rotateAngle + "_"+ imgFile.getName() ;
				 if(this.mRgba != null)
						Highgui.imwrite( savePath, this.mRgba);
			
			}
			else {
				inputFrame.copyTo(mRgba);
			}
			//mGray = inputFrame.gray();
			//mRgba = inputFrame.rgba();
	
			Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGB2GRAY,1);
//			String savePath = grayFold.getAbsolutePath() + File.separator + "gray_" + imgFile.getName();
//			 if(this.mRgba != null)
//					Highgui.imwrite( savePath, this.mGray);
//			 
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
	                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), mGray.size());
	        }
	        else if (mDetectorType == NATIVE_DETECTOR) {
	            if (mNativeDetector != null){
		        	//mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
		        	//mNativeDetector.setMaxObjectSize(mGray.rows());
		        	mNativeDetector.detect(mGray, faces);
	            }
	        }else {
	            Log.e(TAG, "Detection method is not selected!");
	        }
	        Rect[] facesArray = faces.toArray();
	        Log.i(TAG,"detected angle: " + rotateAngle);
	        Log.i(TAG,"detected faces: " + facesArray.length);
	        if(facesArray.length > 0){
	        	for (int i = 0; i < facesArray.length; i++){
	        		//TODO: detect eyes here
	        		Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
	        		
	        	}
	        	break;
	        }else{
	        	rotateAngle -= deltaAngle;
	        }
	       
		}
		return mRgba;
	}
	
	public void detectBatchImages(String path, String extension, boolean isIterative){
		File[] files =new File(path).listFiles();
		Log.i(TAG,path);
		long startTime = System.nanoTime();
		File rooFile = Environment.getExternalStorageDirectory();
		File detectedFold = new File(rooFile.getAbsoluteFile() + File.separator + "detected");
		if(!detectedFold.exists() && !detectedFold.isDirectory())
			detectedFold.mkdirs();
		
		for (int i =0; i < files.length; i++)
	    {
	        File f = files[i];
	        if (f.isFile())
	        {
	            if (f.getPath().substring(f.getPath().length() - extension.length()).equals(extension))
	            {
	            	
	            	Mat mat = this.detectImageAndDraw(f.getAbsolutePath());
	            	
	            	String savePath = detectedFold.getAbsolutePath() + File.separator + "detected_"+ f.getName() ;
					if(mat != null)
						Highgui.imwrite( savePath,mat);
	            }
	        }
	        else if (f.isDirectory() && f.getPath().indexOf("/.") == -1 && isIterative) //??????????????????????????????/????????????
	        	detectBatchImages(f.getPath(), extension, isIterative);
	    }

		long consumingTime = (System.nanoTime() - startTime)/1000000;
		Toast.makeText(this.context, "Average Consuming time: "+ String.valueOf((consumingTime)/files.length) +"ms",
                Toast.LENGTH_LONG).show();
     
		Log.i(TAG,"Total Consuming time: "+ String.valueOf(consumingTime) +"ms");
    	Log.i(TAG,"Total Images        : "+String.valueOf(files.length)  );
    	Log.i(TAG,"Average Consuming time: "+ String.valueOf((consumingTime)/files.length) +"ms");

	}
	
	public boolean loadDetctorFrom(int resouceID,String xmlfileName,int type){
		try {
            //load cascade file from application resources
			File cascadeDir = this.context.getDir("cascade", Context.MODE_PRIVATE);
            
			InputStream is = this.context.getResources().openRawResource(resouceID);
            
            mCascadeFile = new File(cascadeDir, xmlfileName);
            
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            if(type == 0){
            	mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            	if (mJavaDetector.empty()) {
            		Log.e(TAG, "Failed to load cascade classifier");
            		mJavaDetector = null;
	            } else
	                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
	
	            mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
	            
	            //mNativeDetector.setMinNeighbors(1);
	            
	            //mNativeDetector.setScaleFactor(1.1);
	            
            }
            cascadeDir.delete();
            
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
            return false;
        }

	}
	public void loadDetector(){
		Log.i(TAG,"detection loading");
		String xmlFile = "lbpcascade_frontalface.xml";
		//String xmlFile = "haarcascade_frontalface_alt.xml";
		this.loadDetctorFrom(R.raw.haarcascade_frontalface_alt, xmlFile,0);
		Log.i(TAG,"detection loaded");
		
		//TODO: load eye detector
	}
	
	public void testDetectEffect(){
		//int rid = R.drawable.image;
		//String path = this.context.getString(rid);
		if (android.os.Environment.getExternalStorageState().equals(
	               android.os.Environment.MEDIA_MOUNTED)) {
			
	           Toast.makeText(this.context, "???SD???",
	                   Toast.LENGTH_LONG).show();
	           
	           File imageDir2 = this.context.getDir("Faces", Context.MODE_PRIVATE);
	           this.detectBatchImages(imageDir2.getAbsolutePath(), "jpg", false);
	   		
	    }else{
	    	Toast.makeText(this.context, "?????????SD???",
	                   Toast.LENGTH_SHORT).show();
	           
	    }
        
	}
	public void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }
	
	public void setDetectorType(int type) {
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
