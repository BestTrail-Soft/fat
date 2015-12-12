package sk.besttrailsoft.fat.program;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
    private IContextMenuable activity;

    private static int offset;

    private static LayoutInflater inflater=null;

    public DragAndDropStepsAdapter(Activity activity, ArrayList<ProgramStep> data){
        if (data == null)
            throw new InvalidParameterException("data cannot be null");

        if (activity == null)
            throw new InvalidParameterException("activity cannot be null");

        this.activity = (IContextMenuable)activity;
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
            holder.stepDuration = (TextView) view.findViewById(R.id.stepDurationTextView);

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

            if (step.getDistance() != null){
                holder.stepDuration.setText(step.getDistance() + " meter(s)");
            }
            else{
                holder.stepDuration.setText(step.getTime() + " minute(s)");
            }
        }


        view.setId(position);
        view.setOnDragListener(new StepDragListener());
        //view.setOnTouchListener(new StepOnTouchListener());
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipData dragData = ClipData.newPlainText(String.valueOf(view.getId()), "data");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(dragData, shadowBuilder, view , 0);
                return true;
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openListViewMenu(v);
            }
        });



        return view;
    }



    public static class ViewHolder{
        TextView text;
        TextView position;
        TextView stepDuration;

    }




    protected class StepDragListener implements View.OnDragListener {


        @Override
        public boolean onDrag(View view, DragEvent event) {

            final int action = event.getAction();

            switch (action) {

                case DragEvent.ACTION_DRAG_STARTED:




                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Integer startingPosition = Integer.valueOf(event.getClipDescription().getLabel().toString());
                    int currentPosition = view.getId();
                    if (startingPosition == null){
                        System.err.println("Dragged object has invalid id");
                        return false;
                    }




                    if (currentPosition == startingPosition.intValue() + offset)
                        return false;



                    Collections.swap(data, currentPosition, startingPosition.intValue() + offset);

                    // offset calculation

                        offset = currentPosition - startingPosition.intValue();

                    notifyDataSetChanged();
                    System.err.println("s: " + startingPosition + " c: " + currentPosition + " o: " + offset);
                    return false;

                case DragEvent.ACTION_DRAG_ENDED:
                    offset = 0;


            }
            return false;
        }
    }



}
