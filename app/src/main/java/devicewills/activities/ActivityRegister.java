package devicewills.activities;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cracking.jflex.devicewilly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityRegister extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText edit_name;
    private EditText edit_email;
    private EditText edit_passowrd;
    private EditText edit_repeat;
    private Button btn_register;
    private RelativeLayout layout_register;

    private ProgressDialog progressDialog;
    private Animation zoomIn;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_email = (EditText) findViewById(R.id.edit_email);
        edit_passowrd = (EditText) findViewById(R.id.edit_password);
        edit_repeat = (EditText) findViewById(R.id.edit_password_repeat);
        layout_register = (RelativeLayout) findViewById(R.id.layout_Register);
        zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoomin);

        layout_register.setAnimation(zoomIn);

        btn_register = (Button) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_name.getText().toString().getBytes().length == 0 || edit_email.getText().toString().getBytes().length == 0 || edit_passowrd.getText().toString().getBytes().length == 0 || edit_repeat.getText().toString().getBytes().length == 0) {
                    Toast.makeText(ActivityRegister.this, "정보를 모두 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    if (edit_passowrd.getText().toString().getBytes().length < 6) {
                        Toast.makeText(ActivityRegister.this, "비밀번호는 6자리 이상 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!edit_passowrd.getText().toString().equals(edit_repeat.getText().toString())) {
                            Toast.makeText(ActivityRegister.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            registerMember(edit_email.getText().toString(), edit_passowrd.getText().toString());
                        }
                    }
                }
            }
        });

        zoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                layout_register.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void registerMember(String email, String pw) {

        showProgressDialog();

        auth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("ActivityRegister", "create Complete");
                    FirebaseUser user = auth.getCurrentUser();
                    Toast.makeText(ActivityRegister.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e("ActivityRegister", "create Failed" + task.getException());
                    Toast.makeText(ActivityRegister.this, "이미 존재하는 계정이거나 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                }

                dismissProgressDialog();
            }
        });
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(ActivityRegister.this);
        progressDialog.setMessage("회원가입 중...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
