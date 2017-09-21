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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cracking.jflex.devicewilly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ActivityForgetPw extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearLayout layout_forget;
    private EditText edit_forget_email;
    private Button btn_forget;

    private Animation animation;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pw);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edit_forget_email = (EditText) findViewById(R.id.edit_forget_email);
        layout_forget = (LinearLayout) findViewById(R.id.forget_layout);
        animation = AnimationUtils.loadAnimation(this, R.anim.zoomin);

        layout_forget.setAnimation(animation);

        btn_forget = (Button) findViewById(R.id.btnForget);
        btn_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_forget_email.getText().toString().getBytes().length > 0) {
                    findPwFbUser(edit_forget_email.getText().toString());
                } else {
                    Toast.makeText(ActivityForgetPw.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                layout_forget.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void findPwFbUser(String email) {

        showProgressDialog();

        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                dismissProgressDialog();

                if (task.isSuccessful()) {
                    Toast.makeText(ActivityForgetPw.this, "해당 이메일로 인증메일을 보냈습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("findPwFbUser", "error : " + task.getException().getMessage());
                    if (task.getException().getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                        Toast.makeText(ActivityForgetPw.this, "해당 이메일은 존재하지 않거나 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    } else if (task.getException().getMessage().equals("An internal error has occurred. [ INVALID_EMAIL ]")) {
                        Toast.makeText(ActivityForgetPw.this, "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(ActivityForgetPw.this);
        progressDialog.setMessage("전송중...");
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
