package devicewills.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cracking.jflex.devicewilly.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import devicewills.objects.ContactInfo;

/**
 * Created by Dell on 2017-05-23.
 */

public class ContactListAdapter extends BaseAdapter {

    private ArrayList<ContactInfo> contactInfos = new ArrayList<ContactInfo>();
    private ArrayList<ContactInfo> conList = new ArrayList<ContactInfo>();

    public ContactListAdapter() {

    }

    @Override
    public int getCount() {
        return contactInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return contactInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context mContext = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_contact_list, parent, false);
        }

        ImageView contactImg = (ImageView) convertView.findViewById(R.id.ivContactImage);
        TextView contactName = (TextView) convertView.findViewById(R.id.tvContactName);
        TextView contactNum = (TextView) convertView.findViewById(R.id.tvPhoneNumber);
        CheckBox contactChk = (CheckBox) convertView.findViewById(R.id.chkBoxContact);

        ContactInfo info = contactInfos.get(position);
        contactName.setText(info.getmContactName());
        contactNum.setText(info.getmContactNum());
        contactChk.setChecked(info.isChecked());
        contactChk.setTag(conList.get(pos));
        contactChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox chk = (CheckBox) v;
                ContactInfo info = (ContactInfo) chk.getTag();

                info.setChecked(chk.isChecked());
                conList.get(pos).setChecked(chk.isChecked());

            }
        });

        return convertView;
    }

    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());

        contactInfos.clear();
        if (charText.length() == 0) {
            contactInfos.addAll(conList);
        } else {
            for (ContactInfo info : conList) {
                if (charText.length() != 0 && info.getmContactName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    contactInfos.add(info);
                } else if (charText.length() != 0 && info.getmContactNum().toLowerCase(Locale.getDefault()).contains(charText)) {
                    contactInfos.add(info);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void addItem(String name, String num) {
        ContactInfo info = new ContactInfo();

        info.setmContactName(name);
        info.setmContactNum(num);

        contactInfos.add(info);
    }

    public ArrayList<ContactInfo> getList() {
        return conList;
    }

    public void initSearch() {
        conList.addAll(contactInfos);
    }
}
