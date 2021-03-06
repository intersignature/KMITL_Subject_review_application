package kmitl.projectfinal.se.kmitlbackyard.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.santalu.emptyview.EmptyView;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.zhaiyifan.rememberedittext.RememberEditText;
import kmitl.projectfinal.se.kmitlbackyard.R;
import kmitl.projectfinal.se.kmitlbackyard.view.CustomTextView;

public class LoginActivity extends Activity {

    private EditText loginPassword;
    private RememberEditText loginEmail;
    private Button btnLogin, btnRegister;
    private FirebaseAuth firebaseAuth;
    private String inpEmail;
    private String inpPassword;
    private EmptyView emptyView;
    private CustomTextView forgotPass;
    private Dialog dialog;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[kmitl.-]{5}+\\.[ac]{2}+\\.[th]{2}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth =FirebaseAuth.getInstance();

        emptyView = findViewById(R.id.layout_content_container);

        if(firebaseAuth.getCurrentUser() != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        forgotPass = findViewById(R.id.forgot_pass);
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(LoginActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog_forgot_pass);
                dialog.setCancelable(true);
                dialog.show();

                final EditText editEmail = dialog.findViewById(R.id.edit_email);

                Button resetEmailBtn = dialog.findViewById(R.id.reset_email_btn);

                resetEmailBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String emailReset= editEmail.getText().toString();
                        if(emailReset.equals("") || emailReset.isEmpty()){
                            MDToast mdToast = MDToast.makeText(getApplicationContext(), "กรุณากรอกอีเมลล์", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
                            mdToast.show();
                        }
                        else {
                            resetPassword(emailReset);
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    private void resetPassword(String emailReset) {
        firebaseAuth.sendPasswordResetEmail(emailReset)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            MDToast mdToast = MDToast.makeText(getApplicationContext(), "รีพาสเวิร์ดสำเร็จ\nกรุณาตรวจสอบอีเมลล์ของท่าน", MDToast.LENGTH_SHORT, MDToast.TYPE_SUCCESS);
                            mdToast.show();
                        }
                        else {
                            MDToast mdToast = MDToast.makeText(getApplicationContext(), "กรุณาลองอีกครั้ง", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
                            mdToast.show();
                        }
                    }
                });
    }

    private void login() {
        inpEmail = loginEmail.getText().toString();
        inpPassword = loginPassword.getText().toString();
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(inpEmail);
        if (inpPassword.equals("") || inpEmail.equals("")){
            MDToast mdToast = MDToast.makeText(getApplicationContext(), "กรุณากรอกข้อมูลให้ครบ", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
            mdToast.show();
            return;
        }
        else if(!matcher.find()){
            MDToast mdToast = MDToast.makeText(getApplicationContext(), "กรุณากรอกอีเมลให้ถูกต้อง", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
            mdToast.show();
            loginPassword.setText("");
            return;
        }
        emptyView.showLoading();
        firebaseAuth.signInWithEmailAndPassword(inpEmail, inpPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    if (!firebaseAuth.getCurrentUser().isEmailVerified()){
//                        resend in login
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                MDToast mdToast = MDToast.makeText(getApplicationContext(), "กรุณายืนยันอีเมลล์ก่อนเข้าสู่ระบบ", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING);
                                mdToast.show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                MDToast mdToast = MDToast.makeText(getApplicationContext(), "การส่งอีเมลผิดผลาด\nกรุณาล็อกอินใหม่" + firebaseAuth.getCurrentUser().getEmail(), MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
                                mdToast.show();
                            }
                        });

                        Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent2);

                        FirebaseAuth.getInstance().signOut();
                        finish();
                        return;
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    MDToast mdToast = MDToast.makeText(getApplicationContext(), "กรุณากรอกอีเมลหรือพาสเวิร์ดให้ถูกต้อง", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
                    mdToast.show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}