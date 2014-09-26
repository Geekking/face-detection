package org.sysu.bbalbum.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import org.sysu.bbalbum.util.R;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.util.Log;

public class MainActivity extends Activity {
	private static final String TAG = "Matin Activity";
	private UtilDelegate utilDelegate;
	private List<String> imgList;
	private int          curImgIndex = 0;
	private ImageView  originImageView;
    //private ImageView  detectedImageView;
    private GridView   gridview;
    private Button 	   processBtn;
    private Button     nextBtn;
    private TextView   resultText;
    public static MainActivity mactivity;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mactivity=this;
		setContentView(R.layout.activity_main);
		utilDelegate = new UtilDelegate(this);
		imgList = new ArrayList<String>();
		File imageDir = this.getDir("bbalbum", Context.MODE_PRIVATE);
		FdActivity.copyAssetData(this, "testimages", imageDir.getAbsolutePath());   
		this.collectImg(imageDir + File.separator+ "testimages");
		
		File imageDir2 = this.getDir("Faces", Context.MODE_PRIVATE);
		FdActivity.copyAssetData(this, "Faces", imageDir2.getAbsolutePath());   
		
//		if (android.os.Environment.getExternalStorageState().equals(
//	               android.os.Environment.MEDIA_MOUNTED)) {
//				
////	           Toast.makeText(this, "sd card found",
////	                   Toast.LENGTH_LONG).show();
////	           File rootPath = Environment.getExternalStorageDirectory();
//			File rootPath = Environment.getExternalStorageDirectory();
//			
//	    	
//	    }else{
//	    	Toast.makeText(this, "Not sd card found",
//	                   Toast.LENGTH_SHORT).show();
//	           
//	    }
		 this.nextBtn   = (Button)findViewById(R.id.m_next_btn);
		 this.originImageView = (ImageView)findViewById(R.id.m_origin_image);
	     //this.detectedImageView = (ImageView)findViewById(R.id.m_detected_image);
	     gridview = (GridView) findViewById(R.id.face_gridview); 
	     this.originImageView.setImageDrawable(this.getResources().getDrawable(R.drawable.lena));
	     //this.detectedImageView.setImageDrawable(this.getResources().getDrawable(R.drawable.lena));
	     
	     this.resultText = (TextView)findViewById(R.id.resultText);
	     
	     this.nextBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					nextBtnClick();
					nextBtn.setClickable(false);
				}
	        	
	        });
	}
	private void collectImg(String path){
		String[] extensions = {"JPG","jpg","PNG","png","JPEG","jpeg"};
		boolean isIterative = false;
		File[] files =new File(path).listFiles();
		Log.i(TAG,path);
//		long startTime = System.nanoTime();
//		
//		File detectedFold = new File(new File(path).getAbsolutePath() + File.separator + "detected");
//		if(!detectedFold.exists() && !detectedFold.isDirectory())
//			detectedFold.mkdirs();
		
		for (int i =0; i < files.length; i++)
	    {
	        File f = files[i];
	        if (f.isFile())
	        {
	        	for(String extension:extensions){
		            if (f.getPath().substring(f.getPath().length() - extension.length()).equals(extension))
		            {
		            	this.imgList.add(f.getAbsolutePath());
		            	break;
		            }
	        	}
	        }
	        else if (f.isDirectory() && f.getPath().indexOf("/.") == -1 && isIterative) //??????????????????????????????/????????????
	        	collectImg(f.getPath());
	    }

//		long consumingTime = (System.nanoTime() - startTime)/1000000;
//		Toast.makeText(this, "Average Consuming time: "+ String.valueOf((consumingTime)/files.length) +"ms",
//                Toast.LENGTH_LONG).show();
//     
//		Log.i(TAG,"Total Consuming time: "+ String.valueOf(consumingTime) +"ms");
//    	Log.i(TAG,"Total Images        : "+String.valueOf(files.length)  );
//    	Log.i(TAG,"Average Consuming time: "+ String.valueOf((consumingTime)/files.length) +"ms");

	}
	private void nextBtnClick(){
		if( curImgIndex < imgList.size()  ){
			String imgPath = this.imgList.get(curImgIndex);
			this.resultText.setText("Processing...");
			new ProcessOp().execute(imgPath);
			this.showImg(imgPath);
			curImgIndex += 1;
			this.utilDelegate.testDetectTestFaces();
		}else{
			Toast.makeText(this, "Not more Images", Toast.LENGTH_LONG).show();
			this.curImgIndex = 0;
			nextBtn.setClickable(true);
			
		}
		
	}
	private static Bitmap big(Bitmap bitmap,float factor) {
		  Matrix matrix = new Matrix(); 
		  if (factor <= 0)
			  matrix.postScale(1.5f,1.5f); //长和宽放大缩小的比例
		  else{
			  matrix.postScale(factor, factor);
		  }
		  Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		  return resizeBmp;
	}
	private void showImg(String imgPath){
		Bitmap bm = big(BitmapFactory.decodeFile(imgPath),4.0f);
		
		this.originImageView.setImageBitmap(bm);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ProcessOp extends AsyncTask<String, Void, List<Mat> > {

    	
        @Override
        protected List<Mat> doInBackground(String... params) {
        	List<Mat> result = utilDelegate.checkBaby(params[0]);
        	
            return result;
        }
        
        public void showFaces(List<Mat> childFaces){
        	
			
			//detectedImageView.setImageBitmap(bmpOut);
			
        	 ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();  
             for(int i=0; i<childFaces.size(); i++)  
             {  
            	 Mat face = childFaces.get(i);
    			 Bitmap bmpOut = Bitmap.createBitmap(face.cols(), face.rows(), Bitmap.Config.ARGB_8888);
    			 Mat changed = new Mat(face.rows(), face.cols(), CvType.CV_8UC1);
    			 face.convertTo(changed, CvType.CV_8UC1,255);
    			 Utils.matToBitmap(changed, bmpOut);
    			 bmpOut = big(bmpOut,4.0f);
    				
            	 HashMap<String, Object> map = new HashMap<String, Object>();  
            	 map.put("ItemImage", bmpOut);//添加图像资源的ID  
            	 map.put("ItemText", "NO."+String.valueOf(i));//按序号做ItemText  
            	 lstImageItem.add(map);  
               
               	
             }  
             //生成适配器的ImageItem <====> 动态数组的元素，两者一一对应  
             SimpleAdapter saImageItems = new SimpleAdapter(MainActivity.mactivity, //没什么解释  
                                                       lstImageItem,//数据来源   
                                                       R.layout.night_item,//night_item的XML实现  
                                                         
                                                       //动态数组与ImageItem对应的子项          
                                                       new String[] {"ItemImage","ItemText"},   
                                                         
                                                       //ImageItem的XML文件里面的一个ImageView,两个TextView ID  
                                                       new int[] {R.id.ItemImage,R.id.ItemText});  
             //添加并且显示  
             gridview.setAdapter(saImageItems);  
             saImageItems.setViewBinder(new ViewBinder(){
            	  public boolean setViewValue(View view,Object data,String textRepresentation){
            	 if(view instanceof ImageView && data instanceof Bitmap){
            	      ImageView iv=(ImageView)view;
            	      
            	      Bitmap bm = (Bitmap) data;
                      iv.setImageBitmap(bm);
                      
                      return true;
                    }
                     else return false;
            	  }
              });
             //添加消息处理  
             //gridview.setOnItemClickListener(new ItemClickListener()); 
        }
		@Override
        protected void onPostExecute(List<Mat> childFaces) {
			if(childFaces.size() > 0){
				showFaces(childFaces);
				resultText.setText(childFaces.size() + "baby(s) found!");
				nextBtn.setClickable(true);
				
			}else {
				//detectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.lena));
				resultText.setText("No baby found!");	
				nextBtnClick();
			}

			
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
