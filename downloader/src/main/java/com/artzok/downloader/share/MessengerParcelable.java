package com.artzok.downloader.share;

import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * name：赵坤 on 2018/12/17 16:02
 * email：zhaokun@ziipin.com
 */
public class MessengerParcelable implements Parcelable {
    private Messenger mMessenger;

    public MessengerParcelable(Messenger messenger) {
        mMessenger = messenger;
    }

    public MessengerParcelable(Parcel in) {
        this.mMessenger = Messenger.readMessengerOrNullFromParcel(in);
    }

    public Messenger getMessenger() {
        return mMessenger;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Messenger.writeMessengerOrNullToParcel(mMessenger, dest);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MessengerParcelable> CREATOR =
            new Parcelable.Creator<MessengerParcelable>() {
                @Override
                public MessengerParcelable createFromParcel(Parcel source) {
                    return new MessengerParcelable(source);
                }

                @Override
                public MessengerParcelable[] newArray(int size) {
                    return new MessengerParcelable[size];
                }
            };
}
