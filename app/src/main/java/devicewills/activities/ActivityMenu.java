package devicewills.activities;

import android.animation.Animator;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.cracking.jflex.devicewilly.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import devicewills.adapters.AppViewPagerAdapter;
import devicewills.services.WillService;
import devicewills.utils.PreferencesUtil;
import devicewills.utils.ServiceRunningChecker;

public class ActivityMenu extends AppCompatActivity {

    private int[] tabColors;
    private boolean useMenuResource = true;
    private boolean isServiceRunning = false;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private AHBottomNavigationAdapter navigationAdapter;
    private ServiceRunningChecker checker;

    private FirebaseAuth mFbAuth;
    private GoogleApiClient mGoogleApiClient;

    private AppFragments currentFragment;
    private AppViewPagerAdapter adapter;
    private AHBottomNavigationViewPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private FloatingActionButton fab_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enabledTranslucentNavigation = getSharedPreferences("shared", Context.MODE_PRIVATE)
                .getBoolean("translucentNavigation", false);
        setTheme(enabledTranslucentNavigation ? R.style.AppTheme_TranslucentNavigation : R.style.AppTheme);
        setContentView(R.layout.activity_menu);

        initUI();
    }

    private void initUI() {

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(ActivityMenu.this).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();

        //파이어베이스 인증 인스턴스 가져옴
        mFbAuth = FirebaseAuth.getInstance();

        checker = new ServiceRunningChecker();
        isServiceRunning = checker.isServiceRunning("devicewills.services.WillService", getApplicationContext());
        if (isServiceRunning == true) {
            /*
            preferences isServiceRunning 서비스 작동여부
             */
            PreferencesUtil.setBoolPreferences(ActivityMenu.this, "isServiceRunning", true);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ActivityMenu.this);
            alertDialogBuilder.setMessage("이미 서비스를 시작한 상태입니다.\n 서비스를 종료하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ActivityMenu.this, WillService.class);
                            stopService(intent);
                            PreferencesUtil.setBoolPreferences(ActivityMenu.this, "isServiceRunning", false);
                            Toast.makeText(ActivityMenu.this, "서비스를 종료하였습니다. \n새로운 서비스를 시작해보세요!!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            PreferencesUtil.setBoolPreferences(ActivityMenu.this, "isServiceRunning", true);
                            Toast.makeText(ActivityMenu.this, "서비스 종료 까지 새로운 서비스를 시작 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });

            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }

        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        viewPager = (AHBottomNavigationViewPager) findViewById(R.id.view_pager);
        fab_logout = (FloatingActionButton) findViewById(R.id.fab_logout);
        fab_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMenu.this);
                builder.setMessage("로그아웃하시겠습니까?").setCancelable(false).setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final ProgressDialog progressDialog = new ProgressDialog(ActivityMenu.this);
                        progressDialog.setMessage("로그아웃 중...");
                        progressDialog.show();

                        mGoogleApiClient.connect();
                        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(@Nullable Bundle bundle) {

                                mFbAuth.signOut();
                                if (mGoogleApiClient.isConnected()) {
                                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(@NonNull Status status) {
                                            Intent intent = new Intent(ActivityMenu.this, ActivitySignIn.class);
                                            startActivity(intent);
                                            progressDialog.dismiss();
                                            Toast.makeText(ActivityMenu.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                Log.d(null, "Google API Client Connection Suspend");
                            }
                        });
                    }
                }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog logout_alertDialog = builder.create();
                logout_alertDialog.show();
            }
        });

        if (useMenuResource) {

            tabColors = getApplicationContext().getResources().getIntArray(R.array.tab_colors);
            navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu);
            navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors);

        } else {

            AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.ic_apps_black_24dp, R.color.color_tab);
            AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.icon_list, R.color.color_tab);

            bottomNavigationItems.add(item1);
            bottomNavigationItems.add(item2);

            bottomNavigation.addItems(bottomNavigationItems);
        }

        bottomNavigation.setTranslucentNavigationEnabled(true);
        bottomNavigation.setColored(true);

        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (currentFragment == null) {
                    currentFragment = adapter.getCurrentFragment();
                }

                if (wasSelected) {
                    currentFragment.refresh();
                    return true;
                }

                if (currentFragment != null) {
                    currentFragment.willBeHidden();
                }

                viewPager.setCurrentItem(position, false);
                currentFragment = adapter.getCurrentFragment();
                currentFragment.willBeDisplayed();

                if (position == 1) {
                    bottomNavigation.setNotification("", 1);

                    fab_logout.setVisibility(View.VISIBLE);
                    fab_logout.setAlpha(0f);
                    fab_logout.setScaleX(0f);
                    fab_logout.setScaleY(0f);
                    fab_logout.animate()
                            .alpha(1)
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    fab_logout.animate()
                                            .setInterpolator(new LinearOutSlowInInterpolator())
                                            .start();
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .start();

                } else {
                    if (fab_logout.getVisibility() == View.VISIBLE) {
                        fab_logout.animate()
                                .alpha(0)
                                .scaleX(0)
                                .scaleY(0)
                                .setDuration(300)
                                .setInterpolator(new LinearOutSlowInInterpolator())
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        fab_logout.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        fab_logout.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                })
                                .start();
                    }
                }

                return true;
            }
        });

        viewPager.setOffscreenPageLimit(4);
        adapter = new AppViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        currentFragment = adapter.getCurrentFragment();
        bottomNavigation.setDefaultBackgroundResource(R.drawable.bottom_navigation_background);
    }

}
