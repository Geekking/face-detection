package org.sysu.bbalbum.util;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class UtilDelegate {
	private DetectUtil             mdetectUtil = null;
    private PcaNetOperation        mpcaOperation = null;
    private static String          TAG = "Image Process";
    private Context                mContext;
    public UtilDelegate(Context context){
    	this.mdetectUtil = new DetectUtil(context);
    	this.mpcaOperation = new PcaNetOperation(context);
    }
    public void testDetectTestFaces(){
    	this.mdetectUtil.testDetectEffect();
    }
    public List<Mat> checkBaby(String imagePath){
    	long e1 = Core.getTickCount();
    	List<Mat> childFaces = new ArrayList<Mat>();
    	List<Mat>              mFaces;
        List<Boolean>		   mChildren;
        
    	mFaces = this.mdetectUtil.detectImage(imagePath);
    	Log.i(TAG,"Face detected and face count is " + mFaces.size() );
    	long e2 = Core.getTickCount();
    	double time = (e2 - e1)/ Core.getTickFrequency();	
    	Log.i(TAG,"detection time elapsed: "+time);
    	//Toast.makeText(MainActivity.mactivity, "Face detected and face count is " + mFaces.size(), Toast.LENGTH_LONG).show();
    	if(mFaces.size() > 0){
    		mChildren = mpcaOperation.RecogChild(mFaces);
        	Log.i(TAG,"Face Recognized ");
        	long e3 = Core.getTickCount();
        	time = (e3 - e2)/ Core.getTickFrequency();	
        	Log.i(TAG,"recognise time elapsed: "+time);

    		int faceSize = mFaces.size();
    		for(int i = 0;i < faceSize; i++){
    			if(mChildren.get(i)){
    				childFaces.add(mFaces.get(i));
    			}
    		}
    	}
    	return childFaces;
    }

}
