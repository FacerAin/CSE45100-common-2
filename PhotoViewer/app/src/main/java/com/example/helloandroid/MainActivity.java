package com.example.helloandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    ImageView imgView;
    TextView textView;
    String site_url = "https://syw5141.pythonanywhere.com/";
    JSONObject post_json;
    String imageUrl = null;
    Bitmap bmImg = null;
    CloadImage taskDownload;
    EditText commentEditText;
    Button submitCommentButton;
    RecyclerView commentsRecyclerView;
    CommentAdapter commentAdapter;
    List<Comment> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        textView = findViewById(R.id.textView);

        Button downloadButton = findViewById(R.id.btn_download);
        downloadButton.setOnClickListener(v -> {
            for (int i = 0; i < taskDownload.postList.size(); i++) {
                Post post = taskDownload.postList.get(i);
                if (post.getImageBitmap() != null) {
                    saveImageToSDCard(post.getImageBitmap(), "downloaded_image_" + i);
                }
            }


        });

        Button shareButton = findViewById(R.id.btn_share);
        shareButton.setOnClickListener(v -> {
            List<Post> posts = taskDownload.getPostList();
            if (!posts.isEmpty() && posts.get(0).getImageBitmap() != null) {
                shareImage(posts.get(0).getImageBitmap());
            }
        });




    }

    private void shareImage(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Image Description", null);
        Uri uri = Uri.parse(path);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }



    private void saveImageToSDCard(Bitmap bitmap, String imageName) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDirectory = Environment.getExternalStorageDirectory();
                File imageFile = new File(sdCardDirectory, imageName + ".jpg");

                try (FileOutputStream outStream = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    Toast.makeText(this, "Image saved to SD card", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "SD card not mounted", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Permission not granted to write to external storage", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Download", Toast.LENGTH_LONG).show();
    }

    public void onClickUpload(View v) {
        String apiUrl = site_url + "/api_root/Post/";
        Intent intent = new Intent(MainActivity.this, ActivityUpload.class);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "Upload", Toast.LENGTH_LONG).show();
    }

    private class CloadImage extends AsyncTask<String, Integer, List<Post>> {
        private List<Post> postList = new ArrayList<>();

        public List<Post> getPostList() {
            return postList;
        }
        @Override
        protected List<Post> doInBackground(String... urls) {

            try {
                String apiUrl = urls[0];
                String token = "b8f529195320e4886b7c00777630759ecfa21d7a";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);
                    for (int i = 0; i < aryJson.length(); i++) {
                        JSONObject postJson = aryJson.getJSONObject(i);
                        String id = postJson.getString("id");
                        String author = postJson.getString("author");
                        String title = postJson.getString("title");
                        String text = postJson.getString("text");
                        String imageUrl = postJson.getString("image");
                        String createdDate = postJson.getString("created_date");
                        String publishedDate = postJson.optString("published_date", null);
                        List<String> favorites = new ArrayList<>();
                        if (postJson.has("favorites")) {
                            JSONArray favoritesJson = postJson.getJSONArray("favorites");
                            for (int j = 0; j < favoritesJson.length(); j++) {
                                favorites.add(favoritesJson.getString(j));
                            }
                        }
                        Bitmap imageBitmap = null;
                        if (!imageUrl.equals("")) {
                            URL myImageUrl = new URL(imageUrl);
                            conn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = conn.getInputStream();
                            imageBitmap = BitmapFactory.decodeStream(imgStream);
                            imgStream.close();
                        }
                        Post post = new Post(id, author, title, text, imageBitmap, createdDate, publishedDate, favorites);
                        postList.add(post);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return postList;
        }

        @Override
        protected void onPostExecute(List<Post> posts)  {
            if (posts.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
            } else {
                textView.setText("이미지 로드 성공!");
                Collections.sort(posts, (p1, p2) -> Integer.compare(p2.getFavorites().size(), p1.getFavorites().size()));
                RecyclerView recyclerView = findViewById(R.id.recyclerView);

                String currentUserId = "1"; // Assuming user ID is 1
                ImageAdapter adapter = new ImageAdapter(posts, currentUserId);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);

                // Save the first image to SD card as an example
                if (!posts.isEmpty() && posts.get(0).getImageBitmap() != null) {
                    saveImageToSDCard(posts.get(0).getImageBitmap(), "downloaded_image");
                }
            }
        }
    }

    private void submitComment(String postId, String commentText) {
        new Thread(() -> {
            try {
                String apiUrl = "http://10.0.2.2:8000/api_root/comments/";
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Token b8f529195320e4886b7c00777630759ecfa21d7a");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("post", postId);
                jsonParam.put("author", "1"); // Assuming user ID is 1
                jsonParam.put("text", commentText);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Handle success
                    runOnUiThread(() -> {
                        commentList.add(new Comment("1", postId, "Author", commentText, "Now")); // Assuming comment ID is 1
                        commentAdapter.notifyDataSetChanged();
                    });
                } else {
                    // Handle error
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class Post {
        private String id;
        private String author;
        private String title;
        private String text;
        private Bitmap imageBitmap;
        private String createdDate;
        private String publishedDate;
        private List<String> favorites;

        public Post(String id, String author, String title, String text, Bitmap imageBitmap, String createdDate, String publishedDate, List<String> favorites) {
            this.id = id;
            this.author = author;
            this.title = title;
            this.text = text;
            this.imageBitmap = imageBitmap;
            this.createdDate = createdDate;
            this.publishedDate = publishedDate;
            this.favorites = favorites;
        }

        public String getId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }

        public Bitmap getImageBitmap() {
            return imageBitmap;
        }

        public String getCreatedDate() {
            return createdDate;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public List<String> getFavorites() {
            return favorites;
        }

        public void toggleFavorite(String userId) {
            if (favorites.contains(userId)) {
                favorites.remove(userId);
            } else {
                favorites.add(userId);
            }
        }


    }

    public class Comment {
        private String id;
        private String postId;
        private String author;
        private String text;
        private String createdDate;

        public Comment(String id, String postId, String author, String text, String createdDate) {
            this.id = id;
            this.postId = postId;
            this.author = author;
            this.text = text;
            this.createdDate = createdDate;
        }

        public String getId() {
            return id;
        }

        public String getPostId() {
            return postId;
        }

        public String getAuthor() {
            return author;
        }

        public String getText() {
            return text;
        }

        public String getCreatedDate() {
            return createdDate;
        }
    }
}