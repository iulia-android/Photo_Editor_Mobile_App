package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.graphics.Color.GREEN;

public class MainActivity extends AppCompatActivity
        implements ContrastBrightnessFragment.BottomSheetListener , HueSaturationFragment.BottomSheetListenerHS{

    private static final int PICK_IMAGE_REQUEST = 1;
    private OutputStream outputStream;

    private static String TAG = "MainActivity";
    private Button save;
    private Button open;
    private ImageView image;
    private Uri imageUri;
    private ImageButton constrBright;
    private ImageButton hueSat;
    private Bitmap bitmap_original;

    static
    {
        if (OpenCVLoader.initDebug())
        {
            Log.d(TAG, "Succes");
        }
        else
        {
            Log.d(TAG, "Not working");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        save = findViewById(R.id.save_image);
        open = findViewById(R.id.open_image);
        image = findViewById(R.id.image_view);

        bitmap_original = ((BitmapDrawable)image.getDrawable()).getBitmap();

        constrBright = findViewById(R.id.button_filter_cb);

        hueSat = findViewById(R.id.button_filter_hs);

        setListeners();
    }

    private void setListeners() {

        findViewById(R.id.open_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        findViewById(R.id.save_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInStorage();
            }
        });

        constrBright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brightnessContrast();
            }
        });

        hueSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hueSaturation();
            }
        });

        findViewById(R.id.rotate_left_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateLeft();
            }
        });

        findViewById(R.id.rotate_right_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateRight();
            }
        });

        findViewById(R.id.blue_filter_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blue_filter();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purple_filter();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                green_filter();
            }
        });

        findViewById(R.id.button_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                red_filter();
            }
        });

        findViewById(R.id.button_blur).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blurring_image();
            }
        });

        findViewById(R.id.button_to_gray).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to_gray();
            }
        });

    }

    private void to_gray() {
        Bitmap imageBitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat rgba = new Mat();
        Utils.bitmapToMat(imageBitmap,rgba);

        Mat resized = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8UC3);
        Size newSize = new Size(imageBitmap.getWidth(),imageBitmap.getHeight());
        Imgproc.resize(rgba,resized,newSize,0,0,Imgproc.INTER_AREA);

        Mat dst = new Mat(resized.rows(),resized.cols(), CvType.CV_8UC3);

        for(int i= 0; i < resized.rows(); i++){
            for(int j = 0; j < resized.cols(); j++){
                int auxR = 0;
                int auxG = 0;
                int auxB = 0;

                double[] rgb = resized.get(i, j);
                auxR = (int)((rgb[0] + rgb[1] + rgb[2])/3);
                auxG = (int)((rgb[0] + rgb[1] + rgb[2])/3);
                auxB = (int)((rgb[0] + rgb[1] + rgb[2])/3);

                double[] new_rgb = {auxR, auxG, auxB};
                dst.put(i, j, new_rgb);
            }
        }

        Bitmap result = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst,result);
        image.setImageBitmap(result);
    }

    private void blurring_image() {
        Bitmap imageBitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat rgba = new Mat();
        Utils.bitmapToMat(imageBitmap,rgba);

        Mat resized = new Mat(imageBitmap.getHeight(), imageBitmap.getWidth(), CvType.CV_8UC3);
        Size newSize = new Size(imageBitmap.getWidth(),imageBitmap.getHeight());
        Imgproc.resize(rgba,resized,newSize,0,0,Imgproc.INTER_AREA);

        Mat dst = new Mat(resized.rows(),resized.cols(), CvType.CV_8UC3);
        int w =7;
        float sigma = (float)w/6;
        int k = w/2;
        double[][] G = new double[7][7];

        for(int x = 0; x < w; x++){
            for(int y = 0; y < w; y++){
                G[x][y] = 1.0/(2 * Math.PI * sigma * sigma) * Math.exp(-(((x - k)*(x - k) + (y - k)*(y - k)) / (2 * sigma*sigma)));
            }
        }

        for(int x=k; x<resized.rows()-k;x++){
            for(int y = k; y<resized.cols()-k;y++){
                int auxR = 0;
                int auxG = 0;
                int auxB = 0;
                for(int i = -k; i<=k; i++){
                    for(int j=-k; j<=k;j++){
                        double[] rgb = resized.get(x+i,y+j);
                        auxR+=rgb[0]*G[i+k][j+k];
                        auxG+=rgb[1]*G[i+k][j+k];
                        auxB+=rgb[2]*G[i+k][j+k];
                    }
                }
                double[] new_rgb = {auxR, auxG, auxB};
                dst.put(x,y,new_rgb);
            }
        }

        Bitmap newBitmap = Bitmap.createBitmap(dst.cols(),dst.rows(), imageBitmap.getConfig());
        Utils.matToBitmap(dst,newBitmap);
        image.setImageBitmap(newBitmap);
    }

    private void red_filter() {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);

        Mat dst = src.clone();
        Mat color = src.clone();
        color.setTo(new Scalar(255, 0, 0));

        Core.addWeighted(src, 0.8, color, 0.2, 1,  dst);

        Bitmap result = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst,result);
        image.setImageBitmap(result);
    }

    private void green_filter() {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);

        Mat dst = src.clone();
        Mat color = src.clone();
        color.setTo(new Scalar(77, 247, 224));

        Core.addWeighted(src, 0.8, color, 0.2, 1,  dst);

        Bitmap result = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst,result);
        image.setImageBitmap(result);
    }

    private void purple_filter() {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);

        Mat dst = src.clone();
        Mat color = src.clone();
        color.setTo(new Scalar(125, 77, 247));

        Core.addWeighted(src, 0.8, color, 0.2, 1,  dst);

        Bitmap result = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst,result);
        image.setImageBitmap(result);
    }

    private void blue_filter() {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);

        Mat dst = src.clone();
        Mat color = src.clone();
        color.setTo(new Scalar(0, 229, 255));

        Core.addWeighted(src, 0.8, color, 0.2, 1,  dst);

        Bitmap result = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst,result);
        image.setImageBitmap(result);
    }

    private void rotateRight() {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);

        Mat dst = src.clone();
        for(int i = 0; i < src.rows(); i++) {
            for(int j = 0; j < src.cols(); j++) {
                dst.put(j, dst.cols() - i - 1, src.get(i,j));
            }
        }

        Bitmap result = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst,result);
        image.setImageBitmap(result);
    }

    private void rotateLeft() {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);

        Mat dst = src.clone();
        for(int i = 0; i < src.rows(); i++) {
            for(int j = 0; j < src.cols(); j++) {
                dst.put(dst.rows() - j - 1, i, src.get(i,j));
            }
        }

        Bitmap result = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst,result);
        image.setImageBitmap(result);
    }

    private void hueSaturation() {
        HueSaturationFragment hueSaturationFragment = new HueSaturationFragment();
        hueSaturationFragment.show(getSupportFragmentManager(), "filter2");
    }

    private void brightnessContrast() {
        ContrastBrightnessFragment contrastBrightnessFragment = new ContrastBrightnessFragment();
        contrastBrightnessFragment.show(getSupportFragmentManager(), "filter1");
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            imageUri = data.getData();

            Picasso.get().load(imageUri).into(image);
        }
    }

    private void saveInStorage() {
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        Bitmap bitmap1 = drawable.getBitmap();
        String s = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap1,
                String.valueOf(System.currentTimeMillis()) + "title",
                String.valueOf(System.currentTimeMillis()) + "desc");
    }

    @Override
    public void brightnessSeekBar(int value) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);
        if(value > 50) {
            src.convertTo(src, -1, 1, 1);
        }
        else {
            src.convertTo(src, -1, 1, (50 - value)* (-1));
        }
        Bitmap result = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,result);
        image.setImageBitmap(result);
    }

    @Override
    public void contrastSeekBar(int value) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap,src);
        if(value > 50) {
            src.convertTo(src, -1, (value - 50) - 0.5, 0); // x * alpha + beta
        }
        else {
            src.convertTo(src, -1, 0.5 - (50 - value)/100, 0);
        }
        Bitmap result = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,result);
        image.setImageBitmap(result);
    }

    @Override
    public void hueSeekBar(int value) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_16UC4);

        Utils.bitmapToMat(bitmap,src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2HSV, 3);

        for (int j = 0; j < src.rows(); j++)
        {
            for (int i = 0; i < src.cols(); i++)
            {
                // Get hue.
                // Saturation is hsv.at<Vec3b>(j, i)[1], and
                // Value is hsv.at<Vec3b>(j, i)[2].
                int h = (int) src.get(j,i)[0];

                if ( (h +  (value-50)) < 0 )
                    h = 0;
                else if ( (h + (value-50)) > 255 )
                    h = 255;
                else
                    h = h + (value-50);

                // Set hue.
                src.get(j,i)[0] = h;
            }
        }

        Imgproc.cvtColor(src, src, Imgproc.COLOR_HSV2RGB_FULL, 4);

        Bitmap result = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,result);
        image.setImageBitmap(result);
    }

    @Override
    public void saturationSeekBar(int value) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Mat src = new Mat(image.getHeight(),image.getWidth(), CvType.CV_16UC4);

        Utils.bitmapToMat(bitmap,src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2HSV, 3);

        for (int j = 0; j < src.rows(); j++)
        {
            for (int i = 0; i < src.cols(); i++)
            {
                // Get hue.
                // Saturation is hsv.at<Vec3b>(j, i)[1], and
                // Value is hsv.at<Vec3b>(j, i)[2].
                int h = (int) src.get(j,i)[1];

                if ( (h + (value-50)) < 0 )
                    h = 0;
                else if ( (h + (value-50)) > 255 )
                    h = 255;
                else
                    h = h + (value-50);

                // Set hue.
                src.get(j,i)[1] = h;
            }
        }

        Imgproc.cvtColor(src, src, Imgproc.COLOR_HSV2RGB_FULL, 4);

        Bitmap result = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src,result);
        image.setImageBitmap(result);
    }
}