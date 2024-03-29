package org.rdengine.util;

//
//import com.android.emailcommon.Logging;
//import com.android.emailcommon.provider.EmailContent.Attachment;
//import com.android.emailcommon.provider.EmailContent.Message;
//import com.android.emailcommon.provider.EmailContent.MessageColumns;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.List;

public class AttachmentUtilities
{
    public static final String AUTHORITY = "com.android.email.attachmentprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String FORMAT_RAW = "RAW";
    public static final String FORMAT_THUMBNAIL = "THUMBNAIL";

    public static class Columns
    {
        public static final String _ID = "_id";
        public static final String DATA = "_data";
        public static final String DISPLAY_NAME = "_display_name";
        public static final String SIZE = "_size";
    }

    /**
     * The MIME type(s) of attachments we're willing to send via attachments. Any attachments may be added via Intents with Intent.ACTION_SEND or ACTION_SEND_MULTIPLE.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_SEND_INTENT_TYPES = new String[]
    { "*/*", };
    /**
     * The MIME type(s) of attachments we're willing to send from the internal UI. NOTE: At the moment it is not possible to open a chooser with a list of filter types, so the chooser is only opened with the first item in the list.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_SEND_UI_TYPES = new String[]
    { "image/*", "video/*", };
    /**
     * The MIME type(s) of attachments we're willing to view.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[]
    { "*/*", };
    /**
     * The MIME type(s) of attachments we're not willing to view.
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[]
    {};
    /**
     * The MIME type(s) of attachments we're willing to download to SD.
     */
    public static final String[] ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[]
    { "*/*", };
    /**
     * The MIME type(s) of attachments we're not willing to download to SD.
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[]
    {};
    /**
     * Filename extensions of attachments we're never willing to download (potential malware). Entries in this list are compared to the end of the lower-cased filename, so they must be lower case, and should not include a "."
     */
    public static final String[] UNACCEPTABLE_ATTACHMENT_EXTENSIONS = new String[]
    {
            // File types that contain malware
            "ade", "adp", "bat", "chm", "cmd", "com", "cpl", "dll", "exe", "hta", "ins", "isp", "jse", "lib", "mde",
            "msc", "msp", "mst", "pif", "scr", "sct", "shb", "sys", "vb", "vbe", "vbs", "vxd", "wsc", "wsf", "wsh",
            // File types of common compression/container formats (again, to
            // avoid malware)
            "zip", "gz", "z", "tar", "tgz", "bz2", };
    /**
     * Filename extensions of attachments that can be installed. Entries in this list are compared to the end of the lower-cased filename, so they must be lower case, and should not include a "."
     */
    public static final String[] INSTALLABLE_ATTACHMENT_EXTENSIONS = new String[]
    { "apk", };
    /**
     * The maximum size of an attachment we're willing to download (either View or Save) Attachments that are base64 encoded (most) will be about 1.375x their actual size so we should probably factor that in. A 5MB attachment will generally be around 6.8MB downloaded but only 5MB saved.
     */
    public static final int MAX_ATTACHMENT_DOWNLOAD_SIZE = (5 * 1024 * 1024);
    /**
     * The maximum size of an attachment we're willing to upload (measured as stored on disk). Attachments that are base64 encoded (most) will be about 1.375x their actual size so we should probably factor that in. A 5MB attachment will generally be around 6.8MB uploaded.
     */
    public static final int MAX_ATTACHMENT_UPLOAD_SIZE = (5 * 1024 * 1024);

    public static Uri getAttachmentUri(long accountId, long id)
    {
        return CONTENT_URI.buildUpon().appendPath(Long.toString(accountId)).appendPath(Long.toString(id))
                .appendPath(FORMAT_RAW).build();
    }

    public static Uri getAttachmentThumbnailUri(long accountId, long id, int width, int height)
    {
        return CONTENT_URI.buildUpon().appendPath(Long.toString(accountId)).appendPath(Long.toString(id))
                .appendPath(FORMAT_THUMBNAIL).appendPath(Integer.toString(width)).appendPath(Integer.toString(height))
                .build();
    }

    /**
     * Return the filename for a given attachment. This should be used by any code that is going to *write* attachments. This does not create or write the file, or even the directories. It simply builds the filename that should be used.
     */
    public static File getAttachmentFilename(Context context, long accountId, long attachmentId)
    {
        return new File(getAttachmentDirectory(context, accountId), Long.toString(attachmentId));
    }

    /**
     * Return the directory for a given attachment. This should be used by any code that is going to *write* attachments. This does not create or write the directory. It simply builds the pathname that should be used.
     */
    public static File getAttachmentDirectory(Context context, long accountId)
    {
        return context.getDatabasePath(accountId + ".db_att");
    }

    /**
     * Helper to convert unknown or unmapped attachments to something useful based on filename extensions. The mime type is inferred based upon the table below. It's not perfect, but it helps.
     * 
     * <pre>
     *                   |---------------------------------------------------------|
     *                   |                  E X T E N S I O N                      |
     *                   |---------------------------------------------------------|
     *                   | .eml        | known(.png) | unknown(.abc) | none        |
     * | M |-----------------------------------------------------------------------|
     * | I | none        | msg/rfc822  | image/png   | app/abc       | app/oct-str |
     * | M |-------------| (always     |             |               |             |
     * | E | app/oct-str |  overrides  |             |               |             |
     * | T |-------------|             |             |-----------------------------|
     * | Y | text/plain  |             |             | text/plain                  |
     * | P |-------------|             |-------------------------------------------|
     * | E | any/type    |             | any/type                                  |
     * |---|-----------------------------------------------------------------------|
     * </pre>
     * 
     * NOTE: Since mime types on Android are case-*sensitive*, return values are always in lower case.
     * 
     * @param fileName
     *            The given filename
     * @param mimeType
     *            The given mime type
     * @return A likely mime type for the attachment
     */
    public static String inferMimeType(final String fileName, final String mimeType)
    {
        String resultType = null;
        String fileExtension = getFilenameExtension(fileName);
        boolean isTextPlain = "text/plain".equalsIgnoreCase(mimeType);

        if ("eml".equals(fileExtension))
        {
            resultType = "message/rfc822";
        } else
        {
            boolean isGenericType = isTextPlain || "application/octet-stream".equalsIgnoreCase(mimeType);
            // If the given mime type is non-empty and non-generic, return it
            if (isGenericType || TextUtils.isEmpty(mimeType))
            {
                if (!TextUtils.isEmpty(fileExtension))
                {
                    // Otherwise, try to find a mime type based upon the file
                    // extension
                    resultType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                    if (TextUtils.isEmpty(resultType))
                    {
                        // Finally, if original mimetype is text/plain, use it;
                        // otherwise synthesize
                        resultType = isTextPlain ? mimeType : "application/" + fileExtension;
                    }
                }
            } else
            {
                resultType = mimeType;
            }
        }

        // No good guess could be made; use an appropriate generic type
        if (TextUtils.isEmpty(resultType))
        {
            resultType = isTextPlain ? "text/plain" : "application/octet-stream";
        }
        return resultType.toLowerCase();
    }

    /**
     * @return mime-type for a {@link Uri}. - Use {@link ContentResolver#getType} for a content: URI. - Use {@link #inferMimeType} for a file: URI. - Otherwise throw {@link IllegalArgumentException}.
     */
    public static String inferMimeTypeForUri(Context context, Uri uri)
    {
        final String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme))
        {
            return context.getContentResolver().getType(uri);
        } else if ("file".equals(scheme))
        {
            return inferMimeType(uri.getLastPathSegment(), "");
        } else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Extract and return filename's extension, converted to lower case, and not including the "."
     * 
     * @return extension, or null if not found (or null/empty filename)
     */
    public static String getFilenameExtension(String fileName)
    {
        String extension = null;
        if (!TextUtils.isEmpty(fileName))
        {
            int lastDot = fileName.lastIndexOf('.');
            if ((lastDot > 0) && (lastDot < fileName.length() - 1))
            {
                extension = fileName.substring(lastDot + 1).toLowerCase();
            }
        }
        return extension;
    }

    /**
     * Resolve attachment id to content URI. Returns the resolved content URI (from the attachment DB) or, if not found, simply returns the incoming value.
     * 
     * @param attachmentUri
     * @return resolved content URI TODO: Throws an SQLite exception on a missing DB file (e.g. unknown URI) instead of just returning the incoming uri, as it should.
     */
    public static Uri resolveAttachmentIdToContentUri(ContentResolver resolver, Uri attachmentUri)
    {
        Cursor c = resolver.query(attachmentUri, new String[]
        { Columns.DATA }, null, null, null);
        if (c != null)
        {
            try
            {
                if (c.moveToFirst())
                {
                    final String strUri = c.getString(0);
                    if (strUri != null)
                    {
                        return Uri.parse(strUri);
                    }
                }
            } finally
            {
                c.close();
            }
        }
        return attachmentUri;
    }

    /**
     * sample: //content://com.android.email.attachmentprovider/1/36/RAW //file:///data/data/com.android.email/databases/1.db_att/36
     * 
     * @param resolver
     * @param attachmentUri
     * @return
     */
    public static Uri resolveFileUriToContentUri(ContentResolver resolver, Uri attachmentUri)
    {

        List<String> segs = attachmentUri.getPathSegments();
        int size = segs.size();
        if (segs != null && size > 1)
        {
            long id = Long.parseLong(segs.get(size - 1).trim());
            String db = segs.get(size - 2);
            if (db != null)
            {
                int c = db.indexOf(".");
                if (c != -1)
                {
                    long accountId = Long.parseLong(db.substring(0, c).trim());
                    return getAttachmentUri(accountId, id);
                }
            }

        }

        return attachmentUri;
    }
}
