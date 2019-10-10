package org.rdengine.http.upload;// package org.rdengine.http.upload;
//
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.Map;
// import java.util.Set;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.Future;
// import java.util.concurrent.TimeUnit;
//
// import org.json.JSONArray;
// import org.json.JSONObject;
// import org.rdengine.runtime.PreferenceHelper;
//
// import com.motion.android.logic.UserMgr;
//
// import android.text.TextUtils;
//
// import okhttp3.OkHttpClient;
//
/// **
// * Created by CCCMAX on 18/6/19.
// */
//
// public class UploadManager
// {
// public static final String SAVEKEY = "UploadManager_";
//
// private OkHttpClient mClient;
//
// private int mPoolSize = 20;
//
// // 将执行结果保存在future变量中
// private Map<String, Future> mFutureMap;
//
// private ExecutorService mExecutor;
//
// /** 任务列表 */
// private Map<String, UploadTask> mCurrentTaskList;
//
// static UploadManager manager;
//
// /**
// * 方法加锁，防止多线程操作时出现多个实例
// */
//
// private static synchronized void init()
// {
// if (manager == null)
// {
// manager = new UploadManager();
// }
// }
//
// /**
// * 获得当前对象实例
// *
// * @return 当前实例对象
// */
//
// public final static UploadManager getInstance()
// {
// if (manager == null)
// {
// init();
// }
// return manager;
// }
//
// public UploadManager()
// {
// initOkhttpClient();
// // 初始化线程池
// mExecutor = Executors.newFixedThreadPool(mPoolSize);
// mFutureMap = new HashMap<>();
// mCurrentTaskList = new HashMap<>();
//
// loadTaskList();// 从本地记录中加载任务，但是重新执行需要走addUploadTask,并且重新设置任务回调
// }
//
// /**
// * 初始化okhttp
// */
//
// private void initOkhttpClient()
// {
// OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
// okBuilder.connectTimeout(10, TimeUnit.SECONDS);
// okBuilder.readTimeout(10, TimeUnit.SECONDS);
// okBuilder.writeTimeout(60, TimeUnit.SECONDS);
// mClient = okBuilder.build();
// }
//
// /**
// * 添加上传任务 并执行
// *
// * @param uploadTask
// */
//
// public void addUploadTask(UploadTask uploadTask)
// {
// if (uploadTask != null && !isUploading(uploadTask))
// {
// uploadTask.setClient(mClient);
// uploadTask.setUploadStatus(UploadStatus.UPLOAD_STATUS_INIT);
// // 保存上传task列表
// mCurrentTaskList.put(uploadTask.getId(), uploadTask);
// Future future = mExecutor.submit(uploadTask);
// mFutureMap.put(uploadTask.getId(), future);
// }
//
// saveTaskList();
// }
//
// private boolean isUploading(UploadTask task)
// {
// if (task != null)
// {
// if (task.getUploadStatus() == UploadStatus.UPLOAD_STATUS_UPLOADING)
// {
// return true;
// }
// }
// return false;
// }
//
// /**
// * 暂停上传任务
// *
// * @param id
// * 任务id
// */
// public void pause(String id)
// {
// try
// {
// UploadTask task = getUploadTask(id);
// if (task != null)
// {
// task.setUploadStatus(UploadStatus.UPLOAD_STATUS_PAUSE);
// }
// Future future = mFutureMap.get(id);
// if (future != null)
// {
// future.cancel(true);// 中断线程执行
// }
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
//
// saveTaskList();
// }
//
// /**
// * 重新开始已经暂停的上传任务
// *
// * @param id
// * 任务id
// */
// public void resume(String id)
// {
// UploadTask task = getUploadTask(id);
// if (task != null)
// {
// addUploadTask(task);// 重新放回线程池中执行
// }
// }
//
// /**
// * 取消上传任务(同时会删除已经上传的文件，和清空数据库缓存)
// *
// * @param id
// * 任务id
// */
// public void cancel(String id)
// {
// try
// {
// UploadTask task = getUploadTask(id);
// if (task != null)
// {
// mCurrentTaskList.remove(id);
// mFutureMap.remove(id);
// task.cancel();
// }
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
// saveTaskList();
// }
//
// /**
// * 任务完成 清理任务
// *
// * @param id
// */
// protected void taskSuccess(String id)
// {
// try
// {
// UploadTask task = getUploadTask(id);
// if (task != null)
// {
// mCurrentTaskList.remove(id);
// mFutureMap.remove(id);
// }
// } catch (Exception ex)
// {
// }
// saveTaskList();
// }
//
// /**
// * 获得指定的task
// *
// * @param id
// * task id
// * @return
// */
//
// public UploadTask getUploadTask(String id)
// {
// try
// {
// UploadTask currTask = mCurrentTaskList.get(id);
// return currTask;
// } catch (Exception ex)
// {
// }
// return null;
// }
//
// private String getSaveKey()
// {
// return SAVEKEY + UserMgr.getInstance().getUid() + "_";
// }
//
// private void loadTaskList()
// {
// try
// {
// String data = PreferenceHelper.ins().getStringShareData(getSaveKey(), "");
// if(TextUtils.isEmpty(data))
// return;
// JSONArray ja = new JSONArray(data);
// if (ja != null && ja.length() > 0)
// {
// for (int i = 0; i < ja.length(); i++)
// {
// String id = ja.getString(i);
// if (!TextUtils.isEmpty(id))
// {
// try
// {
// String item_data = PreferenceHelper.ins().getStringShareData(UploadTask.getSaveKey() + id,
// "");
// if (!TextUtils.isEmpty(item_data))
// {
// JSONObject json = new JSONObject(item_data);
// UploadTask task = new UploadTask.Builder().setId(id)
// .setFilePath(json.optString("filePath"))
// .setUploadStatus(json.optInt("uploadStatus")).setChunck(json.optInt("chunck"))
// .build();
// if (!TextUtils.isEmpty(task.getFilePath()) && !TextUtils.isEmpty(task.getUrl())
// && task.getUploadStatus() != UploadStatus.UPLOAD_STATUS_SUCCESS
// && task.getUploadStatus() != UploadStatus.UPLOAD_STATUS_CANCEL)
// {
// task.setUploadStatus(UploadStatus.UPLOAD_STATUS_PAUSE);
// mCurrentTaskList.put(task.getId(), task);// 恢复到任务队列中
// }
// }
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
// }
// }
// }
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
// }
//
// private void saveTaskList()
// {
// // 保存任务ID列表 ， 任务详情在任务中自动保存
// try
// {
// JSONArray ja = new JSONArray();
// Iterator iter = mCurrentTaskList.entrySet().iterator();
// while (iter.hasNext())
// {
// Map.Entry entry = (Map.Entry) iter.next();
// String id = (String) entry.getKey();
// // UploadTask task = (UploadTask) entry.getValue();
// ja.put(id);
// }
//
// PreferenceHelper.ins().storeShareStringData(getSaveKey(), ja.toString());
// PreferenceHelper.ins().commit();
// } catch (Exception ex)
// {
// ex.printStackTrace();
// }
//
// }
//
// public int getTaskCount()
// {
// return mCurrentTaskList.size();
// }
//
// public String[] getTaskIdArray()
// {
// Set<String> keyset = mCurrentTaskList.keySet();
// String[] ids = new String[keyset.size()];
// ids = keyset.toArray(ids);
// return ids;
// }
//
// }
