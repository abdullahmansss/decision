package softagi.urdecision;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import softagi.urdecision.Models.UserModel;

public class StartActivity extends AppCompatActivity
{
    private EditText fname_field,lname_field;
    private Button next;
    private String gender = "",fname = "",lname = "";
    private RadioButton male,female;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private int language;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        language = getIntent().getIntExtra("lang", 1);
        init();
    }

    private void init()
    {
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        fname_field = findViewById(R.id.fname_field);
        lname_field = findViewById(R.id.lname_field);
        male = findViewById(R.id.male_rb);
        female = findViewById(R.id.female_rb);
        next = findViewById(R.id.next_btn);

        if (language == 0)
        {
            fname_field.setHint("enter your first name ..");
            lname_field.setHint("enter your last name ..");
            Typeface typeface = ResourcesCompat.getFont(StartActivity.this, R.font.bs);
            fname_field.setTypeface(typeface);
            lname_field.setTypeface(typeface);
            next.setText("next");
            next.setTypeface(typeface);
            male.setText("male");
            female.setText("female");
            male.setTypeface(typeface);
            female.setTypeface(typeface);
        } else if (language == 1)
        {
            fname_field.setHint("ادخل اسمك الأول ..");
            lname_field.setHint("ادخل اسمك الأخير ..");
            Typeface typeface = ResourcesCompat.getFont(StartActivity.this, R.font.cas);
            fname_field.setTypeface(typeface);
            lname_field.setTypeface(typeface);
            next.setText("التالي");
            next.setTypeface(typeface);
            male.setText("ذكر");
            female.setText("أنثي");
            male.setTypeface(typeface);
            female.setTypeface(typeface);
        }

        male.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gender = "male";
            }
        });

        female.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                gender = "female";
            }
        });
    }

    private void startSignIn()
    {
        fname = fname_field.getText().toString();
        lname = lname_field.getText().toString();

        if (TextUtils.isEmpty(fname))
        {
            if (language == 1)
            {
                Toast.makeText(getApplicationContext(), "ادخل اسمك الأول", Toast.LENGTH_SHORT).show();
                fname_field.requestFocus();
            } else if (language == 0)
            {
                Toast.makeText(getApplicationContext(), "enter your first name", Toast.LENGTH_SHORT).show();
                fname_field.requestFocus();
            }
            return;
        }

        if (TextUtils.isEmpty(lname))
        {
            if (language == 1)
            {
                Toast.makeText(getApplicationContext(), "ادخل اسمك الأخير", Toast.LENGTH_SHORT).show();
                lname_field.requestFocus();
            } else if (language == 0)
            {
                Toast.makeText(getApplicationContext(), "enter your last name", Toast.LENGTH_SHORT).show();
                lname_field.requestFocus();
            }
            return;
        }

        if (TextUtils.isEmpty(gender))
        {
            if (language == 1)
            {
                Toast.makeText(getApplicationContext(), "اختار جنسك", Toast.LENGTH_SHORT).show();
            } else if (language == 0)
            {
                Toast.makeText(getApplicationContext(), "select your gender", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        progressDialog = new ProgressDialog(StartActivity.this);
        if (language == 1)
        {
            progressDialog.setMessage("برجاء الانتظار");
        } else if (language == 0)
        {
            progressDialog.setMessage("Wait ...");
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        String name = fname + " " + lname;

        createUser(name,gender);
    }

    private void createUser(final String name, final String g)
    {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            FirebaseUser user = task.getResult().getUser();
                            if (user != null)
                            {
                                Addtodb(name,user.getUid(),g);
                            }
                        } else
                            {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void Addtodb(String name, String id, String g)
    {
        UserModel userModel = new UserModel(name,g);
        databaseReference.child("Users").child(id).setValue(userModel);

        PreferenceManager.getDefaultSharedPreferences(StartActivity.this).edit().putString("name", fname).apply();
        PreferenceManager.getDefaultSharedPreferences(StartActivity.this).edit().putString("gen", g).apply();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    public void next(View view)
    {
        startSignIn();
    }
}