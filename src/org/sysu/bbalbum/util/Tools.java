package org.sysu.bbalbum.util;


import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.util.Log;


class PCA_Out_Result {
	public List<Mat> OutImg;
	public List<Integer> OutImgIdx;
	
	public PCA_Out_Result(){
		OutImg = new ArrayList<Mat>();
		OutImgIdx = new ArrayList<Integer>();
	}
} 


class Hashing_Result {
	Mat Features;
	List<Integer> BlkIdx;
	
	public Hashing_Result(){
		Features = new Mat();
		BlkIdx = new ArrayList<Integer>();
	}
} 


class PCANet {
	int NumStages;
	int PatchSize;
	int[] NumFilters;
	int[] HistBlockSize;
	double BlkOverLapRatio;
	
	public PCANet(){
		NumFilters = new int[2];
		HistBlockSize = new int[2];
	}
} 

public class Tools {
	
	
	private static final int ROW_DIM = 0;
	private static final int COL_DIM = 1;
	
	public static Mat im2colstep(Mat InImg, int[] blockSize, int[] stepSize){
		
		int r_row = blockSize[ROW_DIM] * blockSize[COL_DIM];
		int row_diff = InImg.rows() - blockSize[ROW_DIM];
		int col_diff = InImg.cols() - blockSize[COL_DIM];
		//Log.e("im2colstep", "" + InImg.rows() + "," + InImg.cols());
		int r_col = (row_diff / stepSize[ROW_DIM] + 1) * (col_diff / stepSize[COL_DIM] + 1);
		//Log.e("im2colstep", "" + r_col + "," + r_row);
		Mat OutBlocks = new Mat(r_col, r_row, InImg.depth());

		int blocknum = 0;
		
		int size = (int) InImg.total() * InImg.channels();
	    double[] buff = new double[size];
	    InImg.get(0, 0, buff);
	    int size2 = r_col * r_row;
	    double[] Obuff = new double[size2];
	    int col = InImg.cols();

		for(int j=0; j<=col_diff; j+=stepSize[COL_DIM]){
			for(int i=0; i<=row_diff; i+=stepSize[ROW_DIM]){
				
				for(int m=0; m<blockSize[ROW_DIM]; m++){
					for(int l=0; l<blockSize[COL_DIM]; l++)
						Obuff[blocknum * r_row + blockSize[ROW_DIM] * l + m] = buff[(i + m) * col + j + l];
						//OutBlocks.put(blocknum, blockSize[COL_DIM] * l + m, buff[(i + m) * col + j + l]);
						//OutBlocks.put(blocknum, blockSize[COL_DIM] * l + m, InImg.get(i + m, j + l)[0]);
				}
				blocknum ++;
			}
		}
		OutBlocks.put(0, 0, Obuff);
		return OutBlocks;
		
	}
	
	public static Mat im2col_general(Mat InImg, int[] blockSize, int[] stepSize){
		
		int channels = InImg.channels();
		
		List<Mat> layers = new ArrayList<Mat>();
		if(channels > 1)
			Core.split(InImg, layers);
		else
			layers.add(InImg);
		
		Mat AllBlocks = new Mat();
		List<Mat> src = new ArrayList<Mat>();
		
		int size = layers.size();
		
		long e1 = Core.getTickCount();
		for(int i=0; i<size; i++)
			src.add(im2colstep(layers.get(i), blockSize, stepSize));	
		long e2 = Core.getTickCount();
    	double time = (e2 - e1)/ Core.getTickFrequency();
    	Log.e(" im2col time usage: ", "" + time);
		
		Core.hconcat(src, AllBlocks);
		return AllBlocks.t();
	}
	
	public static PCA_Out_Result PCA_output(List<Mat> InImg, List<Integer> InImgIdx, int PatchSize, int NumFilters, Mat Filters, int threadnum){
		
		PCA_Out_Result result = new PCA_Out_Result();
		
		int img_length = InImg.size();
		int mag = (PatchSize - 1) / 2; 
		//int channels = InImg.get(0).channels();
		
		//Log.e("fake", "" + InImg.get(0).type());
		
		Mat img = new Mat();
		
		
		int[] blockSize = new int[2];
		int[] stepSize = new int[2];

		for(int i=0; i<2; i++){
			blockSize[i] = PatchSize;
			stepSize[i] = 1;
		}

		Mat temp = new Mat();
		Mat mean = new Mat();
		Mat temp2 = new Mat();
		Mat temp3 = new Mat();
		
		//Mat fake1 = Mat.ones(49, 49, CvType.CV_64F);
		//Mat fake2 = Mat.ones(49, 3, CvType.CV_64F);
		Mat fake3 = new Mat();
		//Core.multiply(fake1, fake2, temp);
		
		//Core.gemm(fake1, fake2, 1, fake3, 0, fake3, 0);
		
		for(int i=0; i<img_length; i++){
			
			Imgproc.copyMakeBorder(InImg.get(i), img, mag, mag, mag, mag, Imgproc.BORDER_ISOLATED);
			
			temp = im2col_general(img, blockSize, stepSize);

			Core.reduce(temp, mean, 0, Core.REDUCE_AVG);
			
			temp3.create(0, temp.cols(), temp.type());

			//Log.e("fake", "" + temp3.rows() + ",  " + temp3.cols() + ", " + Filters.rows() + ", " + Filters.cols());
			
			for(int j=0; j<temp.rows(); j++){
				Core.subtract(temp.row(j), mean.row(0), temp2);
				temp3.push_back(temp2.row(0));
			}
			
			//Log.e("fake", "" + temp3.rows() + ",  " + temp3.cols() + ", " + Filters.row(0).rows() + ", " + Filters.row(0).cols());
			
			
			
			//temp = new Mat(1, temp3.cols(), CvType.CV_64F);
			result.OutImgIdx.add(InImgIdx.get(i));
			for(int j=0; j<NumFilters; j++){
		
				Core.gemm(Filters.row(j), temp3, 1, fake3, 0, temp);
				
				temp = temp.reshape(0, InImg.get(i).cols());
				result.OutImg.add(temp.t());
			}
		}
		
		return result;
	}
	
	
	public static double round(double r){  
	    return (r > 0.0) ? Math.floor(r + 0.5) : Math.ceil(r - 0.5);  
	}  

	public static Hashing_Result HashingHist(PCANet PcaNet, List<Integer> ImgIdx, List<Mat> Imgs){
		Hashing_Result ha_result = new Hashing_Result();

		int length = Imgs.size();
		int NumFilters =  PcaNet.NumFilters[PcaNet.NumStages - 1];
		int NumImgin0 = length / NumFilters;

		Mat T = new Mat();
		Mat tempMat = Imgs.get(0);
		int row = tempMat.rows();
		int col = tempMat.cols();
		int depth = tempMat.depth();

		double[] map_weights = new double[NumFilters];
		Mat temp = new Mat();
		for(int i=NumFilters - 1; i>=0; i--)
			map_weights[NumFilters - 1 - i] = Math.pow(2.0, (double)i);

		int[] Ro_BlockSize = new int[2];
		double rate = 1 - PcaNet.BlkOverLapRatio;
		for(int i=0 ;i<PcaNet.HistBlockSize.length; i++)
			Ro_BlockSize[i] = (int)round(PcaNet.HistBlockSize[i] * rate);
		
		Mat BHist = new Mat();

		int ImgIdx_length = ImgIdx.size();
		int[] new_idx = new int[ImgIdx_length];
		for(int i=0; i<ImgIdx_length; i++)
			new_idx[ImgIdx.get(i)] = i;

		Scalar scalar;
		
		List<Mat> matList = new ArrayList<Mat>();
		
		for(int i=0; i<NumImgin0; i++){
			T = Mat.zeros(row, col, depth);	

			for(int j=0; j<NumFilters; j++){
				temp = Heaviside(Imgs.get(NumFilters * new_idx[i] + j));
				
				scalar = new Scalar(map_weights[j]);
				Core.multiply(temp, scalar, temp);
				
				Core.add(T, temp, T);
			}
			
			temp = im2col_general(T, PcaNet.HistBlockSize, Ro_BlockSize); 
			temp = Hist(temp, (int)(Math.pow(2.0, NumFilters)) - 1);
			
			temp = bsxfun_times(temp, NumFilters);
			
			matList.add(temp);
			
		}
		
		Core.hconcat(matList, BHist);

		int rows = BHist.rows();
		int cols = BHist.cols();

		ha_result.Features.create(1, rows * cols, BHist.type()); 

		int size = rows * cols;
	    double[] XBuff = new double[size];
	    BHist.get(0, 0, XBuff);

	    double[] HBuff = new double[size];
	    
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols; j++){	
				HBuff[j * rows + i] = XBuff[i * cols + j];
				//ha_result.Features.put(0, j * rows + i, XBuff[i * cols + j]);
				//ha_result.Features.put(0, j * rows + i, BHist.get(i, j));
			}
		}
		ha_result.Features.put(0, 0, HBuff);
	
		return ha_result;
	}
	
	
    public static Mat Heaviside(Mat X){
		int row = X.rows();
		int col = X.cols();
		int depth = X.depth();

		Mat H = Mat.zeros(row, col, depth);

		int size = (int) X.total() * X.channels();
	    double[] XBuff = new double[size];
	    X.get(0, 0, XBuff);
		
	    double[] HBuff = new double[size];
	    
		for(int i=0; i<row; i++){
			for(int j=0; j<col; j++){
				if(XBuff[i * col + j] > 0) HBuff[i * col + j] = 1;
				else HBuff[i * col + j] = 0;
			}
		}
		H.put(0, 0, HBuff);
		return H;
	}
	
	public static Mat Hist(Mat mat, int Range){
		Mat temp = mat.t();
		int row = temp.rows();
		int col = temp.cols();
		int depth = temp.depth();
		Mat Hist = Mat.zeros(row, Range + 1, depth);

		int index;
		double addone;
		for(int i=0; i<row; i++){
			for(int j=0; j<col; j++){
				index =  (int)temp.get(i, j)[0];
				addone = Hist.get(i, index)[0] + 1;
				Hist.put(i, index, addone);
			}
		}
		
		temp = Hist.t();

		return temp;
	}
	
	public static Mat bsxfun_times(Mat BHist, int NumFilters){
		
		int row = BHist.rows();
		int col = BHist.cols();
		
		int size = (int) BHist.total() * BHist.channels();
	    double[] buff = new double[size];
	    BHist.get(0, 0, buff);
		
		double[] sum = new double[col];
		for(int i=0; i<col; i++)
			sum[i] = 0;
		
		for(int i=0; i<row; i++)
			for(int j=0; j<col; j++)
				sum[j] += buff[i * col + j];
		
		double p = Math.pow(2.0, NumFilters);

		for(int i=0; i<col; i++)
			sum[i] = p / sum[i];

		double[] temp = new double[size];
		for(int i=0; i<row; i++)
			for(int j=0; j<col; j++){
				temp[i * col + j] = buff[i * col + j] * sum[j];
				
			}
		BHist.put(0, 0, temp);
		return BHist;
	}

}
