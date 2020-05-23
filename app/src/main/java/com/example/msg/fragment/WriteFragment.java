package com.example.msg.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.msg.MainActivity;
import com.example.msg.MapActivity;
import com.example.msg.R;
import com.example.msg.SaleActivity;
import com.example.msg.UploadActivity;
import com.example.msg.model.ProductModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;


public class WriteFragment extends Fragment {
    private static final String TAG = "WriteFragment";
    private static final int PICK_FROM_ALBUM = 10;
    private View view;
    private Button enrollment;
    private Button upload;
    private ImageView Image;
    private Uri imageUri;
    private EditText title;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_write, container, false);

        enrollment = (Button) view.findViewById(R.id.writeFragment_button_enrollment);
        enrollment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SaleActivity.class);
                startActivity(intent);
                Toast.makeText(getActivity(), "업로드로 갑니다.", Toast.LENGTH_LONG).show();
            }
        });

        upload = (Button) view.findViewById(R.id.writeFragment_button_enrollment);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                startActivity(intent);
                Toast.makeText(getActivity(), "업로드로 갑니다.", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }


}

