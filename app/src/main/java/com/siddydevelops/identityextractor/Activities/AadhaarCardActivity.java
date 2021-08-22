package com.siddydevelops.identityextractor.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.siddydevelops.identityextractor.PostProcessing.AadhaarProcessing;
import com.siddydevelops.identityextractor.R;
import com.siddydevelops.identityextractor.Utils.CameraUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class AadhaarCardActivity extends AppCompatActivity {

    private ImageView frontImage;
    private ImageView backImage;
    String mCurrentPhotoPath, mCurrentPhotoPath2;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_TAKE_PHOTO_2 = 2;
    private Bitmap mImageBitmap, mImageBitmap2;
    private EditText name, yearOfBirth, gender, aahaarNo, pincode, addressLine1, addressLine2;
    private Button reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aadhaar_card);

        frontImage = findViewById(R.id.imageView);
        backImage = findViewById(R.id.imageView2);
        name = findViewById(R.id.name_editText);
        gender = findViewById(R.id.gender_editText);
        aahaarNo = findViewById(R.id.aadhaar_editText);
        pincode = findViewById(R.id.pincode_editText);
        yearOfBirth = findViewById(R.id.yob_editText);
        addressLine1 = findViewById(R.id.address_line1_editText);
        addressLine2 = findViewById(R.id.address_line2_editText);

        reset = findViewById(R.id.reset);

        reset.setText("RESET");

    }

    public void takePicture(View view) {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;
            try {
                photoFile = CameraUtils.createImageFile(this);
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            }catch(IOException e)
            {
                Toast.makeText(this, "Error Creating File.", Toast.LENGTH_SHORT).show();
            }

            if(photoFile != null)
            {
                Uri photoURL = FileProvider.getUriForFile(this,"com.siddydevelops.identityextractor.fileprovider", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURL);
                startActivityForResult(takePic, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void takeBackPicture(View view) {

        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager()) != null)
        {
            File photoFile = null;
            try {
                photoFile = CameraUtils.createImageFile(this);
                mCurrentPhotoPath2 = photoFile.getAbsolutePath();
            }catch(IOException e)
            {
                Toast.makeText(this, "Error Creating File.", Toast.LENGTH_SHORT).show();
            }

            if(photoFile != null)
            {
                Uri photoURL = FileProvider.getUriForFile(this,"com.siddydevelops.identityextractor.fileprovider", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURL);
                startActivityForResult(takePic, REQUEST_TAKE_PHOTO_2);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            mImageBitmap = CameraUtils.getBitmap(mCurrentPhotoPath);
            frontImage.setImageBitmap(mImageBitmap);
            reset.setVisibility(View.VISIBLE);
        }
        else if(requestCode == REQUEST_TAKE_PHOTO_2 && resultCode == RESULT_OK)
        {
            mImageBitmap2 = CameraUtils.getBitmap(mCurrentPhotoPath2);
            backImage.setImageBitmap(mImageBitmap2);
            reset.setVisibility(View.VISIBLE);
        }


    }

    public void extractInfo(View view) {

        if(mImageBitmap != null && mImageBitmap2 != null)
        {
            FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mImageBitmap);
            detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    HashMap<String, String> dataMap  = new AadhaarProcessing()
                            .processExtractTextForFrontPic(firebaseVisionText, getApplicationContext());
                    presentFrontOutput(dataMap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


            FirebaseVisionImage image2 = FirebaseVisionImage.fromBitmap(mImageBitmap2);
            detector.detectInImage(image2).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    HashMap<String, String> dataMap  = new AadhaarProcessing()
                            .processExtractedForBackPic(firebaseVisionText, getApplicationContext());
                    presentBackOutput(dataMap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }
        else
        {
            Toast.makeText(this, "Please take both front and back image of Aadhaar Card", Toast.LENGTH_SHORT).show();
        }

    }

    public void reset(View view) {
        mImageBitmap = null;
        mImageBitmap2 = null;
        backImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera_back));
        frontImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera_front));
        name.getText().clear();
        yearOfBirth.getText().clear();
        gender.getText().clear();
        pincode.getText().clear();
        aahaarNo.getText().clear();
        addressLine1.getText().clear();
        addressLine2.getText().clear();
        reset.setVisibility(View.GONE);
    }

    private void presentFrontOutput(HashMap<String, String> dataMap)
    {
        if(dataMap != null)
        {
            aahaarNo.setText(dataMap.get("aadhaar"), TextView.BufferType.EDITABLE);
            gender.setText(dataMap.get("gender"), TextView.BufferType.EDITABLE);
            yearOfBirth.setText(dataMap.get("yob"), TextView.BufferType.EDITABLE);
            name.setText(dataMap.get("name"), TextView.BufferType.EDITABLE);
        }
    }

    private void presentBackOutput(HashMap<String, String> dataMap)
    {
        if(dataMap != null)
        {
            addressLine1.setText(dataMap.get("addressLine1"), TextView.BufferType.EDITABLE);
            addressLine2.setText(new StringBuilder().append(dataMap.get("addressLine2")).append(" ").append(dataMap.get("addressLine3")).append(" ").append(dataMap.get("addressLine4")).toString(), TextView.BufferType.EDITABLE);
            //Editable pin_code = addressLine2.getText().toString().substring(addressLine2.toString().lastIndexOf(" ") + 1);
            //String pin_code = dataMap.get("addressLine4").substring(dataMap.get("addressLine4").lastIndexOf(" ")+1);
            //Log.i("Something",pin_code);
            //pincode.toString().substring(pin_code.toString().lastIndexOf(" ") + 1);
            pincode.setText(dataMap.get("pincode"), TextView.BufferType.EDITABLE);
        }
    }

}