package sk.besttrailsoft.fat;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adriana on 2.12.2015.
 */
public class DirectionsApiHelper {

    private static String urlBase = "http://maps.googleapis.com/maps/api/directions/json";

    public static List<LatLng> getRouteFromWaypoints(List<LatLng> waypoints) {
        if(waypoints == null)
            throw new NullPointerException("waypoints cannot be null");
        if(waypoints.size() < 2 || waypoints.get(0) == waypoints.get(1))
            throw new IllegalArgumentException("waypoints must contain at least 2 distinct points");

        List<LatLng> route = new ArrayList<>();
        try {
            JSONObject result = new JSONObject(getResponseFromUrl(buildUrl(waypoints)));
            JSONArray routes = result.getJSONArray("routes");

            long distanceForSegment = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    .getJSONObject("distance").getInt("value");

            JSONArray steps = routes.getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONArray("steps");

            for(int i=0; i < steps.length(); i++) {
                String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");

                for(LatLng p : decodePolyline(polyline)) {
                    route.add(p);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return route;
    }

    public static float distance(LatLng point1, LatLng point2) {
        Location first = new Location("first");
        first.setLatitude(point1.latitude);
        first.setLongitude(point1.longitude);

        Location second = new Location("second");
        second.setLatitude(point2.latitude);
        second.setLongitude(point2.longitude);

        return first.distanceTo(second);
    }

    public static float distance(List<LatLng> points) {
        if(points == null || points.size() < 2)
            return 0;
        float totalDistance = 0;

        for (int i = 0; i<points.size()-1; i++) {
            totalDistance += distance(points.get(i), points.get(i+1));
        }
        return totalDistance;
    }

    private static String buildUrl(List<LatLng> points) {
        StringBuilder urlBuilder = new StringBuilder(urlBase);

        urlBuilder.append("?origin=" + points.get(0).latitude + "," + points.get(0).longitude);
        urlBuilder.append("&destination="+ points.get(points.size()-1).latitude +
                "," + points.get(points.size()-1).longitude);
        urlBuilder.append("&sensor=false");
        urlBuilder.append("&mode=walking");

        urlBuilder.append("&waypoints=");
        for(int i = 1; i < points.size(); i++) {
            urlBuilder.append("via:" + points.get(i).latitude + "," + points.get(i).longitude );
            if(i != points.size()-1) {
                urlBuilder.append("|");
            }
        }

        return urlBuilder.toString();
    }

    private static String getResponseFromUrl(String urlString){
        StringBuilder chaine = new StringBuilder("");
        try{
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }

        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        }

        return chaine.toString();
    }

    private static List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }
}
