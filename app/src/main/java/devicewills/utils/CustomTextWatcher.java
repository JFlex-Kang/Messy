package devicewills.utils;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;

import com.cracking.jflex.devicewilly.R;

/**
 * Created by 9929k on 2017-04-10.
 */

public class CustomTextWatcher implements TextWatcher{

    private TextInputEditText mInputEditText;
    private TextInputLayout mInputLayout;
    private Context mContext;

    public CustomTextWatcher(Context context, TextInputEditText mInputEditText, TextInputLayout mInputLayout){
        this.mInputEditText = mInputEditText;
        this.mInputLayout = mInputLayout;
        this.mContext = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        switch (this.mInputEditText.getId()){
            case R.id.msg_input_edit:
                if(FormValidator.isValidMsg(mInputEditText.getText().toString())==false){
                    mInputLayout.setError("메시지는 140자 이내로 전송할 수 있습니다.");
                    mInputEditText.requestFocus();
                }else{
                    mInputLayout.setErrorEnabled(false);
                }
                break;
        }
    }
}
