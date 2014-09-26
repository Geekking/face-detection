package org.sysu.bbalbum.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.sysu.bbalbum.util.R;

import org.opencv.ml.CvSVM;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class PcaNetOperation {
	private CvSVM SVM = new CvSVM(); 
	private List<Mat> Filters = new ArrayList<Mat>(); 
	private  Context mContext;
	
	PCANet pcaNet = new PCANet();
		
	public PcaNetOperation(Context context){
		
    	pcaNet.NumStages = 2;
    	pcaNet.PatchSize = 7;
    	pcaNet.NumFilters[0] = 8;
     	pcaNet.NumFilters[1] = 8;
     	pcaNet.HistBlockSize[0] = 14;
     	pcaNet.HistBlockSize[1] = 9;
     	pcaNet.BlkOverLapRatio = 0.5;
    	this.mContext = context;

        InputStream XmlFileInputStream = mContext.getResources().openRawResource(R.raw.all_age_48_svm_56x36_2); // getting XM
        String svmPath = "";
        InputStream FiltersFileInputStream = mContext.getResources().openRawResource(R.raw.all_age_48_filters_56x36_2); // getting XM
        String FiltersPath = "";
        Mat temp = null;
       
		try {
			svmPath = readInputWriteToFile(XmlFileInputStream, "svms.xml");
			SVM.load(svmPath);
			FiltersPath = readInputWriteToFile(FiltersFileInputStream, "Filters.xml");
			TaFileStorage storage = new TaFileStorage();
			storage.open(FiltersPath);
			temp = storage.readMat("filter1");
			Filters.add(temp);
			temp = storage.readMat("filter2");
			Filters.add(temp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private String readInputWriteToFile(InputStream is, String fname) throws IOException {
		
		int num = 0;
		
		String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_svms");    
        myDir.mkdirs();
		
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete (); 
        
		 FileOutputStream os = new FileOutputStream(file);
		 byte[] buffer = new byte[4096];
		 int bytesRead;
		 while ((bytesRead = is.read(buffer)) != -1) {
			 os.write(buffer, 0, bytesRead);
		 }
		 is.close();
		 os.close();

        return file.getAbsolutePath();
    }
	public List<Boolean> RecogChild(List<Mat> Imgs){
		
		Hashing_Result hashing_r;
    	PCA_Out_Result out;

    	int ImgsSIze = Imgs.size();
    	
    	List<Boolean> result = new ArrayList<Boolean>();
    	
    	//long e1 = Core.getTickCount();
    	for(int i=0; i<ImgsSIze; i++){
    		out = new PCA_Out_Result();
    		out.OutImgIdx.add(0);
    		out.OutImg.add(Imgs.get(i)); //change testImg to Imgs
    		
    		out = Tools.PCA_output(out.OutImg, out.OutImgIdx, pcaNet.PatchSize, 
    			pcaNet.NumFilters[0], Filters.get(0), 2);
    		
    		for(int j=1; j<pcaNet.NumFilters[1]; j++)
    			out.OutImgIdx.add(j);

    		out = Tools.PCA_output(out.OutImg, out.OutImgIdx, pcaNet.PatchSize, 
    			pcaNet.NumFilters[1], Filters.get(1), 2);		
    		hashing_r = Tools.HashingHist(pcaNet, out.OutImgIdx, out.OutImg);	
    		hashing_r.Features.convertTo(hashing_r.Features, CvType.CV_32F);
    		result.add(SVM.predict(hashing_r.Features) <= 3 ? true : false);
    	}
//    	long e2 = Core.getTickCount();
//    	double time = (e2 - e1)/ Core.getTickFrequency();	
//    	Log.e("recognize time usage: ", ""+time);
		return result;
		
	}
}
