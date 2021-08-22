package com.siddydevelops.identityextractor.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.siddydevelops.identityextractor.PostProcessing.PanProcessing;
import com.siddydevelops.identityextractor.R;
import com.siddydevelops.identityextractor.Utils.CameraUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class PanCardActivity extends AppCompatActivity {

    private Button ocr;
    private ImageView imageView;
    private Bitmap mImageBitmap;
    private Button reset;
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    private EditText name, dateOfBirth, fatherName, panNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pan_card);

        ocr = findViewById(R.id.ocr);
        imageView = findViewById(R.id.imageView);
        reset = findViewById(R.id.reset);

        name = findViewById(R.id.name_editText);
        dateOfBirth = findViewById(R.id.yob_editText);
        fatherName = findViewById(R.id.father_editText);
        panNumber = findViewById(R.id.pan_editText);

    }

    public void takePanPicture(View view) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK)
        {
            mImageBitmap = CameraUtils.getBitmap(mCurrentPhotoPath);
            imageView.setImageBitmap(mImageBitmap);
            reset.setVisibility(View.VISIBLE);
        }

    }

    public void reset(View view) {

        mImageBitmap = null;
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera_back));
        name.getText().clear();
        dateOfBirth.getText().clear();
        fatherName.getText().clear();
        panNumber.getText().clear();
        reset.setVisibility(View.GONE);

    }

    private void presentOutput(HashMap<String, String> dataMap)
    {
        if(dataMap != null)
        {
            panNumber.setText(dataMap.get("pan"), TextView.BufferType.EDITABLE);
            dateOfBirth.setText(dataMap.get("dob"), TextView.BufferType.EDITABLE);
            fatherName.setText(dataMap.get("fatherName"), TextView.BufferType.EDITABLE);
            name.setText(dataMap.get("name"), TextView.BufferType.EDITABLE);
        }
    }

    public void extractInfo(View view) {

        if(mImageBitmap != null)
        {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mImageBitmap);
            FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();;
            detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText text) {
                    HashMap<String, String> dataMap = new PanProcessing()
                            .processText(text, getApplicationContext());
                    presentOutput(dataMap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
        else
        {
            Toast.makeText(this, "Please take a picture.", Toast.LENGTH_SHORT).show();
        }

    }
}