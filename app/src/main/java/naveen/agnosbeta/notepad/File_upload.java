package naveen.agnosbeta.notepad;

import android.*;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.NumberFormat;

public class File_upload extends AppCompatActivity {

    final static int PICK_PDF_CODE = 2342;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    StorageReference storageRef,imageRef;
    UploadTask uploadTask;
    String File_name,description;
    Uri downloadUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        //accessing the firebase storage
        storage = FirebaseStorage.getInstance();
        //creates a storage reference
        storageRef = storage.getReference();
        getPDF();
    }

    //this function will get the pdf from the storage
    private void getPDF() {
        //creating an intent for file chooser
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_PDF_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //when the user choses the file
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //if a file is selected
            if (data.getData() != null) {
                //uploading the file
                //Toast.makeText(this, "File: "+data.getData(), Toast.LENGTH_SHORT).show();
                uploadFile(data.getData());
            }else{
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void uploadFile(final Uri data) {
        //create reference to images folder and assing a name to the file that will be uploaded
        Dialog d=new Dialog(File_upload.this,R.style.Theme_AppCompat_Light_Dialog_Alert);
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
                    description=description_EditTxt.getText().toString();
                    if (data.toString().contains(".pdf")) {
                        File_name=nameEditTxt.getText().toString()+".pdf";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".docx")) {
                        File_name=nameEditTxt.getText().toString()+".docx";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".doc")) {
                        File_name=nameEditTxt.getText().toString()+".doc";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".xlsx")) {
                        File_name=nameEditTxt.getText().toString()+".xlsx";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".xls")) {
                        File_name=nameEditTxt.getText().toString()+".xls";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".ppt")) {
                        File_name=nameEditTxt.getText().toString()+".ppt";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".pptx")) {
                        File_name=nameEditTxt.getText().toString()+".pptx";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".rar")) {
                        File_name=nameEditTxt.getText().toString()+".rar";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".zip")) {
                        File_name=nameEditTxt.getText().toString()+".zip";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else if(data.toString().contains(".txt")) {
                        File_name=nameEditTxt.getText().toString()+".txt";
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    else {
                        File_name=nameEditTxt.getText().toString();
                        imageRef = storage.getReferenceFromUrl("gs://notepad-d9538.appspot.com").child(File_name);
                    }
                    //creating and showing progress dialog
                    progressDialog = new ProgressDialog(File_upload.this);
                    progressDialog.setMax(100);
                    progressDialog.setMessage("Uploading...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    //starting upload
                    uploadTask = imageRef.putFile(data);
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
                            //taskSnapshot.getMetadata(); contains file metadata such as size, content-type, and download URL.
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
                            Intent intent = new Intent(File_upload.this, MainActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("key", "File");
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
        startActivity(new Intent(File_upload.this, MainActivity.class));
        finish();
    }
}
