package org.rdengine.http;

import org.json.JSONObject;

public interface JSONResponse
{
    void onJsonResponse(JSONObject json, int errCode, String msg, boolean cached);
}
