package com.codies.allinonepakistanadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class NewStoreActivity extends AppCompatActivity {
    public static final int IMAGECHOOSERCODE = 1;
    public static final String TAG = "MAINACTIVITY";

    AppCompatSpinner firstSpinner;
    String selectedCategory = "";
    String[] array = {"Top Stores", "Fashion", "Grocery", "Recharge", "Baby & Kids","Medicine", "Food", "Movie", "Furniture", "Gifts", "Home Services"};

    EditText nameET;
    EditText urlET;
    ImageView storeIMG;
    FloatingActionButton uploadImgBT;
    Button saveStoreBT;

    Uri imageUri;
    String fileExtension;
    String imageUrl="";
    boolean imageUploading = false;
    boolean photoSelected = false;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        storageReference = FirebaseStorage.getInstance().getReference("StoreImages");
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        firstSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = array[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        saveStoreBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameET.getText().toString();
                String storeURL = urlET.getText().toString();
                Store store = new Store(name, storeURL, selectedCategory, imageUrl);
                if (name.isEmpty()) {
                    Toast.makeText(NewStoreActivity.this, "Name Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (storeURL.isEmpty()) {
                    Toast.makeText(NewStoreActivity.this, "Store URL Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedCategory.isEmpty()) {
                    Toast.makeText(NewStoreActivity.this, "Selected Category Empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (imageUrl.isEmpty()) {
                    Toast.makeText(NewStoreActivity.this, "Image Not Uploaded Yet", Toast.LENGTH_SHORT).show();
                    return;
                }
                databaseReference.child(selectedCategory).child(name).setValue(store).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NewStoreActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewStoreActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        uploadImgBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    openImageChooser();
                } else {
                    isStoragePermissionGranted();
                }
            }
        });

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
//                openImageChooser();
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
//            openImageChooser();
            return true;
        }
    }

    public void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGECHOOSERCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGECHOOSERCODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            photoSelected = true;
            Glide.with(this).load(imageUri).into(storeIMG);
            fileExtension = getFileExtension(imageUri);
            uploadFile(imageUri, fileExtension);
        }
    }

    public String getFileExtension(Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
    }

    public void uploadFile(Uri imageUri, String extension) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Image Uploading..");
        progressDialog.show();
        imageUploading = true;
        final StorageReference storageReference = this.storageReference.child(System.currentTimeMillis() + "." + extension);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressDialog.dismiss();
                        imageUrl = uri.toString();
                        imageUploading = false;
                        Toast.makeText(NewStoreActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        imageUploading = false;
                        progressDialog.dismiss();
                        Toast.makeText(NewStoreActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //tell user that uploading failed
                imageUploading = false;
                progressDialog.dismiss();
                Toast.makeText(NewStoreActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //keep the user updated about this task
                double val = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    imgProgressBar.setProgress((int) val, true);
                } else {
//                    imgProgressBar.setProgress((int) val);
                }
            }
        });
    }

    void initViews() {
        nameET = findViewById(R.id.storeNameET);
        urlET = findViewById(R.id.storeUrlET);
        storeIMG = findViewById(R.id.storeIMG);
        uploadImgBT = findViewById(R.id.imagePicker);
        saveStoreBT = findViewById(R.id.saveBT);

        firstSpinner = findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, array);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_background_text);
        firstSpinner.setAdapter(spinnerArrayAdapter);
    }
}