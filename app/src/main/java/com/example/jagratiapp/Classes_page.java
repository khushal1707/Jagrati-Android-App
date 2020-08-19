package com.example.jagratiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jagratiapp.model.Classes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Classes_page extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private EditText classname;
    private Button saveButton;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //TODO: Path add karna hai.
    private CollectionReference collectionReference = db.collection("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes_page);

        fab = findViewById(R.id.fab_class_page);
        recyclerView = findViewById(R.id.recyclerview_classes_page);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPopup();
            }
        });
    }

    private void createPopup() {
        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_classes,null);

        classname = view.findViewById(R.id.class_name_pop);
        saveButton = view.findViewById(R.id.saveClass_pop);

        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!classname.getText().toString().isEmpty()){
                    saveclass(view);
                }else
                {
                    Snackbar.make(view,"Empty Not Allowed",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveclass(View view) {
        Classes classes = new Classes();
        classes.setClassName(classname.getText().toString().trim());
        collectionReference.add(classes).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // to add delay
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        startActivity(new Intent(Classes_page.this , Classes_page.class));
                        finish();

                    }
                },800);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Classes_page.this,e.getMessage().toString().trim(),Toast.LENGTH_SHORT).show();

            }
        });

    }
}