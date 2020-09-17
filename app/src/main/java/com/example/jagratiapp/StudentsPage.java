package com.example.jagratiapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jagratiapp.model.Students;
import com.example.jagratiapp.ui.StudentDiffUtil;
import com.example.jagratiapp.ui.StudentRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentsPage extends AppCompatActivity implements View.OnClickListener{

    private String classUid;
    private String groupUid;
    private Button addStudent;
    private String className;
    private String groupName;
    private Button takeAttendence;
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private Button saveButton;
    private EditText studentNameEditText;
    private EditText guardianNameEditText;
    private EditText mobileNoEditText;
    private EditText villageNameEditText;
    private EditText rollnoEditText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private List<Students> studentsList;
    private RecyclerView studentRecyclerView;
    private StudentRecyclerAdapter studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.students_page);

        takeAttendence = findViewById(R.id.Attendence_button);
        addStudent = findViewById(R.id.add_student_button);
        studentRecyclerView = findViewById(R.id.student_recycler_view);
        studentRecyclerView.setHasFixedSize(true);
        studentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentsList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        classUid = bundle.getString("classUid");
        groupUid = bundle.getString("groupUid");
        documentReference = db.collection("Classes").document(classUid)
                .collection("Groups").document(groupUid);

        names();
        addStudent.setOnClickListener(this);
        takeAttendence.setOnClickListener(this);


        //yaha add kiya hai onStart wala
        //kyunki ab update yaha par nahi ho rha hai
        documentReference.collection("Students").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                Students student = documentSnapshot.toObject(Students.class);
                                student.setUid(documentSnapshot.getId());
                                studentsList.add(student);
                            }
                            studentAdapter = new StudentRecyclerAdapter(StudentsPage.this,studentsList);
                            studentRecyclerView.setAdapter(studentAdapter);
                            //studentAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(StudentsPage.this,"Kuch ni hain",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                //yaha tak onstart wala hai


    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_student_button:
                addStudentPopup();
                break;
            case R.id.Attendence_button:
                startActivity(new Intent(StudentsPage.this,AttendencePage.class).putExtra("classUid",classUid).putExtra("groupUid",groupUid));
                finish();
        }
    }

    private void addStudentPopup() {
        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.add_student_popup,null);

        saveButton = view.findViewById(R.id.save_student);
        studentNameEditText = view.findViewById(R.id.student_name_popup);
        guardianNameEditText = view.findViewById(R.id.guardian_name_popup);
        mobileNoEditText = view.findViewById(R.id.mobile_no_popup);
        villageNameEditText = view.findViewById(R.id.village_name_popup);
        rollnoEditText = view.findViewById(R.id.roll_no_popup);

        builder.setView(view);
        dialog = builder.create();
        dialog.show();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String studentName = studentNameEditText.getText().toString().trim();
                String guardianName = guardianNameEditText.getText().toString().trim();
                String mobileNo = mobileNoEditText.getText().toString().trim();
                String villageName = villageNameEditText.getText().toString().trim();
                String rollno = rollnoEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(studentName)
                    && !TextUtils.isEmpty(guardianName)
                    && !TextUtils.isEmpty(mobileNo)
                    && !TextUtils.isEmpty(villageName)
                    && !TextUtils.isEmpty(rollno)){
                    Students student = new Students(classUid,groupUid,studentName,className,groupName,guardianName,mobileNo,villageName,rollno);
                    saveStudent(student);
                }
            }
        });
    }

    private void saveStudent(final Students student) {
        db.collection("Students").document(student.getRollno())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.exists()){
                            documentReference.collection("Students").document(student.getRollno()).set(student)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Map<String,String> outsideStudent = new HashMap<>();
                                            outsideStudent.put("classUid",classUid);
                                            outsideStudent.put("groupUid",groupUid);
                                            db.collection("Students").document(student.getRollno()).set(outsideStudent)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(StudentsPage.this,"Student Saved",Toast.LENGTH_SHORT).show();
                                                            Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    List<Students> newStudentList = studentsList;
                                                                    student.setUid(documentReference.getId());
                                                                    newStudentList.add(student);
                                                                    StudentDiffUtil diffUtil = new StudentDiffUtil(studentsList,newStudentList);
                                                                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffUtil);
                                                                diffResult.dispatchUpdatesTo(studentAdapter);
                                                                                dialog.dismiss();
                                                                            }
                                                                        },600);
                                                                    }})
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                    }});
                                                    }
                                                });
                        }
                        else {
                            Toast.makeText(StudentsPage.this,"Roll no Exists",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void names() {
        db.collection("Classes").document(classUid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null){
                                className = documentSnapshot.getString("className");

                            }
                        }
                    }
                });


        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot != null){
                        groupName = documentSnapshot.getString("groupName");
                    }
                }
            }
        });
    }
}