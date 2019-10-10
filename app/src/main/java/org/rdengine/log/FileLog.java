package org.rdengine.log;

import org.rdengine.runtime.RT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLog
{
    public static void writeLog(String log)
    {
        if (!RT.DEBUG)
        {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = sdf.format(new Date());

        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm:ss");

        String time = timeSdf.format(new Date());
        log = "\n" + "-------------------------" + "\n" + time + ":" + log;

        File file = new File(RT.defaultLog, fileName);
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        FileOutputStream writer;
        try
        {
            writer = new FileOutputStream(file, true);
            writer.write(log.toString().getBytes());
            writer.flush();
            writer.close();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
