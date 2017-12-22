package com.attendanceapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.models.HCEmpCheckList;

import java.util.HashMap;
import java.util.List;

public class HCEmpCheckListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<HCEmpCheckList> empCheckLists;
    private List<HCEmpCheckList> empValuesCheckLists;

    HashMap<Integer,String> updatedRange = new HashMap<>();

    ViewHolder viewholder2 = null;
    HCEmpCheckList obj ;
    HCEmpCheckList showobj ;

    String valueHeading = null,
            valueYesNo = null,
            valueNa = null,
            valueComment = null,
            valueTimeRange = null;

    Boolean showYesNo = false,
            showNa = false,
            showComment = false,
            showTimeRange = false;

    private Activity mContext;

    public HCEmpCheckListAdapter(Activity context, List<HCEmpCheckList> empCheckLists, List<HCEmpCheckList> empValuesCheckLists) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.empCheckLists = empCheckLists;
        this.empValuesCheckLists = empValuesCheckLists;

    }

    @Override
    public int getCount() {
        return empCheckLists.size();
    }

    @Override
    public HCEmpCheckList getItem(int position) {
        return empCheckLists.get(position);
    }

    public HCEmpCheckList getChecklistItem(int position) {
        return empValuesCheckLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        TextView checkList_heading;
        EditText comments_edt, timeRange_edt;
        RadioGroup mRadioGroup;
        RadioButton yes_cb, no_cb, na_cb;
        LinearLayout checkList_data, yes_no_na_layout, comment_layout, timeRange_layout;
    }


    ViewHolder viewholder = null;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        try {


            if(convertView == null) {
                viewholder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.list_item_hc_emp_checklist, null);
                viewholder.checkList_heading = (TextView) convertView.findViewById(R.id.checkList_heading);
                viewholder.comments_edt = (EditText) convertView.findViewById(R.id.comments_edt);
                viewholder.timeRange_edt = (EditText) convertView.findViewById(R.id.duration_edt);

                viewholder.yes_no_na_layout = (LinearLayout) convertView.findViewById(R.id.yes_no_na_layout);
                viewholder.comment_layout = (LinearLayout) convertView.findViewById(R.id.comment_layout);
                viewholder.timeRange_layout = (LinearLayout) convertView.findViewById(R.id.timeRange_layout);
                viewholder.checkList_data = (LinearLayout) convertView.findViewById(R.id.checkList_data);

                viewholder.mRadioGroup = (RadioGroup) convertView.findViewById(R.id.radiogroup);
                viewholder.yes_cb = (RadioButton) convertView.findViewById(R.id.yes_cb);
                viewholder.no_cb = (RadioButton) convertView.findViewById(R.id.no_cb);
                viewholder.na_cb = (RadioButton) convertView.findViewById(R.id.na_cb);
                convertView.setTag(viewholder);
            }

            else {
                viewholder = (ViewHolder) convertView.getTag();
            }

            obj = empValuesCheckLists.get(position);
            showobj = empCheckLists.get(position);

            valueHeading = (obj.getValueHeading());
            valueYesNo = (obj.getValueYesNo());
            valueNa = (obj.getValueNa());
            valueComment = (obj.getValueComment());
            valueTimeRange = (obj.getValueTimeRange());

            showYesNo = (showobj.getShowYesNo());
            showNa = (showobj.getShowNa());
            showComment = (showobj.getShowComment());
            showTimeRange = (showobj.getShowTimeRange());

            viewholder.yes_no_na_layout.setVisibility(View.VISIBLE);
            viewholder.checkList_data.setVisibility(View.VISIBLE);

            // Heading
            if (valueHeading != null) {
                viewholder.checkList_heading.setText(valueHeading);
            }

            // Radio buttons
            if (showYesNo) {
                viewholder.yes_cb.setVisibility(View.VISIBLE);
                viewholder.no_cb.setVisibility(View.VISIBLE);
            } else {
                viewholder.yes_cb.setVisibility(View.GONE);
                viewholder.no_cb.setVisibility(View.GONE);
            }

            if (showNa) {
                viewholder.na_cb.setVisibility(View.VISIBLE);
            } else {
                viewholder.na_cb.setVisibility(View.GONE);
            }

            // Texts
            viewholder.checkList_data.setVisibility(View.VISIBLE);
            if (showComment) {
                viewholder.comment_layout.setVisibility(View.VISIBLE);

            } else {
                viewholder.comment_layout.setVisibility(View.GONE);
            }
            if (showTimeRange) {
                viewholder.timeRange_layout.setVisibility(View.VISIBLE);
            } else {
                viewholder.timeRange_layout.setVisibility(View.GONE);
            }

            // Setting the Values

            String yes_no_val = obj.getValueYesNo().trim();
            if (showYesNo && yes_no_val.equalsIgnoreCase("yes")) {
                viewholder.yes_cb.setChecked(true);
            } else if (showYesNo && yes_no_val.equalsIgnoreCase("no")) {
                viewholder.no_cb.setChecked(true);
            } else if (showNa && valueNa.equalsIgnoreCase("na")) {
                viewholder.na_cb.setChecked(true);
            }

            if (showComment && (valueComment != null)) {
                viewholder.comments_edt.setText(obj.getValueComment());
            }

            if (showTimeRange && (valueTimeRange != null)) {
                if(obj.getCmntChanged().equalsIgnoreCase("yes")){
                    viewholder.timeRange_edt.setText(updatedRange.get(position));
                }
                else{
                    viewholder.timeRange_edt.setText(obj.getValueTimeRange());
                }
//                viewholder.timeRange_edt.setText(obj.getValueTimeRange());
            }


//            viewholder2=viewholder;  /*
//            Get which radio button is selected and update the array list
//             */
            viewholder.mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.yes_cb) {
                        empValuesCheckLists.get(position).setSelectedRadio("yes");
                    } else if (checkedId == R.id.no_cb) {
                        empValuesCheckLists.get(position).setSelectedRadio("no");
                    } else if (checkedId == R.id.na_cb) {
                        empValuesCheckLists.get(position).setSelectedRadio("na");
                    } else {

                    }
                }
            });

         /*   viewholder.timeRange_edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    try {
                        if (!hasFocus) {
                            int itemIndex = v.getId();
                            String enteredValue = viewholder.timeRange_edt.getText().toString();
                            obj.setValueTimeRange(enteredValue);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
            }
        });*/


            viewholder.timeRange_edt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    obj.setCmntChanged("no");
                }
            });
            /*
        Text watcher to reflect changes in Time Range section
         */
            viewholder.timeRange_edt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    obj.setCmntChanged("yes");
                    empValuesCheckLists.get(position).setValueTimeRange(s.toString());
                    updatedRange.put(position,s.toString());
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    if(obj.getCmntChanged().equalsIgnoreCase("yes")){
//                        obj.setValueTimeRange(s.toString());
//                    }

                }

                @Override
                public void afterTextChanged(Editable s) {
//                    obj.setCmntChanged("done");
//                    notifyDataSetChanged();
                }
            });

            viewholder.comments_edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    try {
                        if (!hasFocus) {
                            int itemIndex = v.getId();
                            String enteredValue = viewholder.comments_edt.getText().toString();
                            obj.setValueComment(enteredValue);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });

        } catch (Exception e) {
            e.printStackTrace();
//            empValuesCheckLists.get(position).setSelectedRadio("0"); // If no item is seleted
        }
        return convertView;

    }
    public List<HCEmpCheckList> getUpdatedCheckListValue() {
        if (empValuesCheckLists != null && empValuesCheckLists.size() > 0) {
            return empValuesCheckLists;
        }
        return null;
    }
}
