package sk.besttrailsoft.fat.program;

import android.content.Context;

import com.google.android.gms.games.internal.api.NotificationsImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import sk.besttrailsoft.fat.FileHelper;

/**
 * Created by Mros on 12/1/15.
 */
public class ProgramManager {

    public static final String DIRECTORY = "programs";
    private FileHelper fileHelper;

    public ProgramManager(Context context){
        if (context == null)
            throw new InvalidParameterException("context cannot be null");

        fileHelper = FileHelper.getInstance(context);
    }
    public ArrayList<Program> getAllPrograms(){

        return null;
    }

    public String[] getAllProgramsNames(){

        return fileHelper.getFilesNamesFromDirectory(DIRECTORY);
    }

    public void createProgram(Program program) throws IOException {
        String name = program.getName();
        String content = "";
        try {
            content = program.toJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fileHelper.writeToFile(name,content, DIRECTORY);
    }

    public void deleteProgram(String programName){
        fileHelper.deleteFile(programName, DIRECTORY);

    }

}
