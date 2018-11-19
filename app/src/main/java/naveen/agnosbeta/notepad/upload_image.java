package naveen.agnosbeta.notepad;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;

public class upload_image extends AppCompatActivity {
    private static final int SELECT_PHOTO = 100;
    Uri selectedImage;
    FirebaseStorage storage;
    StorageReference storageRef,imageRef;
    ProgressDialog progressDialog;
    UploadTask uploadTask;
    Uri downloadUrl;
    String File_name,description;
    static Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        //accessing the firebase storage
        storage = FirebaseStorage.getInstance();
        //creates a storage reference
        storageRef = storage.getReference();
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    uploadImage();
                }
        }
    }

    public void uploadImage() {
        //create reference to images folder and assing a name to the file that will be uploaded
        Dialog d=new Dialog(upload_image.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
        d.setTitle("Save To Firebase");
        d.setContentView(R.layout.input_dialog);

        final EditText nameEditTxt= (EditText) d.findViewById(R.id.nameEditText);
        final EditText description_EditTxt= (EditText) d.findViewById(R.id.description_EditText);
        Button saveBtn= (Button) d.findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEditTxt.getText().toString().length() == 0) {
                    nameEditTxt.setError(getString(R.string.error_field_required));
                }
                else if(description_EditTxt.getText().toString().length() == 0){
                    description_EditTxt.setError(getString(R.string.error_field_required));
                }
                else {
                    File_name=nameEditTxt.getText().toString()+".jpg";
                    description=description_EditTxt.getText().toString();
                    imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    //creating and showing progress dialog
                    progressDialog = new ProgressDialog(upload_image.this);
                    progressDialog.setMax(100);
                    progressDialog.setMessage("Uploading...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    //starting upload
                    uploadTask = imageRef.putFile(selectedImage);
                    // Observe state change events such as progress, pause, and resume
                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            //sets and increments value of progressbar
                            progressDialog.incrementProgressBy((int) progress);
                        }
                    });
                    // Register observers to listen for when the download is done or if it fails
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            //Toast.makeText(getApplication(),"Error in uploading!",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            downloadUrl = taskSnapshot.getDownloadUrl();
                            float s = taskSnapshot.getMetadata().getSizeBytes();
                            s = ((s / 1000) / 1048);
                            NumberFormat formatter = NumberFormat.getNumberInstance();
                            formatter.setMinimumFractionDigits(2);
                            formatter.setMaximumFractionDigits(2);
                            String size = formatter.format(s);
                            Upload upload=new Upload(File_name,downloadUrl.toString(),description,size+" mb");
                            FirebaseDatabase.getInstance().getReference().child("File").push().setValue(upload);
                            progressDialog.dismiss();
                            Intent intent = new Intent(upload_image.this, MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("key", "image");
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
            });
        d.show();
        }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(upload_image.this, MainActivity.class));
        finish();
    }
}