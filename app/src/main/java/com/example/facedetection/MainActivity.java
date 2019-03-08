package com.example.facedetection;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;
    ImageView imageView, emoji;
    Button button;
    TextView textView;
    Bitmap bitmap;

    ProgressDialog progressDialog;

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = findViewById(R.id.takeBtn);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.tvResult);
        emoji = findViewById(R.id.emojImage);


        button.setOnClickListener(v -> {

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);
            }

        });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            imageView.setImageBitmap(bitmap);

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setIndeterminate(false);
            progressDialog.show();
            SetupImage(bitmap);


        }
    }


    private void SetupImage(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage
                .fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions highAccuracyOpts = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build();


        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(highAccuracyOpts);


        detector.detectInImage(image).addOnSuccessListener(this::ProcessFace)
                .addOnFailureListener(e -> Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show());


    }


    private void ProcessFace(List<FirebaseVisionFace> faces) {
        for (FirebaseVisionFace face : faces) {

            float smileProb = 0;
            float rightEyeOpenProb = 0;
            float leftEyeOpenProbability = 0;

            Rect bounds = face.getBoundingBox();
            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

            // If landmark detection was enabled
            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
            if (leftEar != null) {
                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
            }


            // If classification was enabled:
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                smileProb = face.getSmilingProbability();
            }


            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                rightEyeOpenProb = face.getRightEyeOpenProbability();
            }

            if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                leftEyeOpenProbability = face.getLeftEyeOpenProbability();
            }

            // If face tracking was enabled:
            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                int id = face.getTrackingId();
            }


            setResults(smileProb > 0.4, rightEyeOpenProb > 0.3, leftEyeOpenProbability > 0.3);


        }
    }


    private void setResults(Boolean smile, Boolean rightEyeOpen, Boolean leftEyeOpen) {

        progressDialog.dismiss();

        String result = "You Look";

        if (smile) {
            // smile
            result += " Lovely";
            if (rightEyeOpen && leftEyeOpen) {

                emoji.setImageResource(R.drawable.love);
                result += " with awesome eyes";
            } else if (rightEyeOpen || leftEyeOpen) {
                result += " when winkling";
            }


        } else if (rightEyeOpen && leftEyeOpen) {
            result += " Scary";
            emoji.setImageResource(R.drawable.devil);
        } else {
            // sad
            emoji.setImageResource(R.drawable.sad);
            result += " Sad";

        }


        textView.setText(result);

    }


}
