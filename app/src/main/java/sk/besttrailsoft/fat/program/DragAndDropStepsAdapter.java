package sk.besttrailsoft.fat.program;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;

import sk.besttrailsoft.fat.R;
import sk.besttrailsoft.fat.program.ProgramStep;

/**
 * Created by Mros on 12/5/15.
 */
public class DragAndDropStepsAdapter extends BaseAdapter {
    private ArrayList<ProgramStep> data;

    private static int offset;

    private static LayoutInflater inflater=null;

    public DragAndDropStepsAdapter(Activity activity, ArrayList<ProgramStep> data){
        if (data == null)
            throw new InvalidParameterException("data cannot be null");

        if (activity == null)
            throw new InvalidParameterException("activity cannot be null");

        this.data = data;
        inflater = (LayoutInflater)activity.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;

        if(convertView==null){
            view = inflater.inflate(R.layout.program_step_item, null);

            holder = new ViewHolder();
            holder.text = (TextView) view.findViewById(R.id.stepText);
            holder.position = (TextView) view.findViewById(R.id.stepPosition);

            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        if (data.size() <= 0){
            holder.text.setText("No steps");
        }
        else{
            ProgramStep step = data.get(position);

            holder.text.setText(step.getText());
            holder.position.setText((position+1) + ".");
        }


        view.setId(position);
        view.setOnDragListener(new StepDragListener());
        view.setOnTouchListener(new StepOnTouchListener());
        return view;
    }



    public static class ViewHolder{
        TextView text;
        TextView position;

    }




    protected class StepDragListener implements View.OnDragListener {


        @Override
        public boolean onDrag(View view, DragEvent event) {

            final int action = event.getAction();

            switch (action) {

                case DragEvent.ACTION_DRAG_STARTED:




                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Integer startingId = Integer.valueOf(event.getClipDescription().getLabel().toString());
                    if (startingId == null){
                        System.err.println("Dragged object has invalid id");
                        return false;
                    }


                    int currentId = view.getId();

                    if (currentId == startingId.intValue() + offset)
                        return true;

                    System.err.println("s: " + startingId + " c: " + currentId + " o: " + offset);

                    Collections.swap(data, view.getId(), startingId.intValue() + offset);
                    offset = Math.abs(currentId - startingId.intValue());
                    notifyDataSetChanged();
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    offset = 0;


            }
            return false;
        }
    }

    protected class StepOnTouchListener implements View.OnTouchListener {


        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                ClipData dragData = ClipData.newPlainText(String.valueOf(view.getId()), "data");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(dragData, shadowBuilder, view , 0);
            }
            return false;
        }
    }
}
