package org.rdengine.http.upload;// package org.rdengine.http.upload;
//
// import java.io.File;
// import java.io.IOException;
// import java.net.URLEncoder;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.Map;
// import java.util.concurrent.atomic.AtomicInteger;
//
// import org.json.JSONObject;
// import org.rdengine.http.HttpParam;
// import org.rdengine.http.HttpUtil;
// import org.rdengine.log.DLOG;
// import org.rdengine.net.Network;
// import org.rdengine.runtime.PreferenceHelper;
// import org.rdengine.util.FileUtils;
//
// import com.motion.android.logic.UserMgr;
// import com.motion.android.logic.api.API_Serviceinfo;
// import com.motion.android.logic.bean.ShortVideoBean;
//
// import android.os.Handler;
// import android.os.Looper;
// import android.os.Message;
// import android.text.TextUtils;
//
// import okhttp3.Headers;
// import okhttp3.MediaType;
// import okhttp3.OkHttpClient;
// import okhttp3.Request;
// import okhttp3.RequestBody;
// import okhttp3.Response;
//
/// **
// * Created by CCCMAX on 18/6/19.
// */
//
// public class UploadTask implements Runnable
// {
// public static final String SAVEKEY = "UploadTask_";
//
// public static final String URL_UPLOAD = API_Serviceinfo.host_video_upload + "resumable_upload?videoId=";
// public static final String URL_CHECK = API_Serviceinfo.host_video_upload + "getStatus";
//
// private OkHttpClient mClient;
// private UploadTaskListener mListener;
//
// private Builder mBuilder;
// private String id;// task id
// private String filePath; // File name when saving
// private int uploadStatus;
// private AtomicInteger chunck = new AtomicInteger(0);
// private int chuncks;// 流块 从0开始，chuncks是总数
//
// final int blockLength = 1024 * 1024;
//
// private float progress;
//
// private int errorCode;
//
// private UploadTask(Builder builder)
// {
// mBuilder = builder;
// mClient = new OkHttpClient();
// this.id = mBuilder.id;
// this.filePath = mBuilder.filePath;
// this.uploadStatus = mBuilder.uploadStatus;
// this.chunck = new AtomicInteger(mBuilder.chunck);
// this.setmListener(mBuilder.listener);
//
// try
// {
// File file = new File(filePath);
// if (file.length() % blockLength == 0)
// {
// chuncks = (int) file.length() / blockLength;
// } else
// {
// chuncks = (int) file.length() / blockLength + 1;
//
// }
// } catch (Exception ex)
// {
// }
//
// }
//
// public interface ERRCODE
// {
// int FILE_NOT_EXISTS = -1;
// int CHECK_RECIVE_ERROR = -2;
// int READ_FILE_ERROR = -3;
// int NET_ERROR = -4;
// int OTHER = -5;
// }
//
// @Override
// public void run()
// {
// try
// {
// uploadStatus = UploadStatus.UPLOAD_STATUS_START;
// onCallBack();
//
// File file = new File(filePath);
// if (!file.exists() || !file.isFile())
// {
// errorCode = ERRCODE.FILE_NOT_EXISTS;
// throw new IOException("文件不存在 paht=" + filePath);
// }
//
// if (file.length() % blockLength == 0)
// {
// chuncks = (int) file.length() / blockLength;
// } else
// {
// chuncks = (int) file.length() / blockLength + 1;
//
// }
//
// if (chunck.get() != 0)
// {
// // 断点续传 ，需要向服务器检测传输进度
// int reciveSize = 0;
// for (int i = 0; i <= 3; i++)
// {
// reciveSize = checkReciveSize(String.valueOf(id));
// if (reciveSize >= 0)
// break;
// Thread.sleep(500);
// }
//
// if (reciveSize >= 0)
// {
// // 最近的节点，已接受长度/分片长度
// float service_chucnk = 1.0F * reciveSize / blockLength;
// chunck.set((int) service_chucnk);
// } else
// {
// // 向服务器请求最近节点失败
// errorCode = ERRCODE.CHECK_RECIVE_ERROR;
// throw new IOException("向服务器请求已传数据长度失败");
// }
// }
//
// while (chunck.get() <= chuncks - 1 && uploadStatus != UploadStatus.UPLOAD_STATUS_PAUSE
// && uploadStatus != UploadStatus.UPLOAD_STATUS_CANCEL
// && uploadStatus != UploadStatus.UPLOAD_STATUS_ERROR)
// {
//
// // 从文件什么位置开始
// long offsize = chunck.get() * blockLength;
// // 上传的片段
// final byte[] mBlock = FileUtils.getBlock(offsize, file, blockLength);
// if (mBlock == null || mBlock.length == 0)
// {
// errorCode = ERRCODE.READ_FILE_ERROR;
// throw new IOException(
// "文件分片获取失败, offsize=" + offsize + " blockLength=" + blockLength + " paht=" + filePath);
// }
//
// // 任务状态
// uploadStatus = UploadStatus.UPLOAD_STATUS_UPLOADING;
//
// String fileName = URLEncoder.encode(getFileNameFromUrl(filePath), "UTF-8");
// Map<String, String> headerMap = new HashMap<String, String>();
// headerMap.put("Content-Disposition", "attachment; filename=" + fileName);
// headerMap.put("Content-Type", "application/octet-stream");
// // 待上传文件字节范围,如第一片段bytes 0-51200/511920,最后一个片段bytes 460809-511919/511920(注:文件第一个字节标号为0,最后一个字节标号为n-1,其中n为文件字节大小)
// headerMap.put("X-Content-Range",
// "bytes " + offsize + "-" + (offsize + mBlock.length - 1) + "/" + file.length());
// headerMap.put("Session-ID", String.valueOf(id));
// headerMap.put("Content-Length", String.valueOf(mBlock.length));
//
// RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), mBlock);
//
// Request request = new Request.Builder().localPath(getUrl()).headers(makeHeader(headerMap)).post(requestBody)
// .build();
//
// Response response = null;
// for (int i = 0; i <= 3; i++)
// {
// response = mClient.newCall(request).execute();
// if (response.isSuccessful())
// break;
// Thread.sleep(500);// 等待后 重试请求
// }
//
// if (response.isSuccessful())
// {
// onCallBack();
// chunck.set(chunck.get() + 1);
// } else
// {
// uploadStatus = UploadStatus.UPLOAD_STATUS_ERROR;
// onCallBack();
// }
// }
//
// if (chunck.get() >= chuncks)
// {
// uploadStatus = UploadStatus.UPLOAD_STATUS_SUCCESS;
// onCallBack();
// }
//
// } catch (Exception e)
// {
// errorCode = ERRCODE.OTHER;
// if (Network.getNetworkState() != Network.NetworkMode.NET_WORK_OK)
// errorCode = ERRCODE.NET_ERROR;
//
// uploadStatus = UploadStatus.UPLOAD_STATUS_ERROR;
// onCallBack();
// e.printStackTrace();
// }
// }
//
// /**
// * 向服务器检测传输进度
// *
// * @param sessionId
// * @return 已经接受的数据大小
// */
// private int checkReciveSize(String sessionId)
// {
// try
// {
// String localPath = URL_CHECK;
// HttpParam hp = new HttpParam();
// hp.put("sessionId", sessionId);
// JSONObject json = HttpUtil.getSync(localPath, hp);
// if (json != null)
// {
// // 网络请求成功
// int code = json.optInt("code");
// JSONObject j_data = json.optJSONObject("data");
// if (code == 0 && j_data != null && j_data.has("reciveSize"))
// {
// // 解析数据成功
// int reciveSize = j_data.optInt("reciveSize");
// return reciveSize;
// }
// return -2;
// }
// } catch (Exception ex)
// {
// }
// return -1;
//
// }
//
// /**
// * 删除数据库文件和已经上传的文件
// */
// protected void cancel()
// {
// uploadStatus = UploadStatus.UPLOAD_STATUS_CANCEL;
// onCallBack();
// }
//
// /**
// * 分发回调事件到ui层
// */
// private void onCallBack()
// {
// if (uploadStatus == UploadStatus.UPLOAD_STATUS_CANCEL || uploadStatus == UploadStatus.UPLOAD_STATUS_SUCCESS)
// cleanLocal();
// else saveLocal();
//
// mHandler.sendEmptyMessage(uploadStatus);
//
// }
//
// public void refreshCallback()
// {
// onCallBack();
// }
//
// Handler mHandler = new Handler(Looper.getMainLooper())
// {
// @Override
// public void handleMessage(Message msg)
// {
// try
// {
// int code = msg.what;
// int _chunck = chunck.get();
// DLOG.d("uploadtask", "status=" + code + " chunck=" + _chunck);
// switch (code)
// {
// case UploadStatus.UPLOAD_STATUS_ERROR :// 上传失败
// {
// if (mListener != null)
// mListener.onError(UploadTask.this, errorCode, _chunck);
// }
// break;
// case UploadStatus.UPLOAD_STATUS_START : // 上传线程启动（文件检测、寻找断点、网络尝试连接）
// {
// if (mListener != null)
// mListener.onStart(UploadTask.this, getUploadProgress(), _chunck);
// }
// case UploadStatus.UPLOAD_STATUS_UPLOADING :// 正在上传（每次上传分片成功后）
// {
// if (mListener != null)
// mListener.onUploading(UploadTask.this, getUploadProgress(), _chunck);
// }
// break;
// case UploadStatus.UPLOAD_STATUS_PAUSE :// 暂停上传
// {
// if (mListener != null)
// mListener.onPause(UploadTask.this);
// }
// break;
// case UploadStatus.UPLOAD_STATUS_CANCEL :// 取消任务
// {
// if (mListener != null)
// mListener.onCancel(UploadTask.this);
// }
// break;
// case UploadStatus.UPLOAD_STATUS_SUCCESS :// 上传成功
// {
// UploadManager.getInstance().taskSuccess(getId());
// if (mListener != null)
// mListener.onUploadSuccess(UploadTask.this);
// }
// break;
// }
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
//
// }
// };
//
// private float makeUploadProgress()
// {
// int c = chunck.get();
// if (c <= 0)
// {
// return 0;
// } else if (c >= chuncks - 1)
// {
// return 1;
// }
// return c * 1.0F / (chuncks * 1.0F - 1);
// }
//
// public float getUploadProgress()
// {
// progress = makeUploadProgress();
// return progress;
// }
//
// private String getFileNameFromUrl(String localPath)
// {
// try
// {
// if (!TextUtils.isEmpty(localPath) && localPath.contains("/"))
// {
// return localPath.substring(localPath.lastIndexOf("/") + 1);
// }
// } catch (Exception ex)
// {
// }
// return System.currentTimeMillis() + "";
// }
//
// public void setClient(OkHttpClient mClient)
// {
// this.mClient = mClient;
// }
//
// public Builder getBuilder()
// {
// return mBuilder;
// }
//
// public void setBuilder(Builder builder)
// {
// this.mBuilder = builder;
// }
//
// public String getId()
// {
// return id;
// }
//
// public String getUrl()
// {
// return URL_UPLOAD + id.trim();
// }
//
// public String getFilePath()
// {
// return filePath;
// }
//
// public void setUploadStatus(int uploadStatus)
// {
// this.uploadStatus = uploadStatus;
// }
//
// public int getUploadStatus()
// {
// return uploadStatus;
// }
//
// public void setmListener(UploadTaskListener mListener)
// {
// this.mListener = mListener;
// }
//
// public static class Builder
// {
//
// /** ID */
// private String id;// task id
//
// /** 上传地址 */
// private String localPath;
//
// /** 文件本地路径 */
// private String filePath;
//
// private int uploadStatus = UploadStatus.UPLOAD_STATUS_PAUSE;
//
// /** 分块 ， 第几块 从0开始 */
// private int chunck;
//
// private UploadTaskListener listener;
//
// /**
// * 作为上传task开始、删除、停止的key值，如果为空则默认是url
// *
// * @param id
// * @return
// */
//
// public Builder setId(String id)
// {
// this.id = id;
// return this;
// }
//
// /**
// * 设置上传状态
// *
// * @param uploadStatus
// * @return
// */
//
// public Builder setUploadStatus(int uploadStatus)
// {
// this.uploadStatus = uploadStatus;
// return this;
// }
//
// /**
// * 第几块
// *
// * @param chunck
// * @return
// */
//
// public Builder setChunck(int chunck)
// {
// this.chunck = chunck;
// return this;
// }
//
// /**
// * 设置文件路径
// *
// * @param filePath
// * @return
// */
//
// public Builder setFilePath(String filePath)
// {
// this.filePath = filePath;
// return this;
// }
//
// /**
// * 设置上传回调
// *
// * @param listener
// * @return
// */
//
// public Builder setListener(UploadTaskListener listener)
// {
// this.listener = listener;
// return this;
// }
//
// public UploadTask build()
// {
// return new UploadTask(this);
// }
// }
//
// private Headers makeHeader(Map<String, String> headerMap)
// {
// if (headerMap != null && headerMap.size() > 0)
// {
// Headers.Builder builder = new Headers.Builder();
// Iterator iter = headerMap.entrySet().iterator();
// while (iter.hasNext())
// {
// try
// {
// Map.Entry entry = (Map.Entry) iter.next();
// String key = (String) entry.getKey();
// String val = (String) entry.getValue();
// builder.add(key, val);
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
// }
// Headers headers = builder.build();
// return headers;
// }
// return null;
// }
//
// public JSONObject toJson()
// {
// JSONObject json = new JSONObject();
// try
// {
// json.put("id", id);
// json.put("filePath", filePath);
// json.put("uploadStatus", uploadStatus);
// json.put("chunck", chunck);
// } catch (Exception ex)
// {
// }
// return json;
// }
//
// public static String getSaveKey()
// {
// return SAVEKEY + UserMgr.getInstance().getUid() + "_";
// }
//
// public void saveLocal()
// {
// try
// {
// PreferenceHelper.ins().storeShareStringData(getSaveKey() + id, toJson().toString());
// PreferenceHelper.ins().commit();
// } catch (Exception ex)
// {
// }
// }
//
// public void cleanLocal()
// {
// PreferenceHelper.ins().clearDatasWithTag(getSaveKey() + id);
// PreferenceHelper.ins().clearDatasWithTag(getSaveKey() + id + "_svb");
// }
//
// /** 存视频信息到本地 */
// public void saveShortVideo(ShortVideoBean svb)
// {
// try
// {
// JSONObject json = svb.toJson();
// PreferenceHelper.ins().storeShareStringData(getSaveKey() + id + "_svb", json.toString());
// PreferenceHelper.ins().commit();
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
// }
//
// public ShortVideoBean loadShortVideo()
// {
// ShortVideoBean svb = null;
// try
// {
// String str = PreferenceHelper.ins().getStringShareData(getSaveKey() + id + "_svb", "");
// if (!TextUtils.isEmpty(str))
// {
// JSONObject json = new JSONObject(str);
// if (json != null && json.length() > 0)
// {
// svb = new ShortVideoBean().jsonParse(json);
// }
// }
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
// return svb;
// }
//
// }