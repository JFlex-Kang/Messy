package devicewills.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cracking.jflex.devicewilly.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.melnykov.fab.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import devicewills.adapters.ContactListAdapter;
import devicewills.objects.ContactInfo;
import devicewills.objects.UserSettingProfile;
import devicewills.services.WillService;
import devicewills.utils.PreferencesUtil;

/**
 * Created by Dell on 2017-05-23.
 */

public class ActivitySelectContacts extends AppCompatActivity {

    private final int TYPE_NEW = 0;
    private Animation fadeIn;

    private List<ContactInfo> mContactList;
    private ListView listview;
    private ContactListAdapter contactListAdapter;
    private RelativeLayout layout_conatcts;
    private FloatingActionButton fab_start_service;
    private SearchView search_contacts;

    private FirebaseAuth mFbAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFbDb;
    private FirebaseUser mFbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);

        listview = (ListView) findViewById(R.id.list_contaccts);
        contactListAdapter = new ContactListAdapter();

        layout_conatcts = (RelativeLayout) findViewById(R.id.layout_contacts);

        fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        layout_conatcts.startAnimation(fadeIn);

        fab_start_service = (FloatingActionButton) findViewById(R.id.fab_start_service);
        fab_start_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ContactInfo> contacts = new ArrayList<ContactInfo>();

                for (ContactInfo info : contactListAdapter.getList()) {
                    if (info.isChecked() == true) {
                        contacts.add(new ContactInfo(info.getmContactName(), info.getmContactNum()));
                    }
                }

                if (contacts.size() > 0) {

                    if (PreferencesUtil.getArrayListPreferendes(ActivitySelectContacts.this) != null) {

                        PreferencesUtil.delArrPreferences(ActivitySelectContacts.this);
                        for (int i = 0; i < contacts.size(); i++) {
                            PreferencesUtil.addArrPreference(ActivitySelectContacts.this, contacts.get(i));
                        }
                    } else {
                        for (int i = 0; i < contacts.size(); i++) {
                            PreferencesUtil.addArrPreference(ActivitySelectContacts.this, contacts.get(i));
                        }
                    }

                    //베터리 측정 서비스 시작
                    Intent i = new Intent(ActivitySelectContacts.this, WillService.class);
                    startService(i);

                    if (PreferencesUtil.getIntPreferences(getApplicationContext(), "service_type") == TYPE_NEW) {
                        //입력 정보를 db 안에 푸시함
                        UserSettingProfile profile = new UserSettingProfile(PreferencesUtil.getIntPreferences(ActivitySelectContacts.this, "bty_perc"), PreferencesUtil.getPreferences(ActivitySelectContacts.this, "bty_msg"), getCurrentTime());
                        mFbDb.getReference("willDatas/" + mFbUser.getUid()).push().setValue(profile).addOnSuccessListener(ActivitySelectContacts.this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        });
                    } else {
                        finish();
                    }

                    PreferencesUtil.setBoolPreferences(ActivitySelectContacts.this, "isServiceRunning", true);
                    Toast.makeText(ActivitySelectContacts.this, "서비스를 시작합니다.\n디바이스 베터리가 " + PreferencesUtil.getIntPreferences(ActivitySelectContacts.this, "bty_perc") + "%가 되었을 때 메시지를 보냅니다.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(ActivitySelectContacts.this, "보낼 대상을 선택해주세요", Toast.LENGTH_SHORT).show();
                }

            }
        });

        search_contacts = (SearchView) findViewById(R.id.search_contacts);
        search_contacts.setQueryHint("Search Contact");

        search_contacts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactListAdapter.filter(newText.toString().trim());
                return false;
            }
        });

        mFbAuth = FirebaseAuth.getInstance();
        mFbDb = FirebaseDatabase.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFbUser = firebaseAuth.getCurrentUser();
                if (mFbUser != null) {
                    //유저정보 얻어오기 성공
                } else {
                    startActivity(new Intent(ActivitySelectContacts.this, ActivitySignIn.class));
                    finish();
                    return;
                }
            }
        };

        getContacts();
    }

    private void getContacts() {
        mContactList = new ArrayList();
        ContactInfo info;
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    info = new ContactInfo();
                    info.setmContactName(name);

                    Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (phoneCursor.moveToNext()) {
                        String phoneNum = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        info.setmContactNum(phoneNum);
                        contactListAdapter.addItem(name, phoneNum);
                    }

                    phoneCursor.close();
                    mContactList.add(info);
                }
            }
            contactListAdapter.initSearch();
            listview.setAdapter(contactListAdapter);
        }
    }

    private Date getCurrentTime() {
        try {
            Calendar c = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String formatDate = df.format(c.getTime());
            Date stringToDate = df.parse(formatDate);

            return stringToDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
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

}
