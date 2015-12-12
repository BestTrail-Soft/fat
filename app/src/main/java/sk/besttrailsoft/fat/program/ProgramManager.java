package sk.besttrailsoft.fat.program;

import android.content.Context;

import com.google.android.gms.games.internal.api.NotificationsImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
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

    public Program getProgram(String name) throws IOException, JSONException {
        if (name == null || name == ""){
            throw new InvalidParameterException("name is null or empty");
        }
        Program program = new Program();
        String json = fileHelper.getFileContent(name, DIRECTORY);
        JSONObject jsonObject = new JSONObject(json);
       program.setName(jsonObject.getString("name"));
        program.setSteps(convertJsonArrayToStepsArray(jsonObject.getJSONArray("steps")));
        System.err.println(json);
        return program;

    }

    public void deleteProgram(String programName){
        fileHelper.deleteFile(programName, DIRECTORY);

    }

    private ArrayList<ProgramStep> convertJsonArrayToStepsArray(JSONArray array) throws JSONException {
        ArrayList<ProgramStep> result = new ArrayList<>();
        ProgramStep step;
        String distance;
        JSONObject obj;
        for (int i=0;i<array.length();i++){
            obj = array.getJSONObject(i);
            step = new ProgramStep();
            step.setText(obj.getString("text"));

            if ((distance = obj.getString("distance")) != null){
                step.setDistance(Integer.parseInt(distance));
            }
            else{
                step.setTime(Integer.parseInt(obj.getString("time")));
            }
            result.add(step);
        }
        return result;
    }

}
