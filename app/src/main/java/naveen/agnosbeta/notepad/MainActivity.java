package naveen.agnosbeta.notepad;

import android.*;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewStub stubGrid;
    private GridView gridView;
    private ViewStub stubList;
    private ListView listView;
    ProgressDialog progressDialog;
    FirebaseStorage storage;
    private ListViewAdapter listViewAdapter;
    private GridViewAdapter gridViewAdapter;
    private List<Product> productList;
    private int currentViewMode = 0;
    private ImageView delete_bin;
    private final int CAMERA_PERMISSION_CODE=2;
    private final int REQUEST_APP_SETTINGS=3;
    private Bitmap bmp;

    static final int VIEW_MODE_GRIDVIEW = 1;
    static final int VIEW_MODE_LISTVIEW = 0;


    private String action;


    protected static Uri photoURI;
    protected static final int REQUEST_TAKE_PHOTO = 1;
    protected static String mCurrentPhotoPath;

    private Animator mCurrentAnimator;

    /**
     * The system "short" animation time duration, in milliseconds. This duration is ideal for
     * subtle animations or animations that occur very frequently.
     */
    private int mShortAnimationDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_actitity);


        stubGrid = (ViewStub) findViewById(R.id.stub_grid);
        stubList = (ViewStub) findViewById(R.id.stub_list);

        storage = FirebaseStorage.getInstance();
        //Inflate ViewStub before get view

        //creates a storage reference
        stubGrid.inflate();
        stubList.inflate();

        final Animation animation;
        gridView = (GridView) findViewById(R.id.mygridview);
        listView = (ListView) findViewById(R.id.mylistview);



        delete_bin=(ImageView) findViewById(R.id.delete_bin);
        delete_bin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Drag to delete Item.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.list_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_change();
            }
        });

        findViewById(R.id.grid_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_change();
            }
        });


        final ImageButton menu_Button=(ImageButton)findViewById(R.id.menu_icon);
        menu_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.support.v7.widget.PopupMenu p = new android.support.v7.widget.PopupMenu(MainActivity.this,menu_Button);
                Menu m = p.getMenu();
                getMenuInflater().inflate(R.menu.main, m);
                p.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener(){

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.item_menu_1:
                            {
                                break;
                            }
                            case R.id.item_menu_2:
                            {
                                finish();
                            }
                        }
                        return true;
                    }

                });
                p.show();
            }
        });

        listView.setOnItemClickListener(onItemClick);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });

        gridView.setOnItemClickListener(onItemClick);
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                findViewById(R.id.delete_bin).setVisibility(View.VISIBLE);
                ClipData data=ClipData.newPlainText("","");
                View.DragShadowBuilder shadowBuilder=new View.DragShadowBuilder(view);
                view.startDragAndDrop(data,shadowBuilder,view,0);
                final int item_id= (int) id;
                delete_bin.setOnDragListener(new View.OnDragListener() {
                    @Override
                    public boolean onDrag(View v, DragEvent event) {
                        int dragEvent= event.getAction();
                        switch (dragEvent) {
                            case DragEvent.ACTION_DRAG_ENTERED:
                                break;
                            case DragEvent.ACTION_DRAG_EXITED:
                                break;
                            case DragEvent.ACTION_DROP:
                                Intent intent=new Intent(MainActivity.this,Delete_item_activity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("item", Splash_screen.uploads_name[item_id]);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                                break;
                        }
                        return true;
                    }
                });
                return true;
            }
        });


        //............................................ FLOATING_ACTION_BUTTON...................................................

        final ImageView fab =findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(findViewById(R.id.image_button).getVisibility()==View.VISIBLE) {
                    findViewById(R.id.camera_button).setVisibility(View.GONE);
                    findViewById(R.id.image_button).setVisibility(View.GONE);
                    findViewById(R.id.file_button).setVisibility(View.GONE);

                }
                else {
                    findViewById(R.id.camera_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.image_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.file_button).setVisibility(View.VISIBLE);

                }
            }
        });

        //............................................CAMERA_BUTTON...................................................

        final FloatingActionButton camera_BUTTON=findViewById(R.id.camera_button);
        camera_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action="camera_action";
                permission();
            }
        });

        //............................................IMAGE_BUTTON...................................................

        final FloatingActionButton imageView=findViewById(R.id.image_button);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action="image_action";
                permission();
            }
        });

        //............................................FILE_BUTTON...................................................

        final FloatingActionButton file_button=findViewById(R.id.file_button);
        file_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action="file_action";
                permission();
            }
        });




        //get list of product
        getProductList();


        //Get current view mode in share reference
        SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
        currentViewMode = sharedPreferences.getInt("currentViewMode", VIEW_MODE_GRIDVIEW);//Default is view listview

        switchView();

        /*gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item, productList);
        gridView.setAdapter(gridViewAdapter);*/

    }

    private void view_change() {
        if(VIEW_MODE_LISTVIEW == currentViewMode) {
            currentViewMode = VIEW_MODE_GRIDVIEW;
        } else {
            currentViewMode = VIEW_MODE_LISTVIEW;
        }
        //Switch view
        switchView();
        //Save view mode in share reference
        SharedPreferences sharedPreferences = getSharedPreferences("ViewMode", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentViewMode", currentViewMode);
        editor.apply();
    }

    private void switchView() {

        if(VIEW_MODE_LISTVIEW == currentViewMode) {
            //Display listview
            stubList.setVisibility(View.VISIBLE);
            findViewById(R.id.list_icon).setVisibility(View.GONE);
            //Hide gridview
            stubGrid.setVisibility(View.GONE);
            findViewById(R.id.grid_icon).setVisibility(View.VISIBLE);
        } else {
            //Hide listview
            stubList.setVisibility(View.GONE);
            findViewById(R.id.list_icon).setVisibility(View.VISIBLE);
            //Display gridview
            stubGrid.setVisibility(View.VISIBLE);
            findViewById(R.id.grid_icon).setVisibility(View.GONE);
        }
        setAdapters();
    }

    private void setAdapters() {
        if(VIEW_MODE_LISTVIEW == currentViewMode) {
            listViewAdapter = new ListViewAdapter(this, R.layout.list_item, productList);
            listView.setAdapter(listViewAdapter);
        } else {

            gridViewAdapter = new GridViewAdapter(this, R.layout.grid_item, productList);
            gridView.setAdapter(gridViewAdapter);
        }
    }





    public List<Product> getProductList() {
        //pseudo code to get product, replace your code to get real product here
        productList = new ArrayList<>();

            Log.e("Tag", String.valueOf(Splash_screen.uploads_name.length));


        for (int i = 0; i < Splash_screen.uploads_name.length; i++) {
            if(Splash_screen.uploads_name[i].contains(".pdf"))
            {
                productList.add(new Product(R.drawable.pdf_icon, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".rar"))
            {
                productList.add(new Product(R.drawable.rar_icon, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".zip"))
            {
                productList.add(new Product(R.drawable.zip, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".jpg"))
            {
                productList.add(new Product(R.drawable.jpg_icon, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".ppt"))
            {
                productList.add(new Product(R.drawable.ppt, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".pptx"))
            {
                productList.add(new Product(R.drawable.ppt, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".doc") ||Splash_screen.uploads_name[i].contains(".docx"))
            {
                productList.add(new Product(R.drawable.doc_icon, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".xls"))
            {
                productList.add(new Product(R.drawable.xlsx, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".xlsx"))
            {
                productList.add(new Product(R.drawable.xlsx, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else if(Splash_screen.uploads_name[i].contains(".txt"))
            {
                productList.add(new Product(R.drawable.txt_icon, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
            else
            {
                productList.add(new Product(R.drawable.unknown_icon, Splash_screen.uploads_name[i], "Description : "+Splash_screen.uploads_desc[i],"Size : "+Splash_screen.uploads_size[i]));
            }
        }

        return productList;
    }

    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Do any thing when user click to item
            if(Splash_screen.uploads_name[position].contains(".jpg"))
            {
                view_image(position);
            }
            else
            {
                Intent intent=new Intent(MainActivity.this,doc_viewer.class);
                Bundle bundle = new Bundle();
                bundle.putInt("item_postion",position);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    };




    private void view_image(final int position) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        final View view = factory.inflate(R.layout.image_viwer, null);
        ImageView image = (ImageView) view.findViewById(R.id.DialogImage);

        view.findViewById(R.id.dialog_Image_loading).setVisibility(View.VISIBLE);
        Picasso.get().load(Splash_screen.uploads_url[position]).into(image);
        alertadd.setView(view);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),image_viewer_activity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("item_postion",position);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
        alertadd.show();
    }


    protected void permission() {
         if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                 && ContextCompat.checkSelfPermission(MainActivity.this,
                 Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                 && ContextCompat.checkSelfPermission(MainActivity.this,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            if(action.equalsIgnoreCase("camera_action")) {
                onCamera_Action();
            }
            else if(action.equalsIgnoreCase("image_action")) {
                onImage_Action();
            }
            else if(action.equalsIgnoreCase("file_action")) {
                onFile_Action();
            }

        }
        else {
             Toast.makeText(getApplicationContext(), "PLEASE ENABLE THE ALL PERMISSIONS !", Toast.LENGTH_SHORT).show();
             goToSettings();
        }
    }

    //...........................................Camera_action..............................................//


    private void onCamera_Action() {
        startActivity(new Intent(getApplicationContext(),upload_image_camera.class));
    }
    private void onImage_Action() {
        startActivity(new Intent(getApplicationContext(),upload_image.class));
    }
    private void onFile_Action() {
        startActivity(new Intent(getApplicationContext(),File_upload.class));
    }
        //......................................Camera_Action_End.........................................................//
    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }

    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                return false;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_APP_SETTINGS) {
            if (hasPermissions(android.Manifest.permission.CAMERA) && hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION) && hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                onCamera_Action();
            }
            else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack (true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Boolean flag=true;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String value = bundle.getString("key");
            if (value.equalsIgnoreCase("download_class")&&flag) {
                flag=false;
                onRestart();
            }
        }
    }
}
