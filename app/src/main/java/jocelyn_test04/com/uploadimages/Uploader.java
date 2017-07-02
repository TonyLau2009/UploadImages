package jocelyn_test04.com.uploadimages;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by Jocelyn on 26/11/2016.
 */

public class Uploader extends AsyncTask<Void, Void, MyCommand>{

    private Context context;
    private String url;
    private ArrayList<ImageView> imageViewList;

    private ProgressDialog pd;

    public Uploader(Context context, ArrayList<ImageView> imageViewList, String url) {
        this.context = context;
        this.url = url;
        this.imageViewList = imageViewList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(context);
        pd.setTitle("Upload Image");
        pd.setMessage("Encoding Images....Please wait");
        pd.show();
    }

    @Override
    protected MyCommand doInBackground(Void... voids) {
        return this.encodeImages();
    }

    @Override
    protected void onPostExecute(MyCommand mCom) {
        super.onPostExecute(mCom);

        pd.dismiss();

        mCom.execute();
    }

    private MyCommand encodeImages(){
        MyCommand mCommand = new MyCommand(context);
        for(ImageView imageView : imageViewList){
            ByteArrayOutputStream baos = null;
            Bitmap mBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

            try {
                baos = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                baos.flush();

                byte[] array = baos.toByteArray();
                final String encoded_String = Base64.encodeToString(array, Base64.DEFAULT);

                StringRequest str_request = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(context,
                                        "Upload Successfull..!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> map = new HashMap<>();
                        map.put("encoded_str", encoded_String);
                        map.put("img_name", "_Img.jpg");
                        return map;
                    }
                };

                mCommand.add(str_request);

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(baos != null){
                    try {
                        baos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mCommand;
    }
}
