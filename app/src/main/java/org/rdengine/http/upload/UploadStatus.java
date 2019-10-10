package org.rdengine.http.upload;

/**
 * Created by CCCMAX on 18/6/19.
 */

public class UploadStatus
{
    public static final int UPLOAD_STATUS_INIT = -1;
    public static final int UPLOAD_STATUS_START = 0;
    public static final int UPLOAD_STATUS_UPLOADING = 1;
    public static final int UPLOAD_STATUS_PAUSE = 2;
    public static final int UPLOAD_STATUS_ERROR = 3;
    public static final int UPLOAD_STATUS_CANCEL = 4;
    public static final int UPLOAD_STATUS_SUCCESS = 5;
}