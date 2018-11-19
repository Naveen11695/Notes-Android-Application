package naveen.agnosbeta.notepad;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class image_viewer_activity extends AppCompatActivity {

    private int position;
    ProgressDialog progressDialog;
    ImageView imageView;
    TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.image_viewer_activity);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            position = bundle.getInt("item_postion");
        }
        findViewById(R.id.image_viwer_form).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findViewById(R.id.title).getVisibility()==View.VISIBLE) {
                    findViewById(R.id.title).setVisibility(View.GONE);
                    findViewById(R.id.download_image_button).setVisibility(View.GONE);
                }
                else {
                    findViewById(R.id.title).setVisibility(View.VISIBLE);
                    findViewById(R.id.download_image_button).setVisibility(View.VISIBLE);
                }
            }
        });

        imageView=findViewById(R.id.item_viewer);
        title=findViewById(R.id.title);
        title.setText(Splash_screen.uploads_name[position]);
        Picasso.get().load(Splash_screen.uploads_url[position]).into(imageView);
    }
    public void download_image(View view)
    {
        showProgressDialog(getString(R.string.app_name),"Downloading..");
        new DownloadFile().execute(Splash_screen.uploads_url[position], Splash_screen.uploads_name[position]);
        Log.d("Download complete","----------");
    }

    protected class DownloadFile extends AsyncTask<String, Void, Void> {


        @Override
        protected Void doInBackground(String... strings) {
            String fileUrl = strings[0];   // -> http://maven.apache.org/maven-1.x/maven.pdf
            String fileName = strings[1];  // -> maven.pdf
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File folder = new File(extStorageDirectory, "Doc");
            folder.mkdir();
            File pdfFile = new File(folder, fileName);

            try{
                pdfFile.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            FileDownloader.downloadFile(fileUrl, pdfFile);
            // Get instance of Vibrator from current Context
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            // Vibrate for 400 milliseconds
            v.vibrate(400);
            dismissProgressDialog();
            return null;
        }

    }




    protected void showProgressDialog(String title, String msg) {
        progressDialog = ProgressDialog.show(this, title, msg, true);
    }


    protected void dismissProgressDialog() {
        progressDialog.dismiss();
    }
}
