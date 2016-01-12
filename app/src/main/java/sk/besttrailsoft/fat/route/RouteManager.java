package sk.besttrailsoft.fat.route;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import sk.besttrailsoft.fat.FileHelper;
import sk.besttrailsoft.fat.program.Program;
import sk.besttrailsoft.fat.program.ProgramStep;

/**
 * Created by Adriana on 21.12.2015.
 */
public class RouteManager {
    public static final String DIRECTORY = "routes";
    private FileHelper fileHelper;

    public RouteManager(Context context){
        if (context == null)
            throw new InvalidParameterException("context cannot be null");

        fileHelper = FileHelper.getInstance(context);
    }
    public ArrayList<Route> getAllRoutes(){
        return null;
    }

    public String[] getAllRoutesNames(){

        return fileHelper.getFilesNamesFromDirectory(DIRECTORY);
    }

    public boolean doPredefinedRoutesExist(){

        return 0 < fileHelper.getFilesNamesFromDirectory(DIRECTORY).length;
    }

    public void createRoute(Route route) throws IOException {
        String name = route.getName();
        String content = "";
        try {
            content = route.toJson();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fileHelper.writeToFile(name, content, DIRECTORY);
    }

    public Route getRoute(String name) throws IOException, JSONException {
        if (name == null || name.equals("")){
            throw new InvalidParameterException("name is null or empty");
        }
        Route route = new Route();
        String json = fileHelper.getFileContent(name, DIRECTORY);
        JSONObject jsonObject = new JSONObject(json);
        route.setName(jsonObject.getString("name"));
        route.setWaypoints(convertJsonArrayToWaypointsStringArray(jsonObject.getJSONArray("waypoints")));
        System.err.println(json);
        return route;

    }

    public void deleteRoute(String routeName){
        fileHelper.deleteFile(routeName, DIRECTORY);

    }

    private ArrayList<String> convertJsonArrayToWaypointsStringArray(JSONArray array) throws JSONException {
        ArrayList<String> result = new ArrayList<>();
        String point;
        JSONObject obj;
        for (int i=0;i<array.length();i++){
            obj = array.getJSONObject(i);
            point = obj.getString("point");

            result.add(point);
        }
        return result;
    }
}
