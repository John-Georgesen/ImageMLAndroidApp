package com.example.interviewapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    Button cameraBtn;
    Button galleryBtn;
    Button detectTextBtn;
    Button detectFaceBtn;
    TextView imageText;
    ImageView imageView;
    Bitmap analyzeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        //init all objects
        cameraBtn = (Button)findViewById(R.id.cameraButton);
        galleryBtn = (Button)findViewById(R.id.galleryButton);
        detectTextBtn = (Button)findViewById(R.id.DetectTextBtn);
        detectFaceBtn = (Button)findViewById(R.id.DetectFaceBtn);
        imageText = (TextView)findViewById(R.id.foundText);
        imageView = (ImageView)findViewById(R.id.FoundImage);

        //set button onclicks
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,123);
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 124);
            }
        });
        detectTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectText();
            }
        });
        detectFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectFace();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if request code is for camera
        if (requestCode == 123)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            analyzeImage = photo;
        }
        //if request code is for gallery
        if (requestCode == 124)
        {
            Uri selectedImage = data.getData();

            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(photo);
            analyzeImage = photo;
        }
    }

    private void detectText() {
        // this is a method to detect a text from image.
        // below line is to create variable for firebase
        // vision image and we are getting image bitmap.
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(analyzeImage);

        // below line is to create a variable for detector and we
        // are getting vision text detector from our firebase vision.
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();

        // adding on success listener method to detect the text from image.
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                // calling a method to process
                // our text after extracting.
                processTxt(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // handling an error listener.
                Toast.makeText(MainActivity.this, "Fail to detect the text from image..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processTxt(FirebaseVisionText text) {
        // below line is to create a list of vision blocks which
        // we will get from our firebase vision text.
        List<FirebaseVisionText.Block> blocks = text.getBlocks();

        // checking if the size of the
        // block is not equal to zero.
        if (blocks.size() == 0) {
            // if the size of blocks is zero then we are displaying
            // a toast message as no text detected.
            Toast.makeText(MainActivity.this, "No Text ", Toast.LENGTH_LONG).show();
            return;
        }
        // extracting data from each block using a for loop.
        for (FirebaseVisionText.Block block : text.getBlocks()) {
            // below line is to get text
            // from each block.
            String txt = block.getText();

            // below line is to set our
            // string to our text view.
            imageText.setText(txt);
        }
    }
    private void detectFace()
    {
        FirebaseVisionImage image = null;
        FirebaseVisionFaceDetector detector = null;
        FirebaseVisionFaceDetectorOptions options
                = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setModeType(
                        FirebaseVisionFaceDetectorOptions
                                .ACCURATE_MODE)
                .setLandmarkType(
                        FirebaseVisionFaceDetectorOptions
                                .ALL_LANDMARKS)
                .setClassificationType(
                        FirebaseVisionFaceDetectorOptions
                                .ALL_CLASSIFICATIONS)
                .build();

        // we need to create a FirebaseVisionImage object
        // from the above mentioned image types(bitmap in
        // this case) and pass it to the model.
        try {
            image = FirebaseVisionImage.fromBitmap(analyzeImage);
            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // It’s time to prepare our Face Detection model.
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace> >() {
                    @Override
                    // adding an onSuccess Listener, i.e, in case
                    // our image is successfully detected, it will
                    // append it's attribute to the result
                    // textview in result dialog box.
                    public void onSuccess(
                            List<FirebaseVisionFace>
                                    firebaseVisionFaces)
                    {
                        String resultText = "";
                        int i = 1;
                        for (FirebaseVisionFace face :
                                firebaseVisionFaces) {
                            resultText
                                    = resultText
                                    .concat("\nFACE NUMBER. "
                                            + i + ": ")
                                    .concat(
                                            "\nSmile: "
                                                    + face.getSmilingProbability()
                                                    * 100
                                                    + "%")
                                    .concat(
                                            "\nleft eye open: "
                                                    + face.getLeftEyeOpenProbability()
                                                    * 100
                                                    + "%")
                                    .concat(
                                            "\nright eye open "
                                                    + face.getRightEyeOpenProbability()
                                                    * 100
                                                    + "%");
                            i++;

                            imageText.setText(resultText);
                        }
                    }
                }) // adding an onfailure listener as well if
                // something goes wrong.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast
                                .makeText(
                                        MainActivity.this,
                                        "Oops, Something went wrong",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}



