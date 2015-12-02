package sk.besttrailsoft.fat.program;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mros on 12/1/15.
 */
public class Program {
    public Program(){
        steps = new ArrayList<>();
    }

    private String name;
    private ArrayList<ProgramStep> steps;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ProgramStep> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<ProgramStep> steps) {
        this.steps = steps;
    }

    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();

            json.put("name", getName());
        JSONArray steps = new JSONArray();

        for (ProgramStep step : getSteps()) {
            steps.put(step.toJson());
        }

        json.put("steps", steps);

        return json.toString();
    }
}

