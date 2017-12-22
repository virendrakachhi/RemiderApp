package com.attendanceapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.attendanceapp.R;
import com.attendanceapp.activities.MediaActivity;
import com.attendanceapp.models.HCEmpCheckList;
import com.attendanceapp.models.HCEmployee;
import com.attendanceapp.models.MediaBean;

import java.util.ArrayList;
import java.util.List;

public class HCManagerCheckListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<HCEmpCheckList> empCheckLists = new ArrayList<>();
    private List<HCEmpCheckList> empValuesCheckLists = new ArrayList<>();
    private List<MediaBean> arryMedia = new ArrayList<>();


    private Context mContext;

    public HCManagerCheckListAdapter(Context context, List<HCEmpCheckList> empCheckLists, List<HCEmpCheckList> empValuesCheckLists, List<MediaBean> arryMedia) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        this.empCheckLists = empCheckLists;
        this.arryMedia = arryMedia;
        this.empValuesCheckLists = empValuesCheckLists;
    }

    @Override
    public int getCount() {
        return empCheckLists.size();
    }

/*
    @Override
    public Object getItem(int i) {
        return null;
    }
*/

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

    public class ViewHolder {
        final TextView checkList_heading;
        final TextView comments_edt, timeRange_edt;
        final RadioGroup mRadioGroup;
        final RadioButton yes_cb, no_cb, na_cb;
        final LinearLayout checkList_data, yes_no_na_layout, comment_layout, timeRange_layout;
        final ImageView imgShowAttachments;

        ViewHolder(View view) {

            checkList_heading = (TextView) view.findViewById(R.id.checkList_heading);
            comments_edt = (TextView) view.findViewById(R.id.comments_edt);
            timeRange_edt = (TextView) view.findViewById(R.id.duration_edt);

            imgShowAttachments = (ImageView) view.findViewById(R.id.img_view_attachment);

            yes_no_na_layout = (LinearLayout) view.findViewById(R.id.yes_no_na_layout);
            comment_layout = (LinearLayout) view.findViewById(R.id.comment_layout);
            timeRange_layout = (LinearLayout) view.findViewById(R.id.timeRange_layout);
            checkList_data = (LinearLayout) view.findViewById(R.id.checkList_data);

            mRadioGroup = (RadioGroup) view.findViewById(R.id.radiogroup);
            yes_cb = (RadioButton) view.findViewById(R.id.yes_cb);
            no_cb = (RadioButton) view.findViewById(R.id.no_cb);
            na_cb = (RadioButton) view.findViewById(R.id.na_cb);


            checkList_data.setVisibility(View.VISIBLE);

            comments_edt.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });


        }
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), getChecklistItem(position), view, position);
        return view;
    }

    private void bind(HCEmpCheckList mCheckList, HCEmpCheckList valuesCheckList, View view, final int position) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String valueHeading = null,
                valueYesNo = null,
                valueNa = null,
                valueComment = null,
                valueTimeRange = null;

        Boolean showYesNo = false,
                showNa = false,
                showComment = false,
                showTimeRange = false;
        try {
            valueHeading = (valuesCheckList.valueHeading);
            valueYesNo = (valuesCheckList.valueYesNo);
            valueNa = (valuesCheckList.valueNa);
            valueComment = (valuesCheckList.valueComment);
            valueTimeRange = (valuesCheckList.valueTimeRange);

            showYesNo = (mCheckList.showYesNo);
            showNa = (mCheckList.showNa);
            showComment = (mCheckList.showComment);
            showTimeRange = (mCheckList.showTimeRange);

            if (position == empValuesCheckLists.size() - 1) {
                if (arryMedia.size() > 0) {
                    holder.imgShowAttachments.setVisibility(View.VISIBLE);
                    holder.imgShowAttachments.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (arryMedia.size() == 1) {
                                Intent intentTOview = new Intent(mContext, MediaActivity.class);
                                intentTOview.putExtra("MediaType", arryMedia.get(0).getStrMediaType());
                                intentTOview.putExtra("MediaURL", arryMedia.get(0).getStrMediaURL());
                                mContext.startActivity(intentTOview);
                            } else {
                                employeePicker();
                            }
                        }
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        holder.yes_no_na_layout.setVisibility(view.VISIBLE);
        holder.checkList_data.setVisibility(view.VISIBLE);

        // Heading
        if (valueHeading != null) {
            holder.checkList_heading.setText(valueHeading);

        }

        // Radio buttons

        if (showYesNo) {
            holder.yes_cb.setVisibility(view.VISIBLE);
            holder.no_cb.setVisibility(view.VISIBLE);
        } else {
            holder.yes_cb.setVisibility(view.GONE);
            holder.no_cb.setVisibility(view.GONE);
        }

        if (showNa) {
            holder.na_cb.setVisibility(view.VISIBLE);
        } else {
            holder.na_cb.setVisibility(view.GONE);
        }

        // Texts

        holder.checkList_data.setVisibility(view.VISIBLE);
        if (showComment) {
            holder.comment_layout.setVisibility(view.VISIBLE);

        } else {
            holder.comment_layout.setVisibility(view.GONE);

        }
        if (showTimeRange) {
            holder.timeRange_layout.setVisibility(view.VISIBLE);
        } else {
            holder.timeRange_layout.setVisibility(view.GONE);
        }


        // Setting the Values
        try {

            String yes_no_val = valueYesNo.trim();

            if (showYesNo && yes_no_val.equalsIgnoreCase("yes")) {
                holder.yes_cb.setChecked(true);
                holder.yes_cb.setEnabled(false);
                holder.no_cb.setVisibility(view.GONE);
                holder.na_cb.setVisibility(view.GONE);
//                empValuesCheckLists.get(position).setSelectedRadio("1"); // If yes item is seleted
            } else if (showYesNo && yes_no_val.equalsIgnoreCase("no")) {
                holder.no_cb.setChecked(true);
                holder.no_cb.setEnabled(false);
                holder.yes_cb.setVisibility(view.GONE);
                holder.na_cb.setVisibility(view.GONE);
//                empValuesCheckLists.get(position).setSelectedRadio("0"); // If no seleted
            } else if (showNa && yes_no_val.equalsIgnoreCase("na")) {
                holder.na_cb.setChecked(true);
                holder.na_cb.setEnabled(false);
                holder.no_cb.setVisibility(view.GONE);
                holder.yes_cb.setVisibility(view.GONE);
//                empValuesCheckLists.get(position).setSelectedRadio("-1"); // If NA item is seleted
            } else {
                holder.yes_no_na_layout.setVisibility(view.GONE);
                holder.yes_cb.setVisibility(view.GONE);
                holder.no_cb.setVisibility(view.GONE);
                holder.na_cb.setVisibility(view.GONE);
//                empValuesCheckLists.get(position).setSelectedRadio("0"); // If no item is seleted
            }
        } catch (Exception e) {
            e.printStackTrace();
//            empValuesCheckLists.get(position).setSelectedRadio("0"); // If no item is seleted
        }


        if (showComment && (valueComment != null)) {
            holder.comments_edt.setText(valueComment);
        }

        if (showTimeRange && (valueTimeRange != null)) {
            holder.timeRange_edt.setText(valueTimeRange);
        }



        /*
            Get which radio button is selected and update the array list
             */
        holder.mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.yes_cb) {
                    empValuesCheckLists.get(position).setSelectedRadio("1");
                } else if (checkedId == R.id.no_cb) {
                    empValuesCheckLists.get(position).setSelectedRadio("0");
                } else if (checkedId == R.id.na_cb) {
                    empValuesCheckLists.get(position).setSelectedRadio("-1");
                } else {
                    empValuesCheckLists.get(position).setSelectedRadio("0");
                }
            }
        });

        /*
        Text watcher to reflect changes in comment section
         */
        holder.comments_edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                empValuesCheckLists.get(position).setValueComment(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /*
        Text watcher to reflect changes in Time Range section
         */
        holder.timeRange_edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                empValuesCheckLists.get(position).setValueTimeRange(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_hc_manager_checklist, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    public List<HCEmpCheckList> getUpdatedCheckListValue() {
        if (empValuesCheckLists != null && empValuesCheckLists.size() > 0) {
            return empValuesCheckLists;
        }
        return null;
    }


    private void employeePicker() {

        final AlertDialog dialogBox;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = (View) inflater.inflate(R.layout.employee_picker_layout, null);

        alertDialog.setView(convertView);
        alertDialog.setTitle("Select file");

        ListView lv = (ListView) convertView.findViewById(R.id.lv);
        ArrayList<HCEmployee> stringArrayList = new ArrayList<HCEmployee>();
        for (int i = 0; i < arryMedia.size(); i++) {
            HCEmployee hcEmployee = new HCEmployee();
            hcEmployee.setName(arryMedia.get(i).getStrMediaName());
            stringArrayList.add(hcEmployee);
        }
        final HCEmpPickerAdapter adapter = new HCEmpPickerAdapter(mContext, stringArrayList);
        lv.setAdapter(adapter);

        alertDialog.setCancelable(true);
        dialogBox = alertDialog.create();
        dialogBox.show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogBox.dismiss();

                Intent intentTOview = new Intent(mContext, MediaActivity.class);
                intentTOview.putExtra("MediaType", arryMedia.get(i).getStrMediaType());
                intentTOview.putExtra("MediaURL", arryMedia.get(i).getStrMediaURL());
                mContext.startActivity(intentTOview);


            }
        });

    }

}
