package sk.besttrailsoft.fat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by Mros on 11/17/15.
 */
public class ProgramStepsAdapter extends ArrayAdapter {

    public ProgramStepsAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ProgramStepsAdapter(Context context, int resource, Object[] objects) {
        super(context, resource, objects);
    }

/*  @Override
    public View getView(int position, View convertView, ViewGroup parent) {
/*      return new View();
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.itemlistrow, null);
        }

        Item p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.id);
            TextView tt2 = (TextView) v.findViewById(R.id.categoryId);
            TextView tt3 = (TextView) v.findViewById(R.id.description);

            if (tt1 != null) {
                tt1.setText(p.getId());
            }

            if (tt2 != null) {
                tt2.setText(p.getCategory().getId());
            }

            if (tt3 != null) {
                tt3.setText(p.getDescription());
            }
        }

        return v;
    }*/


}
