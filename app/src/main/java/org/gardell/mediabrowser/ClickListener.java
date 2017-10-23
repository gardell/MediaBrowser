package org.gardell.mediabrowser;

import android.view.View;

public interface ClickListener<T> {
    void onClick(T value, View view);
}
