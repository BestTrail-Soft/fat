package sk.besttrailsoft.fat;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Mros on 11/17/15.
 */
public class FileHelper {
    private static Context context;

    private static FileHelper ourInstance = new FileHelper();

    public static FileHelper getInstance(Context context) {
        ourInstance.initialize(context);
        return ourInstance;
    }

    private FileHelper() {
    }

    private static void initialize(Context context){
        if (context == null)
            throw new IllegalArgumentException("context cannot be null");

        FileHelper.context = context;
    }

    public String[] getFilesNamesFromDirectory(String directoryName){
        if (directoryName == null || directoryName.isEmpty())
            throw new IllegalArgumentException("directoryName cannot be null");

        File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
        String[] files = directory.list();

        if (files == null)
            return new String[]{};

        return files;
    }

    public void writeToFile(String filename, String content, String directoryName) throws IOException {

        if (filename == null || filename.isEmpty())
            throw new IllegalArgumentException("filename cannot be null");

        if (content == null)
            throw new IllegalArgumentException("content cannot be null");

        if (directoryName == null || directoryName.isEmpty())
            throw new IllegalArgumentException("directory cannot be null");

        FileOutputStream outputStream;

            File directory = context.getDir(directoryName, Context.MODE_PRIVATE);
            File file = new File(directory, filename);
            outputStream = new FileOutputStream(file);
            outputStream.write(content.getBytes());
            outputStream.close();


    }


}
