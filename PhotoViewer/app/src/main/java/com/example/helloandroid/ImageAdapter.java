package com.example.helloandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<MainActivity.Post> posts;
    private String currentUserId;

    public ImageAdapter(List<MainActivity.Post> posts, String currentUserId) {
        this.posts = posts;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.Post post = posts.get(position);
        holder.titleTextView.setText(post.getTitle());
        holder.descriptionTextView.setText(post.getText());
        holder.imageView.setImageBitmap(post.getImageBitmap());
        holder.favoritesButton.setText("Favorites: " + post.getFavorites().size());

        holder.favoritesButton.setOnClickListener(v -> {
            post.toggleFavorite(currentUserId);
            holder.favoritesButton.setText("Favorites: " + post.getFavorites().size());
            updateFavoritesOnServer(post.getId(), currentUserId);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private void updateFavoritesOnServer(String postId, String userId) {
        new Thread(() -> {
            try {
                if (postId == null || postId.isEmpty()) {
                    throw new IllegalArgumentException("Post ID is missing");
                }
                String apiUrl = "https://syw5141.pythonanywhere.com/api_root/Post/" + postId + "/toggle_favorite/";
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Token b8f529195320e4886b7c00777630759ecfa21d7a");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_id", userId);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Handle success
                } else {
                    // Handle error
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public ImageView imageView;
        public Button favoritesButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            imageView = itemView.findViewById(R.id.imageView);
            favoritesButton = itemView.findViewById(R.id.favoritesButton);
        }
    }
}