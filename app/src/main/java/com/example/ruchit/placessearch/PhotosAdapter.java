package com.example.ruchit.placessearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>{

    List<Bitmap> photoBitmaps;
    Context context;

    PhotosAdapter(List<Bitmap> photoBitmaps, Context context){
        this.photoBitmaps = photoBitmaps;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return photoBitmaps.size();
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_photos_image_view, viewGroup, false);
        PhotoViewHolder pvh = new PhotoViewHolder(v, context, photoBitmaps);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder resultViewHolder, int i) {
        resultViewHolder.imageView.setImageBitmap(photoBitmaps.get(i));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        Context context;
        List<Bitmap> photoBitmaps;

        PhotoViewHolder(View itemView, Context context, List<Bitmap> photoBitmaps) {
            super(itemView);
            this.context = context;
            this.photoBitmaps = photoBitmaps;
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

}