package org.gardell.mediabrowser;

import android.support.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

final class Config {
    static final URL PROGRAMS_URL;

    static {
        try {
            PROGRAMS_URL = new URL("http://api.sr.se/api/v2/programs/?format=json");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    static URL getProgramUrl(int id) {
        try {
            return new URL("http://api.sr.se/api/v2/programs/" + id + "?format=json");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
