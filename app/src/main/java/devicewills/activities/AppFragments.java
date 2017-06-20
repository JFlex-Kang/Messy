package devicewills.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cracking.jflex.devicewilly.R;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.melnykov.fab.FloatingActionButton;
import com.tsengvn.typekit.Typekit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import devicewills.adapters.DataListAdapter;
import devicewills.listeners.RecyclerItemClickListener;
import devicewills.objects.UserSettingProfile;
import devicewills.services.BatteryCheckService;
import devicewills.services.WillService;
import devicewills.utils.CustomTextWatcher;
import devicewills.utils.FormValidator;
import devicewills.utils.PreferencesUtil;
import devicewills.utils.TimeCalculator;

/**
 *
 */
public class AppFragments extends Fragment implements GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener {

    private TextView txtMin;
    private TextView txtMax;
    private TextView txtDataSetting;
    private TextView txtSelectBtyPerc;
    private CrystalSeekbar crystalRangeSeekbar1;
    private DonutProgress batteryProgress;
    private TextInputLayout mInputMsgLayout;
    private TextInputEditText mEditMsg;
    private FloatingActionButton mFabSendToFb;
    private FrameLayout fragmentContainer;
    private RelativeLayout linear_service;
    private LinearLayout layout_blank;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFbAuth;
    private FirebaseUser mFbUser;
    private FirebaseDatabase mFbDb;
    private DatabaseReference userDatabase;

    private WillService mWillService;
    private ArrayList<UserSettingProfile> userDatas;
    private ArrayList<String> mListPercentages;

    private final int TYPE_NEW = 0;
    private final int TYPE_DATA = 1;
    private int mCurPercentage = 0;
    private String mSelectedPercentage;

    /**
     * Create a new instance of the fragment
     */
    public static AppFragments newInstance(int index) {
        AppFragments fragment = new AppFragments();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //파이어베이스 인증 인스턴스 가져옴
        mFbAuth = FirebaseAuth.getInstance();
        //파이어베이스 DB 인스턴스 가져옴
        mFbDb = FirebaseDatabase.getInstance();
        //현제 로그인 유저 정보 가져옴
        mFbUser = mFbAuth.getCurrentUser();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(getContext()).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();

        if (getArguments().getInt("index", 0) == 0) {
            View view = inflater.inflate(R.layout.fragment_menu, container, false);
            initDemoSettings(view);
            return view;
        } else {
            View view = inflater.inflate(R.layout.fragment_setting_list, container, false);
            initDataList(view);
            return view;
        }
    }

    /**
     * Init demo settings
     */
    private void initDemoSettings(View view) {

        linear_service = (RelativeLayout) view.findViewById(R.id.layout_menu);
        txtDataSetting = (TextView) view.findViewById(R.id.txt_mid);
        txtDataSetting.setTypeface(Typekit.createFromAsset(getContext(),"Roboto-Light.ttf"));
        txtSelectBtyPerc = (TextView) view.findViewById(R.id.txt_top);
        txtSelectBtyPerc.setTypeface(Typekit.createFromAsset(getContext(),"Roboto-Bold.ttf"));
        txtMax = (TextView) view.findViewById(R.id.txt_val_max);
        txtMin = (TextView) view.findViewById(R.id.txt_val_min);
        batteryProgress = (DonutProgress) view.findViewById(R.id.battery_progress);
        crystalRangeSeekbar1 = (CrystalSeekbar) view.findViewById(R.id.seekbar1);
        crystalRangeSeekbar1.setMinValue(1);
        mInputMsgLayout = (TextInputLayout) view.findViewById(R.id.msg_input_layout);
        mEditMsg = (TextInputEditText) view.findViewById(R.id.msg_input_edit);
        mFabSendToFb = (FloatingActionButton) view.findViewById(R.id.fab_send_fbase);

        mEditMsg.addTextChangedListener(new CustomTextWatcher(getContext(), mEditMsg, mInputMsgLayout));

        PermissionListener listener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                //파이어베이스 인증 인스턴스 가져옴
                mFbAuth = FirebaseAuth.getInstance();

                if (mWillService == null) {
                    //베터리 측정 서비스 시작
                    Intent i = new Intent(getActivity(), BatteryCheckService.class);
                    getActivity().startService(i);
                    //베터리 잔량 띄우는 리시버 등록
                    getActivity().registerReceiver(mBatteryRecv, new IntentFilter(BatteryCheckService.BROADCAST_ACTION));
                }

                mFabSendToFb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (PreferencesUtil.getBoolPreferneces(getContext(), "isServiceRunning") == true) {
                            showWarnDialog();
                        } else {
                            if (FormValidator.isValidMsg(mEditMsg.getText().toString()) == false) {
                                Toast.makeText(getActivity(), "메시지는 140자 이하로 전송 할 수 있습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                PreferencesUtil.setIntPreferences(getActivity(), "bty_perc", Integer.parseInt(mSelectedPercentage));
                                PreferencesUtil.setPreferences(getActivity(), "bty_msg", mEditMsg.getText().toString());
                                PreferencesUtil.setIntPreferences(getActivity(), "service_type", TYPE_NEW);
                                Intent intent = new Intent(getActivity(), ActivitySelectContacts.class);
                                startActivity(intent);
                            }
                        }
                    }
                });

                // set listener
                crystalRangeSeekbar1.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
                    @Override
                    public void valueChanged(Number value) {
                        txtMin.setText(String.valueOf(value) + "%");
                        mSelectedPercentage = String.valueOf(value);
                    }
                });

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getActivity(), deniedPermissions.toString() + "퍼미션이 거절되었습니다. [설정]>[권한}으로 가셔서 권한설정을 하세요.", Toast.LENGTH_SHORT).show();

                mFabSendToFb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), "퍼미션이 거절되었습니다. [설정]>[권한}으로 가셔서 권한설정을 하세요.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        new TedPermission(getContext()).setPermissionListener(listener).setDeniedMessage("퍼미션 거부시 서비스 이용이 불가합니다. [설정]>[권한}으로 가셔서 권한설정을 하세요.").setPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE).check();
    }

    /**
     * Init the fragment
     */
    private void initDataList(View view) {

        fragmentContainer = (FrameLayout) view.findViewById(R.id.layout_list);
        layout_blank = (LinearLayout) view.findViewById(R.id.layout_blank);
        recyclerView = (RecyclerView) view.findViewById(R.id.list_recycler_view);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final int pos) {
                if (PreferencesUtil.getBoolPreferneces(getContext(), "isServiceRunning") == true) {
                    showWarnDialog();
                } else {
                    TimeCalculator calculator = new TimeCalculator();

                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getContext());
                    alert_confirm.setMessage(Html.fromHtml("<strong><font color=#ff0000>" + calculator.calculateTime(userDatas.get(pos).getmInsertDate()) + "</font></strong>" + "에 " + "<strong><font color=#ff0000>" + userDatas.get(pos).getmBatteryPerc() + "%</font></strong>때, " + "<strong><font color=#ff0000>" + userDatas.get(pos).getmMessage() + "</font></strong>" + "라 전송했던 셋팅으로 서비스를 시작합니다."))
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PreferencesUtil.setIntPreferences(getActivity(), "bty_perc", userDatas.get(pos).getmBatteryPerc());
                                    PreferencesUtil.setPreferences(getActivity(), "bty_msg", userDatas.get(pos).getmMessage());
                                    PreferencesUtil.setIntPreferences(getActivity(), "service_type", TYPE_DATA);
                                    Intent intent = new Intent(getActivity(), ActivitySelectContacts.class);
                                    startActivity(intent);
                                }
                            }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });

                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                }
            }
        }));
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        userDatabase = mFbDb.getReference("willDatas/" + mFbUser.getUid());

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userDatas = new ArrayList<UserSettingProfile>();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    UserSettingProfile userData = dataSnapshot1.getValue(UserSettingProfile.class);
                    userDatas.add(userData);
                }

                if (userDatas.size() == 0) {
                    layout_blank.setVisibility(View.VISIBLE);
                    Snackbar.make(recyclerView, "셋팅 목록이 비었습니다.", Snackbar.LENGTH_SHORT).show();
                } else {
                    layout_blank.setVisibility(View.INVISIBLE);
                }

                Collections.sort(userDatas, new Comparator<UserSettingProfile>() {
                    @Override
                    public int compare(UserSettingProfile o1, UserSettingProfile o2) {
                        if (o1.getmInsertDate() == null || o2.getmInsertDate() == null) {
                            return 0;
                        }
                        return (o1.getmInsertDate().getTime() > o1.getmInsertDate().getTime() ? 1 : -1);
                    }
                });

                for (int i = 0; i < userDatas.size(); i++) {
                    System.out.println(userDatas.get(i).getmBatteryPerc() + " " + userDatas.get(i).getmMessage() + "" + userDatas.get(i).getmInsertDate());
                }

                DataListAdapter adapter = new DataListAdapter(userDatas);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //서비스의 브로드캐스트 리시버로부터 호출받을 시 실행되는 메소드
    private BroadcastReceiver mBatteryRecv = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mCurPercentage = intent.getIntExtra("percentage", 0);

            if (mCurPercentage <= 20) {
                crystalRangeSeekbar1.setMaxValue(mCurPercentage);
                txtMax.setText(String.valueOf(mCurPercentage) + "%");
                batteryProgress.setProgress(mCurPercentage);
                batteryProgress.setTextColor(Color.parseColor("#ff0000"));
                batteryProgress.setUnfinishedStrokeColor(Color.parseColor("#ff0000"));
            } else {
                if (20 < mCurPercentage && mCurPercentage <= 60) {
                    batteryProgress.setTextColor(Color.parseColor("#ff8000"));
                } else {
                    batteryProgress.setTextColor(Color.parseColor("#0080ff"));
                }
                batteryProgress.setUnfinishedStrokeColor(Color.parseColor("#ff6666"));
                crystalRangeSeekbar1.setMaxValue(20);
                txtMax.setText("20%");
                batteryProgress.setProgress(mCurPercentage);
            }
        }
    };

    private void showWarnDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setMessage("이미 서비스를 시작한 상태입니다.\n 서비스를 종료하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), WillService.class);
                        getContext().stopService(intent);
                        PreferencesUtil.setBoolPreferences(getContext(), "isServiceRunning", false);
                        Toast.makeText(getContext(), "서비스를 종료하였습니다. \n새로운 서비스를 시작해보세요!!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        PreferencesUtil.setBoolPreferences(getContext(), "isServiceRunning", true);
                        Toast.makeText(getContext(), "서비스 종료 까지 새로운 서비스를 시작 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * Refresh
     */
    public void refresh() {
        if (getArguments().getInt("index", 0) > 0 && recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    /**
     * Called when a fragment will be displayed
     */
    public void willBeDisplayed() {
        // Do what you want here, for example animate the content
        if (fragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            fragmentContainer.startAnimation(fadeIn);
        } else if (linear_service != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            linear_service.startAnimation(fadeIn);
        }
    }

    /**
     * Called when a fragment will be hidden
     */
    public void willBeHidden() {
        if (fragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
            fragmentContainer.startAnimation(fadeOut);
        } else if (linear_service != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
            linear_service.startAnimation(fadeIn);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("TAG", "destroy");

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBatteryRecv);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String[] arr = mListPercentages.get(position).split("%");
        mSelectedPercentage = arr[0];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
