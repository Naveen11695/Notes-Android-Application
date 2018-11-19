package naveen.agnosbeta.notepad;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VersionedPackage;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;

/**
 * Created by naVeen on 30-09-2017.
 */
public class Splash_screen extends AppCompatActivity{
    private ProgressBar mProgressBar;
    private int mProgressStatus = 0;
    private PackageManager packageManager;
    Animation animation;
    TextView title;
    protected static DatabaseReference mDatabaseReference;
    protected   static String[] uploads_name=null;
    protected   static String[] uploads_url=null;
    protected   static String[] uploads_desc=null;
    protected   static String[] uploads_size=null;
    protected static List<Upload> uploadList=null;

    private Handler mHandler = new Handler();

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.splash_screen);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        title=(TextView)findViewById(R.id.title);

        if(isNetworkAvailable()) {

            //Download.download_data();

            //Log.e( "Download_class","Item: "+" is Dragged.");

            animation = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_up);
            title.startAnimation(animation);
            mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
            uploadList = new ArrayList<>();
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("File");
            //retrieving upload data from firebase database
            mDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    uploadList.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Upload upload = postSnapshot.getValue(Upload.class);
                        uploadList.add(upload);
                    }

                    uploads_name = new String[uploadList.size()];
                    uploads_url = new String[uploadList.size()];
                    uploads_desc = new String[uploadList.size()];
                    uploads_size = new String[uploadList.size()];
                    for (int i = 0; i < uploads_name.length; i++) {
                        uploads_name[i] = uploadList.get(i).getName();
                        uploads_url[i] = uploadList.get(i).getUrl();
                        uploads_desc[i] =uploadList.get(i).getDesc();
                        uploads_size[i] =uploadList.get(i).getSize();
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (mProgressStatus < 100){
                                mProgressStatus++;
                                android.os.SystemClock.sleep(50);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(mProgressStatus);

                                    }
                                });
                            }
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(isNetworkAvailable()) {
                                        onSplash();
                                    }

                                    else {
                                        Toast.makeText(getApplicationContext(),"Network Unavailable!!",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            });
                        }
                    }).start();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                        finish();
                }
            });
             }
        else
        {
            View snackbarView = getWindow().getDecorView().getRootView();
            Snackbar.make(snackbarView, "Network Unavailable !", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(getApplicationContext(),Splash_screen.class));
                           finish();
                        }
                    })
                    .show();

        }

    }

    public void onSplash()
    {
        //startActivity(new Intent(getApplicationContext(), Download_firebase.class));
       // finish();
        Intent intent=new Intent(Splash_screen.this,MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("key", "download_class");
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }


    // Private class isNetworkAvailable
    private boolean isNetworkAvailable() {
        // Using ConnectivityManager to check for Network Connection
        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }


}


