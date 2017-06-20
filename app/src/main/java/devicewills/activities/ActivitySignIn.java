package devicewills.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.cracking.jflex.devicewilly.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by Dell on 2017-04-28.
 */

public class ActivitySignIn extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static int REQ_GOOGLE_LOGIN = 100;

    public static GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFbAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private LinearLayout signIn_Layout;
    private SignInButton mSignInBtn;

    private Animation zoomIn;
    private  ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSignInBtn = (SignInButton) findViewById(R.id.btn_sign_in_google);
        signIn_Layout = (LinearLayout) findViewById(R.id.signIn_layout);

        zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoomin);
        signIn_Layout.startAnimation(zoomIn);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();

        //파이어베이스 인증 인스턴스 가져옴
        mFbAuth = FirebaseAuth.getInstance();

        settingListener();

    }

    private void settingListener() {
        //인증 상태 변화 리스너 선언
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //현재 로그인 되어있는 유저 정보 가져옴
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //로그인 활성화시
                    Intent intent = new Intent(ActivitySignIn.this, ActivityMenu.class);
                    startActivity(intent);
                    finish();
                } else {
                    //로그인 비활성화시
                }
            }
        };

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(intent, REQ_GOOGLE_LOGIN);
            }
        });

        zoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                signIn_Layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //TODO,
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //TODO,
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mFbAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFbAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseWithGoogle(account);
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(), "인증실패.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseWithGoogle(GoogleSignInAccount account) {

        showProgressDialog();
        //구글 로그인 계정의 토큰을 가져와 인증정보로 교환
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        //파이어베이스 인증 정보로 교환하여 인증한다.
        mFbAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //인증 실패시
                if (!task.isSuccessful()) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "인증실패.", Snackbar.LENGTH_SHORT).show();
                }
                dismissProgressDialog();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(ActivitySignIn.this);
        progressDialog.setMessage("로그인 중...");
        progressDialog.show();
    }

    private void dismissProgressDialog(){
        progressDialog.dismiss();
    }
}
