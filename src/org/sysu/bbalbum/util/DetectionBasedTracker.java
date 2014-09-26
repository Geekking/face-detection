package org.sysu.bbalbum.util;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class DetectionBasedTracker
{
    public DetectionBasedTracker(String cascadeName, int minFaceSize) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize);
       // mHaarCascadeObj = nativeLoadCascadeObj(cascadeName);
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }
    public void setMaxObjectSize(int size) {
        nativeSetMaxObjectSize(mNativeObj, size);
    }
    public void setScaleFactor(double factor) {
        nativeSetScaleFactor(mNativeObj, factor);
    }
    public void setMinNeighbors(int minNeighbors) {
        nativeSetMinNeighbors(mNativeObj, minNeighbors);
    }
    public void detect(Mat imageGray, MatOfRect faces) {
        nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
    }
    public void detectMultiScale(Mat imageGray, MatOfRect faces, double scale_factor, int min_neighbors,int flags,int min_size,int max_size){
    	//nativeDetectMultiScale(mHaarCascadeObj,imageGray.getNativeObjAddr(),faces.getNativeObjAddr(),scale_factor,min_neighbors,flags,min_size,max_size);
    }
    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private long mNativeObj = 0;
    private long mHaarCascadeObj = 0;
    
    //private static native long nativeLoadCascadeObj(String cascadeName);
    
    private static native long nativeCreateObject(String cascadeName, int minFaceSize);
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeStart(long thiz);
    private static native void nativeStop(long thiz);
    
    private static native void nativeSetFaceSize(long thiz, int size);
    private static native void nativeSetMaxObjectSize(long thiz, int size);
    private static native void nativeSetScaleFactor(long thiz, double factor);
    private static native void nativeSetMinNeighbors(long thiz, int mins);
    
    
    private static native void nativeDetect(long thiz, long inputImage, long faces);
    
    //private static native void nativeDetectMultiScale(long thiz, long inputImage, long faces,double scale_factor, int min_neighbors,int flags,int min_size,int max_size );

}
