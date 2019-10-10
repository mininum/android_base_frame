package org.rdengine.util;

import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FileUtils
{

    public final static String FILE_EXTENSION_SEPARATOR = ".";

    public static boolean isExistSDCard()
    {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            return true;
        } else return false;
    }

    /**
     * 删除某文件夹中所有文件，但是不删除文件夹
     * 
     * @param file
     * @return
     */
    public static void deleteAllFilesInDir(File file)
    {
        if (file == null)
            return;
        try
        {
            if (file.exists())
            {
                if (file.isDirectory())
                {
                    // 文件夹
                    File[] children = file.listFiles();// 子文件列表
                    for (File f : children)
                    {
                        deleteAllFilesInDir(f);
                    }
                } else
                {
                    // 文件
                    try
                    {
                        file.delete();
                    } catch (Exception e)
                    {
                    }
                }
            } else
            {
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 使用文件通道的方式复制文件
     * 
     * @param s
     *            源文件
     * @param t
     *            复制到的新文件
     */

    public static void fileChannelCopy(File s, File t)
    {
        if (s == null || !s.exists() || t == null)
            return;
        if (t.exists())
        {
            t.delete();
        }

        try
        {
            t.createNewFile();
        } catch (IOException e1)
        {
            e1.printStackTrace();
        }

        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try
        {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();// 得到对应的文件通道
            out = fo.getChannel();// 得到对应的文件通道
            in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制文件夹
     * 
     * @param sourceDir
     * @param targetDir
     * @param filterFilePath
     *            需要过滤掉的文件 绝对路径
     * @throws IOException
     */
    public static void copyDirectiory(String sourceDir, String targetDir, ArrayList<String> filterFilePath)
            throws IOException
    {
        // 新建目标目录
        File tfDir = new File(targetDir);
        if (tfDir.exists())
            tfDir.delete();
        tfDir.mkdirs();

        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++)
        {
            if (file[i].isFile())
            {
                if (filefilter(file[i].getName())) // 过滤文件
                {
                    // DLOG.d("copyDirectiory filter " + file[i].getAbsolutePath());
                    continue;
                }

                // 源文件
                File sourceFile = file[i];

                if (filterFilePath != null && filterFilePath.size() > 0)
                {
                    if (filterFilePath.contains(sourceFile.getAbsolutePath()))
                    {
                        // Log.d("copyDirectiory filter " + file[i].getAbsolutePath());
                        continue;// 绝对路径文件过滤
                    }
                }

                // 目标文件
                File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
                fileChannelCopy(sourceFile, targetFile);
            }
            if (file[i].isDirectory())
            {
                // 准备复制的源文件夹
                String dir1 = sourceDir + File.separator + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + File.separator + file[i].getName();
                copyDirectiory(dir1, dir2, filterFilePath);
            }
        }
    }

    /**
     * 文件过滤
     * 
     * @param filename
     *            文件名
     * @return true 需要过滤掉
     */
    public static boolean filefilter(String filename)
    {
        boolean ret = false;
        if (".DS_Store".equals(filename))
            ret = true;
        return ret;
    }

    /**
     * read file
     * 
     * @param filePath
     * @param charsetName
     *            The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException
     *             if an error occurs while operator BufferedReader
     */
    public static StringBuilder readFile(String filePath, String charsetName)
    {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile())
        {
            return null;
        }

        BufferedReader reader = null;
        try
        {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                if (!fileContent.toString().equals(""))
                {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e)
        {
            throw new RuntimeException("IOException occurred. ", e);
        } finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     * 
     * @param filePath
     * @param content
     * @param append
     *            is append, if true, write to the end of file, else clear content of file and write into it
     * @return return true
     * @throws RuntimeException
     *             if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, String content, boolean append)
    {
        FileWriter fileWriter = null;
        try
        {
            makeDirs(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (IOException e)
        {
            throw new RuntimeException("IOException occurred. ", e);
        } finally
        {
            if (fileWriter != null)
            {
                try
                {
                    fileWriter.close();
                } catch (IOException e)
                {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     * 
     * @param filePath
     * @param stream
     * @return
     * @see {@link #writeFile(String, InputStream, boolean)}
     */
    public static boolean writeFile(String filePath, InputStream stream)
    {
        return writeFile(filePath, stream, false);
    }

    /**
     * write file
     * 
     * @param file
     *            the file to be opened for writing.
     * @param stream
     *            the input stream
     * @param append
     *            if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @return return true
     * @throws RuntimeException
     *             if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(String filePath, InputStream stream, boolean append)
    {
        return writeFile(filePath != null ? new File(filePath) : null, stream, append);
    }

    /**
     * write file
     * 
     * @param file
     * @param stream
     * @return
     * @see {@link #writeFile(File, InputStream, boolean)}
     */
    public static boolean writeFile(File file, InputStream stream)
    {
        return writeFile(file, stream, false);
    }

    /**
     * write file
     * 
     * @param file
     *            the file to be opened for writing.
     * @param stream
     *            the input stream
     * @param append
     *            if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @return return true
     * @throws RuntimeException
     *             if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(File file, InputStream stream, boolean append)
    {
        OutputStream o = null;
        try
        {
            makeDirs(file.getAbsolutePath());
            o = new FileOutputStream(file, append);
            byte data[] = new byte[1024];
            int length = -1;
            while ((length = stream.read(data)) != -1)
            {
                o.write(data, 0, length);
            }
            o.flush();
            return true;
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        } catch (IOException e)
        {
            throw new RuntimeException("IOException occurred. ", e);
        } finally
        {
            if (o != null)
            {
                try
                {
                    o.close();
                    stream.close();
                } catch (IOException e)
                {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * copy file
     * 
     * @param sourceFilePath
     * @param destFilePath
     * @return
     * @throws RuntimeException
     *             if an error occurs while operator FileOutputStream
     */
    public static boolean copyFile(String sourceFilePath, String destFilePath)
    {
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(sourceFilePath);
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        }
        return writeFile(destFilePath, inputStream);
    }

    /**
     * read file to string list, a element of list is a line
     * 
     * @param filePath
     * @param charsetName
     *            The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException
     *             if an error occurs while operator BufferedReader
     */
    public static List<String> readFileToList(String filePath, String charsetName)
    {
        File file = new File(filePath);
        List<String> fileContent = new ArrayList<String>();
        if (file == null || !file.isFile())
        {
            return null;
        }

        BufferedReader reader = null;
        try
        {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                fileContent.add(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e)
        {
            throw new RuntimeException("IOException occurred. ", e);
        } finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * get file name from path, not include suffix
     * 
     * <pre>
     *      getFileNameWithoutExtension(null)               =   null
     *      getFileNameWithoutExtension("")                 =   ""
     *      getFileNameWithoutExtension("   ")              =   "   "
     *      getFileNameWithoutExtension("abc")              =   "abc"
     *      getFileNameWithoutExtension("a.mp3")            =   "a"
     *      getFileNameWithoutExtension("a.b.rmvb")         =   "a.b"
     *      getFileNameWithoutExtension("c:\\")              =   ""
     *      getFileNameWithoutExtension("c:\\a")             =   "a"
     *      getFileNameWithoutExtension("c:\\a.b")           =   "a"
     *      getFileNameWithoutExtension("c:a.txt\\a")        =   "a"
     *      getFileNameWithoutExtension("/home/admin")      =   "admin"
     *      getFileNameWithoutExtension("/home/admin/a.txt/b.mp3")  =   "b"
     * </pre>
     * 
     * @param filePath
     * @return file name from path, not include suffix
     * @see
     */
    public static String getFileNameWithoutExtension(String filePath)
    {
        if (StringUtil.isEmpty(filePath))
        {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1)
        {
            return (extenPosi == -1 ? filePath : filePath.substring(0, extenPosi));
        }
        if (extenPosi == -1)
        {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1, extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * get file name from path, include suffix
     * 
     * <pre>
     *      getFilePath(null)               =   null
     *      getFilePath("")                 =   ""
     *      getFilePath("   ")              =   "   "
     *      getFilePath("a.mp3")            =   "a.mp3"
     *      getFilePath("a.b.rmvb")         =   "a.b.rmvb"
     *      getFilePath("abc")              =   "abc"
     *      getFilePath("c:\\")              =   ""
     *      getFilePath("c:\\a")             =   "a"
     *      getFilePath("c:\\a.b")           =   "a.b"
     *      getFilePath("c:a.txt\\a")        =   "a"
     *      getFilePath("/home/admin")      =   "admin"
     *      getFilePath("/home/admin/a.txt/b.mp3")  =   "b.mp3"
     * </pre>
     * 
     * @param filePath
     * @return file name from path, include suffix
     */
    public static String getFileName(String filePath)
    {
        if (StringUtil.isEmpty(filePath))
        {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * get folder name from path
     * 
     * <pre>
     *      getFolderName(null)               =   null
     *      getFolderName("")                 =   ""
     *      getFolderName("   ")              =   ""
     *      getFolderName("a.mp3")            =   ""
     *      getFolderName("a.b.rmvb")         =   ""
     *      getFolderName("abc")              =   ""
     *      getFolderName("c:\\")              =   "c:"
     *      getFolderName("c:\\a")             =   "c:"
     *      getFolderName("c:\\a.b")           =   "c:"
     *      getFolderName("c:a.txt\\a")        =   "c:a.txt"
     *      getFolderName("c:a\\b\\c\\d.txt")    =   "c:a\\b\\c"
     *      getFolderName("/home/admin")      =   "/home"
     *      getFolderName("/home/admin/a.txt/b.mp3")  =   "/home/admin/a.txt"
     * </pre>
     * 
     * @param filePath
     * @return
     */
    public static String getFolderName(String filePath)
    {

        try
        {
            if (StringUtil.isEmpty(filePath))
            {
                return filePath;
            }

            int filePosi = filePath.lastIndexOf(File.separator);
            return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
        } catch (Exception e)
        {
            return filePath;
        }
    }

    /**
     * get suffix of file from path
     * 
     * <pre>
     *      getFileExtension(null)               =   ""
     *      getFileExtension("")                 =   ""
     *      getFileExtension("   ")              =   "   "
     *      getFileExtension("a.mp3")            =   "mp3"
     *      getFileExtension("a.b.rmvb")         =   "rmvb"
     *      getFileExtension("abc")              =   ""
     *      getFileExtension("c:\\")              =   ""
     *      getFileExtension("c:\\a")             =   ""
     *      getFileExtension("c:\\a.b")           =   "b"
     *      getFileExtension("c:a.txt\\a")        =   ""
     *      getFileExtension("/home/admin")      =   ""
     *      getFileExtension("/home/admin/a.txt/b")  =   ""
     *      getFileExtension("/home/admin/a.txt/b.mp3")  =   "mp3"
     * </pre>
     * 
     * @param filePath
     * @return
     */
    public static String getFileExtension(String filePath)
    {
        if (StringUtil.isEmpty(filePath))
        {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1)
        {
            return "";
        }
        return (filePosi >= extenPosi) ? "" : filePath.substring(extenPosi + 1);
    }

    /**
     * Creates the directory named by the trailing filename of this file, including the complete directory path required to create this directory. <br/>
     * <br/>
     * <ul>
     * <strong>Attentions:</strong>
     * <li>makeDirs("C:\\Users\\Trinea") can only create users folder</li>
     * <li>makeFolder("C:\\Users\\Trinea\\") can create Trinea folder</li>
     * </ul>
     * 
     * @param filePath
     * @return true if the necessary directories have been created or the target directory already exists, false one of the directories can not be created.
     *         <ul>
     *         <li>if {@link FileUtils#getFolderName(String)} return null, return false</li>
     *         <li>if target directory already exists, return true</li>
     *         <li>return {@link File#makeFolder}</li>
     *         </ul>
     */
    public static boolean makeDirs(String filePath)
    {
        String folderName = getFolderName(filePath);
        if (StringUtil.isEmpty(folderName))
        {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }

    /**
     * @param filePath
     * @return
     * @see #makeDirs(String)
     */
    public static boolean makeFolders(String filePath)
    {
        return makeDirs(filePath);
    }

    /**
     * Indicates if this file represents a file on the underlying file system.
     *
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath)
    {
        if (StringUtil.isEmpty(filePath))
        {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * Indicates if this file represents a directory on the underlying file system.
     *
     * @param directoryPath
     * @return
     */
    public static boolean isFolderExist(String directoryPath)
    {
        if (StringUtil.isEmpty(directoryPath))
        {
            return false;
        }

        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path)
    {
        if (StringUtil.isEmpty(path))
        {
            return true;
        }

        File file = new File(path);
        if (!file.exists())
        {
            return true;
        }
        if (file.isFile())
        {
            return file.delete();
        }
        if (!file.isDirectory())
        {
            return false;
        }
        for (File f : file.listFiles())
        {
            if (f.isFile())
            {
                f.delete();
            } else if (f.isDirectory())
            {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * get file size
     * <ul>
     * <li>if path is null or empty, return -1</li>
     * <li>if path exist and it is a file, return file size, else return -1</li>
     * <ul>
     *
     * @param path
     * @return returns the length of this file in bytes. returns -1 if the file does not exist.
     */
    public static long getFileSize(String path)
    {
        if (StringUtil.isEmpty(path))
        {
            return -1;
        }

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    public static long getDirSize(File file)
    {
        try
        {
            if (file.exists())
            {
                // 如果是目录则递归计算其内容的总大小
                if (file.isDirectory())
                {
                    File[] children = file.listFiles();
                    long size = 0;
                    for (File f : children)
                        size += getDirSize(f);
                    return size;
                } else
                {
                    long size = file.length();
                    return size;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public static Intent openFile(String filePath)
    {
        if (filePath == null)
        {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists())
            return null;
        /* 取得扩展名 */
        String end = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length())
                .toLowerCase();
        end = end.trim().toLowerCase();
        System.out.println(end);
        /* 依扩展名的类型决定MimeType */
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf") || end.equals("ogg")
                || end.equals("wav") || end.equals("amr"))
        {
            return getAudioFileIntent(filePath);
        } else if (end.equals("3gp") || end.equals("mp4"))
        {
            return getAudioFileIntent(filePath);
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg")
                || end.equals("bmp"))
        {
            return getImageFileIntent(filePath);
        } else if (end.equals("apk"))
        {
            return getApkFileIntent(filePath);
        } else if (end.equals("ppt"))
        {
            return getPptFileIntent(filePath);
        } else if (end.equals("xls"))
        {
            return getExcelFileIntent(filePath);
        } else if (end.equals("doc"))
        {
            return getWordFileIntent(filePath);
        } else if (end.equals("pdf"))
        {
            return getPdfFileIntent(filePath);
        } else if (end.equals("chm"))
        {
            return getChmFileIntent(filePath);
        } else if (end.equals("txt"))
        {
            return getTextFileIntent(filePath, false);
        } else
        {
            return getAllIntent(filePath);
        }
    }

    // Android获取一个用于打开APK文件的intent
    public static Intent getAllIntent(String param)
    {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "*/*");
        return intent;
    }

    // Android获取一个用于打开APK文件的intent
    public static Intent getApkFileIntent(String param)
    {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        return intent;
    }

    // Android获取一个用于打开VIDEO文件的intent
    public static Intent getVideoFileIntent(String param)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "video/*");
        return intent;
    }

    // Android获取一个用于打开AUDIO文件的intent
    public static Intent getAudioFileIntent(String param)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "audio/*");
        return intent;
    }

    // Android获取一个用于打开Html文件的intent
    public static Intent getHtmlFileIntent(String param)
    {

        Uri uri = Uri.parse(param).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content")
                .encodedPath(param).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    // Android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(String param)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "image/*");
        return intent;
    }

    // Android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent(String param)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    // Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(String param)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    // Android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent(String param)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    // Android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent(String param)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    // Android获取一个用于打开文本文件的intent
    public static Intent getTextFileIntent(String param, boolean paramBoolean)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean)
        {
            Uri uri1 = Uri.parse(param);
            intent.setDataAndType(uri1, "text/plain");
        } else
        {
            Uri uri2 = Uri.fromFile(new File(param));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }

    // Android获取一个用于打开PDF文件的intent
    public static Intent getPdfFileIntent(String param)
    {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/pdf");
        return intent;
    }
    /*
     * @TargetApi(Build.VERSION_CODES.KITKAT) public static String getRealPathFromURI(Uri contentUri, Activity context) { String ret = null; if (PhoneUtil.hasKitkat()) { if ("com.android.providers.downloads.documents".equals(contentUri.getAuthority())) { final String id = DocumentsContract.getDocumentId(contentUri); final Uri uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id)); Cursor cursor = null; final String column = "_data"; final String[]
     * projection = { column }; try { cursor = context.getContentResolver().query(uri, projection, null, null, null); if (cursor != null && cursor.moveToFirst()) { final int index = cursor.getColumnIndexOrThrow(column); return cursor.getString(index); } } finally { if (cursor != null) cursor.close(); } return null; } if (DocumentsContract.isDocumentUri(context, contentUri)) { String wholeID = DocumentsContract.getDocumentId(contentUri); if (!StringUtil.isEmpty(wholeID)) { String id =
     * wholeID.split(":")[1]; String[] column = { MediaStore.Images.Media.DATA }; Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, MediaStore.Images.Media._ID + "=?", new String[] { id }, null); int columnIndex = cursor.getColumnIndex(column[0]); if (cursor != null && cursor.moveToFirst()) { ret = cursor.getString(columnIndex); } cursor.close(); } return ret; } } String[] proj = { MediaStore.Images.Media.DATA }; Cursor cursor =
     * context.managedQuery(contentUri, proj, null, null, null); // 不要调用cursor的close try { if (cursor != null && cursor.moveToFirst()) { int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); ret = cursor.getString(column_index); } } catch (Exception e) { e.printStackTrace(); } return ret; }
     */

    /**
     * 获取文件指定分块内容
     */
    public static byte[] getBlock(long offset, File file, int blockSize)
    {
        byte[] result = new byte[blockSize];
        RandomAccessFile accessFile = null;
        try
        {
            accessFile = new RandomAccessFile(file, "r");
            accessFile.seek(offset);
            int readSize = accessFile.read(result);
            if (readSize == -1)
            {
                return null;
            } else if (readSize == blockSize)
            {
                return result;
            } else
            {
                byte[] tmpByte = new byte[readSize];
                System.arraycopy(result, 0, tmpByte, 0, readSize);
                return tmpByte;
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (accessFile != null)
            {
                try
                {
                    accessFile.close();
                } catch (IOException e1)
                {
                }
            }
        }
        return null;
    }

}
