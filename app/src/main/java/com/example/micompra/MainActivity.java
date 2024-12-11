package com.example.micompra;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView insertNameBox;
    ImageView imgView;
    Dialog addItemDialog, endShoppingDialog;
    FloatingActionButton addItemBtn, endShoppingBtn, helpBtn;
    Button cameraBtn, addBtn, cancelAddBtn, endBtn, cancelEndBtn;
    RecyclerView recyclerView;
    DatabaseHelper myDB;
    ArrayList<String> item_names;
    ArrayList<byte[]> item_imgs;
    ArrayList<Integer> item_is_selected;
    ComprasAdapter comprasAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        helpBtn = findViewById(R.id.helpBtn);

        endShoppingDialog = new Dialog(MainActivity.this);
        endShoppingDialog.setContentView(R.layout.end_shopping_menu);
        endShoppingDialog.setCanceledOnTouchOutside(false);
        endShoppingBtn = findViewById(R.id.endShoppingBtn);
        endBtn = endShoppingDialog.findViewById(R.id.endBtn);
        cancelEndBtn = endShoppingDialog.findViewById(R.id.cancelBtn2);

        addItemDialog = new Dialog(MainActivity.this);
        addItemDialog.setContentView(R.layout.add_item_menu);
        addItemDialog.setCanceledOnTouchOutside(false);
        addItemBtn = findViewById(R.id.addItemBtn);
        cameraBtn = addItemDialog.findViewById(R.id.cameraBtn);
        addBtn = addItemDialog.findViewById(R.id.addBtn);
        cancelAddBtn = addItemDialog.findViewById(R.id.cancelBtn);

        insertNameBox = addItemDialog.findViewById(R.id.insertNameBox);
        imgView = addItemDialog.findViewById(R.id.imgView);
        imgView.setImageResource(R.drawable.camera);

        myDB = new DatabaseHelper(MainActivity.this);
        item_names = new ArrayList<>();
        item_imgs = new ArrayList<>();
        item_is_selected = new ArrayList<>();
        storeDataInArrays();

        recyclerView = findViewById(R.id.recyclerView);
        comprasAdapter = new ComprasAdapter(MainActivity.this, item_names, item_imgs, item_is_selected);
        recyclerView.setAdapter(comprasAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // USER MANUAL
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, Manual.class);
                startActivity(myIntent);
            }
        });

        // CAMERA ACCESS PERMISSIONS
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 1000);

        // Gestione dialog fine spesa
        // END SHOPPING BUTTON
        endShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endShoppingDialog.show();
            }
        });
        // CONFIRM END SHOPPING BUTTON
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = myDB.getWritableDatabase();

                db.delete(myDB.TABLE_NAME, "is_selected = ?", new String[]{"1"});

                item_names.clear();
                item_imgs.clear();
                item_is_selected.clear();
                storeDataInArrays();
                comprasAdapter.notifyDataSetChanged();

                endShoppingDialog.dismiss();
                Toast.makeText(MainActivity.this, "Elementos seleccionados borrados", Toast.LENGTH_SHORT).show();
            }
        });
        // CANCEL END SHOPPING BUTTON
        cancelEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endShoppingDialog.dismiss();
            }
        });

        // Gestione dialog aggiungi item
        // ADD ITEM BUTTON
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemDialog.show();
            }
        });
        // CAMERA BUTTON
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,100);
            }
        });
        // ADD TO LIST BUTTON
        boolean emptyName = true;
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper db = new DatabaseHelper(MainActivity.this);

                if(!insertNameBox.getText().toString().trim().equals("")){ // Entra se non vuoto
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imgView.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();

                    db.addItem(insertNameBox.getText().toString().trim(), getBytesFromBitmap(bitmap));
                    Toast.makeText(MainActivity.this, "Añadido a la lista", Toast.LENGTH_SHORT).show();

                    item_names.add(insertNameBox.getText().toString().trim());
                    item_imgs.add(getBytesFromBitmap(bitmap));
                    item_is_selected.add(0); // Assuming a default value for selection
                    comprasAdapter.notifyItemInserted(item_names.size() - 1);


                    insertNameBox.setText("");
                    imgView.setImageResource(R.drawable.camera);
                    addItemDialog.dismiss();
                }
                else
                    Toast.makeText(MainActivity.this, "Insertar algún texto", Toast.LENGTH_SHORT).show();
            }
        });
        // CANCEL ADD BUTTON
        cancelAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertNameBox.setText("");
                imgView.setImageResource(R.drawable.camera);
                addItemDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgView.setImageDrawable(null);
            imgView.setImageBitmap(imageBitmap);
        }
    }
    public byte[] getBytesFromBitmap(@NonNull Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); // Compress to PNG or JPEG
        return stream.toByteArray();
    }

    void storeDataInArrays(){
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
            Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
        }
        else{
            while(cursor.moveToNext()){
                item_names.add(cursor.getString(1));
                item_imgs.add(cursor.getBlob(2));
                item_is_selected.add(cursor.getInt(3));
            }
        }
    }
}