package sk.besttrailsoft.fat.program;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mros on 12/1/15.
 */
public class ProgramStep {
    private String text;
    private Long distance;
    private Integer time;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getDistance() {
        return distance;
    }

    public void setDistance(Long distance) {
        this.distance = distance;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String toJson(){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("text", getText());

            if (distance != null)
                jsonObject.put("distance", getDistance());

            if (time != null)
                jsonObject.put("time", getTime());

            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }
}
