package sk.besttrailsoft.fat.route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Adriana on 21.12.2015.
 */
public class Route {
    private String name;
    private List<String> waypoints;

    public void setName(String name) {
        if(name == null)
            throw new NullPointerException("name cannot be null");
        this.name = name;
    }

    public void setWaypoints(List<String> waypoints) {
        if(waypoints == null)
            throw new NullPointerException("waypoints cannot be null");
        if(waypoints.size() <1)
            throw new IllegalArgumentException("waypoints must contain at least one element");
        this.waypoints = waypoints;
    }

    public String getName() {
        return name;
    }

    public List<String> getWaypoints() {
        return waypoints;
    }

    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("name", getName());
        JSONArray waypoints = new JSONArray();

        for (String point : this.waypoints) {
            JSONObject jsonObject= new JSONObject();
            try {
                jsonObject.put("point", point);
                waypoints.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        json.put("waypoints", waypoints);

        return json.toString();
    }
}
