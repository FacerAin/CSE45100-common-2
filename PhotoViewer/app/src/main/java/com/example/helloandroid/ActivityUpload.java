package com.example.helloandroid;

import android.content.Intent;
import android.graphics.Bitmap;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityUpload extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private EditText editTextTitle, editTextText;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imageView = findViewById(R.id.imageView);
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextText = findViewById(R.id.editTextText);
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        Button buttonUpload = findViewById(R.id.buttonUpload);

        buttonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage != null) {
                    new PutPost().execute();
                } else {
                    Toast.makeText(ActivityUpload.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class PutPost extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                String apiUrl = "https://syw5141.pythonanywhere.com/api_root/Post/";
                String token = "b8f529195320e4886b7c00777630759ecfa21d7a";
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                String currentDate = sdf.format(new Date());

                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                builder.addTextBody("title", editTextTitle.getText().toString(), ContentType.TEXT_PLAIN);
                builder.addTextBody("author", "1", ContentType.TEXT_PLAIN);
                builder.addTextBody("text", editTextText.getText().toString(), ContentType.TEXT_PLAIN);
                builder.addTextBody("created_date", currentDate, ContentType.TEXT_PLAIN);
                builder.addTextBody("published_date", currentDate, ContentType.TEXT_PLAIN);
                builder.addBinaryBody("image", imageBytes, ContentType.DEFAULT_BINARY, "image.jpg");

                HttpEntity entity = builder.build();
                conn.setRequestProperty("Content-Type", entity.getContentType().getValue());
                OutputStream os = conn.getOutputStream();
                entity.writeTo(os);
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "Upload successful";
                } else {
                    return "Upload failed";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(ActivityUpload.this, result, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ActivityUpload.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}