package devicewills.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cracking.jflex.devicewilly.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
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
    private EditText editFbEmail;
    private EditText editFbPassword;
    private Button btnemailLogin;
    private SignInButton mSignInBtn;
    private LoginButton mFbSigninBtn;
    private TextView txtRegister;
    private TextView txtForget;

    private CallbackManager callbackManager;

    private Animation zoomIn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSignInBtn = (SignInButton) findViewById(R.id.btn_sign_in_google);
        mSignInBtn.setSize(SignInButton.SIZE_WIDE);
        signIn_Layout = (LinearLayout) findViewById(R.id.signIn_layout);

        zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoomin);
        signIn_Layout.startAnimation(zoomIn);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();

        //파이어베이스 인증 인스턴스 가져옴
        mFbAuth = FirebaseAuth.getInstance();

        //페이스북 로그인 응답 처리 콜백관리자 생성
        callbackManager = CallbackManager.Factory.create();

        txtRegister = (TextView) findViewById(R.id.txt_register);
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivitySignIn.this, ActivityRegister.class));
            }
        });

        txtForget = (TextView) findViewById(R.id.txt_forget);
        txtForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivitySignIn.this, ActivityForgetPw.class));
            }
        });

        mFbSigninBtn = (LoginButton) findViewById(R.id.btn_sign_in_fb);
        mFbSigninBtn.setReadPermissions("email", "public_profile");
        mFbSigninBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("ActivitySignIn", "fb:onSuceess" + loginResult);
                firebaseWithFaceBook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.e("ActivitySignIn", "fb:onCancel");
                Snackbar.make(getWindow().getDecorView().getRootView(), "인증실패.", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("ActivitySignIn", "fb:onError" + error);
                Snackbar.make(getWindow().getDecorView().getRootView(), "인증실패.", Snackbar.LENGTH_SHORT).show();
            }
        });

        editFbEmail = (EditText) findViewById(R.id.edit_fb_email);
        editFbPassword = (EditText) findViewById(R.id.edit_fb_pw);
        btnemailLogin = (Button) findViewById(R.id.btnEmailLogin);
        btnemailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                mFbAuth.signInWithEmailAndPassword(editFbEmail.getText().toString(), editFbPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        dismissProgressDialog();

                        if (task.isSuccessful()) {
                            startActivity(new Intent(ActivitySignIn.this, ActivityMenu.class));
                        } else {
                            Log.e("Login Error", String.valueOf(task.getException()));
                            if (task.getException().getMessage().equals("The password is invalid or the user does not have a password.")) {
                                Toast.makeText(ActivitySignIn.this, "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                            } else if (task.getException().getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                Toast.makeText(ActivitySignIn.this, "없는 계정이거나 삭제된 계정입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ActivitySignIn.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

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

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseWithFaceBook(AccessToken accessToken) {
        Log.d("ActivitySignIn", "firebaseWithFB");

        showProgressDialog();

        //페이스북 로그인 계정의 토큰을 가져와 인증정보로 변환
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());

        //파이어베이스 인증전보를 인증함
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

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(ActivitySignIn.this);
        progressDialog.setMessage("로그인 중...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        progressDialog.dismiss();
    }
}
