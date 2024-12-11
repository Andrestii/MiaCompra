package com.example.micompra;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ComprasAdapter extends RecyclerView.Adapter<ComprasAdapter.MyViewHolder> {

    Context context;
    ArrayList item_names, item_imgs, item_is_selected;
    DatabaseHelper databaseHelper;

    public ComprasAdapter(Context context, ArrayList item_names, ArrayList item_imgs, ArrayList item_is_selected){
        this.context = context;
        this.item_names = item_names;
        this.item_imgs = item_imgs;
        this.item_is_selected = item_is_selected;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ComprasAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComprasAdapter.MyViewHolder holder, int position) {
        holder.textViewItemName.setText(String.valueOf(item_names.get(position)));

        byte[] imageBytes = (byte[]) item_imgs.get(position);
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.imageViewItem.setImageBitmap(bitmap);
        } else {
            holder.imageViewItem.setImageResource(R.drawable.camera);
        }

        // Set initial state of the CheckBox
        holder.checkBox.setChecked(item_is_selected.get(position).equals(1));

        // Listener to update item_is_selected when the CheckBox is toggled
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newValue = isChecked ? 1 : 0;
            databaseHelper.updateIsSelected(String.valueOf(item_names.get(position)), newValue);
            item_is_selected.set(position, newValue);
        });
    }

    @Override
    public int getItemCount() {
        return item_names.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewItemName;
        ImageView imageViewItem;
        CheckBox checkBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
