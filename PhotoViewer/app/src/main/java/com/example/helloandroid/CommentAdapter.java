package com.example.helloandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<MainActivity.Comment> comments;

    public CommentAdapter(List<MainActivity.Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.Comment comment = comments.get(position);
        holder.commentAuthorTextView.setText(comment.getAuthor());
        holder.commentTextView.setText(comment.getText());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView commentAuthorTextView;
        public TextView commentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            commentAuthorTextView = itemView.findViewById(R.id.commentAuthorTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
        }
    }
}