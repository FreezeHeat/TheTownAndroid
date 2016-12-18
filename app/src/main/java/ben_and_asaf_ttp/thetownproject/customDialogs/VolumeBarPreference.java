package ben_and_asaf_ttp.thetownproject.customDialogs;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import ben_and_asaf_ttp.thetownproject.R;

public class VolumeBarPreference extends DialogPreference{

    public VolumeBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setDialogLayoutResource(R.id);
        setPositiveButtonText(R.string.general_ok);
        setNegativeButtonText(R.string.general_cancel);
        setDialogIcon(null);
    }
}
