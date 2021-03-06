package com.hyperlife;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hyperlife.model.UserHelperClass;



import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class SignUpActivity extends AppCompatActivity {
    //    TextView txtDate;
//    private int mYear, mMonth, mDay, mHour, mMinute;
//    TextInputLayout layoutEmail,layoutUsername,layoutName,layoutPassword;
//    TextInputEditText edtEmail, edtUsername, edtName, edtPassword;
//    Button btnCreateAccount;
    private EditText mEmail, mPass, mName, mUsername;
    private TextView mHaveAccount, mDisplayDate;
    private Button signupButton;
    private ProgressBar mProgressbarAuth1;
    //
    private FirebaseAuth mAuth;
    private FirebaseDatabase rootNode;
    private FirebaseFirestore firestore;
    private String mDate = "";
    DatabaseReference reference;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private CheckBox maleCheckbox, femaleCheckbox;

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mHaveAccount = findViewById(R.id.jumptosignin);
        signupButton = findViewById(R.id.btSignup);
        mProgressbarAuth1 = findViewById(R.id.progressBar_signup);
        mDisplayDate = findViewById(R.id.date_picker);
        maleCheckbox = findViewById(R.id.male_checkbox);
        femaleCheckbox = findViewById(R.id.female_checkbox);
        //
        mAuth = FirebaseAuth.getInstance();

        View includeSignUpFieldLayout = findViewById(R.id.includedLayout);


        //Already have account
        mHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });

        //Move to signin after sign up
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
                mProgressbarAuth1.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressbarAuth1.setVisibility(View.INVISIBLE);
                    }
                }, 4000);
            }
        });

        maleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (maleCheckbox.isChecked()) {
                    femaleCheckbox.setChecked(false);
                }
            }
        });

        femaleCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (femaleCheckbox.isChecked()) {
                    maleCheckbox.setChecked(false);
                }
            }
        });

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        SignUpActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                Log.d("AddToDoItemActivity", "onDateSet: date" + dayOfMonth + "/" + month + "/" + year);
                mDate = dayOfMonth + "/" + month + "/" + year;

                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int age = currentYear - year;
                if (age >= 13) {
                    mDisplayDate.setText(mDate);
                } else {
                    Toast.makeText(SignUpActivity.this, "You need to be older than 12 years old to use this application!", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void createUser() {

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");

        //Get all the values
        View includeSignUpFieldLayout = findViewById(R.id.includedLayout);
        mEmail = includeSignUpFieldLayout.findViewById(R.id.et_email_signup);
        mPass = includeSignUpFieldLayout.findViewById(R.id.et_password_signup);
        mName = includeSignUpFieldLayout.findViewById(R.id.et_fullname);
        mUsername = includeSignUpFieldLayout.findViewById(R.id.et_username);
        mDisplayDate = includeSignUpFieldLayout.findViewById(R.id.date_picker);
        maleCheckbox = includeSignUpFieldLayout.findViewById(R.id.male_checkbox);
        femaleCheckbox = includeSignUpFieldLayout.findViewById(R.id.female_checkbox);
        String name = mName.getText().toString();
        String username = mUsername.getText().toString();
        String email = mEmail.getText().toString();
        String pass = mPass.getText().toString();
        UserHelperClass helperClass = new UserHelperClass(name, username, email, pass);
        reference.child(username).setValue(helperClass);
        //..................


        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (!pass.isEmpty()) {
                if (!(!maleCheckbox.isChecked() && !femaleCheckbox.isChecked())) {
                    if (!mDisplayDate.getText().toString().equals("Select your date of birth")) {
                        mAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        String tempGender = getGender(maleCheckbox, femaleCheckbox);
                                        String passworldHashed = BCrypt.withDefaults().hashToString(12, pass.toCharArray());
                                        CreateUserOnFirebase(email, username, passworldHashed, tempGender, mDate);

                                        Toast.makeText(SignUpActivity.this, "Sign Up Successfully !!", Toast.LENGTH_SHORT).show();
                                        //..........

                                        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignUpActivity.this, "Registration Error !!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Please select date of birth!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Please select gender!", Toast.LENGTH_SHORT).show();
                }
            } else {
                mPass.setError("Your Password must not empty");
            }
        } else if (email.isEmpty()) {
            mEmail.setError("Your email must not empty");
        } else {
            mEmail.setError("Please enter correct email");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void CreateUserOnFirebase(String userEmail, String userName, String password, String gender, String dateOfBirth) {
        //Set up firestore
        firestore = FirebaseFirestore.getInstance();
        LocalDate today = LocalDate.now();

        // Save user data to firestore
        Map<String, Object> user = new HashMap<>();
        user.put("name", userName);
        user.put("email", userEmail);
        user.put("password", password);
        user.put("gender", gender);
        user.put("join_date", today.toString());
        user.put("date_of_birth", dateOfBirth);
        user.put("weight", "empty");
        user.put("height", "empty");
        user.put("step_goal", "empty");
        user.put("drink_goal", "empty");
        user.put("calories_burn_goal", "empty");
        user.put("sleep_goal", "empty");
        user.put("on_screen_goal", "empty");
        user.put("recent_workout", "empty");
        user.put("time_to_sleep", "empty");
        user.put("wake_time", "empty");
        user.put("Google||Facebook", false);
        firestore.collection("users").document(userEmail)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignUpActivity.this, "Fail to save data to Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getGender(CheckBox maleCheckbox, CheckBox femaleCheckbox) {
        if (maleCheckbox.isChecked()) {
            return "Male";
        } else {
            return "Female";
        }
    }
}