package com.newnd.autoganarator;

import org.json.JSONObject;

public interface getDataListner {

    void onsuccess();

    void onUpdate(String url);

    void onRedirect(String url);

    void reloadActivity();

    void ongetExtradata(JSONObject extraData);
}
