package com.hisilicon.tvui.epg;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.epg.EPGEvent;
import com.hisilicon.dtv.epg.IExtendedDescription;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;

/**
 * Defined AlertDialog.Program event name,time and detail description informations are showed in
 * this dialog.<br>
 *
 * @author z00209628
 * @see EPGEvent
 */
public class DescriptionDialog extends Dialog
{
    /**
     * Define program event object.<br>
     */
    private EPGEvent mEPGEvent = null;

    private final Context mContext;

    public DescriptionDialog(Context context, int theme, EPGEvent event)
    {
        super(context, theme);

        this.mEPGEvent = event;

        this.mContext = context;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LayoutParams params = this.getWindow().getAttributes();
        params.x = 0;
        params.y = 0;
        params.dimAmount = 0.0f;
        this.getWindow().setAttributes(params);

        this.setContentView(R.layout.epg_description_information);
        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        TextView nameTxtView = (TextView) findViewById(R.id.tv_epg_program_event_name);
        TextView timeTxtView = (TextView) findViewById(R.id.tv_epg_program_event_time);
        TextView descriptionTextView = (TextView) findViewById(R.id.tv_epg_program_event_detail_description);
        //Button exitButton = (Button) findViewById(R.id.btn_epg_exit);

        // Detail description information view obtain focus as default
        descriptionTextView.requestFocus();

        // Show empty string as default action
        String name = "";
        String startDateString = "";
        String endDateString = "";
        String shortDescription = "";
        String detailDescription = "";
        String parentalDescription = "";
        String genreDescription = "";
        if (null != mEPGEvent)
        {
            // Obtain program event name,time and detail description informations while object not
            // null
            name = mEPGEvent.getEventName();
            if (null == name)
            {
                name = "";
            }
            LogTool.d(LogTool.MEPG, "name = " + name);

            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());


            Calendar startCal = mEPGEvent.getStartTimeCalendar();
            if (null != startCal)
            {

                int hour = startCal.get(Calendar.HOUR_OF_DAY);
                int min = startCal.get(Calendar.MINUTE);
                startDateString = String.format("%02d", hour) + ":" + String.format("%02d", min);
            }

            if (startDateString.length() > 0)
            {
                // Add separate string between start time and end time while start time string is
                // not empty
                startDateString += "-";
            }
            Calendar endCal = mEPGEvent.getEndTimeCalendar();
            if (null != endCal)
            {
                int hour = endCal.get(Calendar.HOUR_OF_DAY);
                int min = endCal.get(Calendar.MINUTE);
                endDateString = String.format("%02d", hour) + ":" + String.format("%02d", min);
            }

            shortDescription = mEPGEvent.getShortDescription();
            if ((null != shortDescription) && (shortDescription.length() > 0))
            {
                shortDescription += System.getProperty("line.separator");
            }
            else
            {
                shortDescription = "";
            }
            LogTool.d(LogTool.MEPG, "shortDescription = " + shortDescription);

            IExtendedDescription extendedDescription = mEPGEvent.getExtendedDescription();
            if (null != extendedDescription)
            {
                detailDescription = extendedDescription.getDetailDescription();
                if (null == detailDescription)
                {
                    detailDescription = "";
                }
            }
            LogTool.d(LogTool.MEPG, "detailDescription = " + detailDescription);

            int parentalRating = 0;
            String strCountry = "";
            String strRating = "";

            parentalRating = mEPGEvent.getParentLockLevel();
            strCountry = mEPGEvent.getParentCountryCode();

            LogTool.d(LogTool.MEPG, "strCountry = " + strCountry + " parentalRating = " + parentalRating);
            if (parentalRating != 0)
            {
                strRating = mContext.getResources().getString(R.string.epg_parent_lock_desc_age);

                parentalDescription += "\n";
                parentalDescription += mContext.getResources().getString(R.string.epg_parent_lock_desc_title);
                if (strCountry.equals("BRA") || strCountry.equals("bra"))
                {
                    int age = parentalRating & 0xf;
                    int content = (parentalRating >> 4) & 0xf;

                    if ((age <= 1) || (age >= 7))
                    {
                        age = 0;
                    }
                    else
                    {
                        age = (age + 3) * 2;
                    }

                    if (age < 10)
                    {
                        //less than 10 years old
                        parentalDescription += mContext.getResources().getString(R.string.epg_parent_lock_desc_less);
                    }
                    else
                    {
                        //at least (age) years old
                        parentalDescription += String.format(strRating, age);
                    }

                    if (content != 0)
                    {
                        int contenNum = 0;
                        parentalDescription += ", ";
                        parentalDescription += mContext.getResources().getString(R.string.epg_parent_lock_content);
                        if ((content & 0x1) != 0)
                        {
                            parentalDescription += mContext.getResources().getString(R.string.epg_parent_lock_desc_drugs);
                            contenNum++;
                        }
                        if ((content & 0x2) != 0)
                        {
                            if (contenNum != 0)
                            {
                                parentalDescription += ", ";
                            }
                            parentalDescription += mContext.getResources().getString(R.string.epg_parent_lock_desc_violence);
                            contenNum++;

                        }
                        if ((content & 0x4) != 0)
                        {
                            if (contenNum != 0)
                            {
                                parentalDescription += ", ";
                            }
                            parentalDescription += mContext.getResources().getString(R.string.epg_parent_lock_desc_sex);
                        }
                    }
                }
                else
                {
                    parentalDescription += String.format(strRating, parentalRating);
                }
            }

            LogTool.d(LogTool.MEPG, "parentalDescription = " + parentalDescription);

            genreDescription += getGenreDescription();
        }

        nameTxtView.setText(name);
        timeTxtView.setText(String.format("%s%s", startDateString, endDateString));
        descriptionTextView.setText(String.format("%s%s%s%s", shortDescription, detailDescription, parentalDescription, genreDescription));

    }

    private String getGenreDescription()
    {
        String genreDescription = "";
        //genre
        int levelOne = mEPGEvent.getContentLevel1();
        LogTool.d(LogTool.MEPG, "levelOne = " + levelOne);
        if (levelOne > 15 && levelOne < 25)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_movie);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 16];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 31 && levelOne < 37)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_new);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 32];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 47 && levelOne < 52)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_game);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 48];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 63 && levelOne < 76)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_sport);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 64];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 79 && levelOne < 86)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_child);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 80];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 95 && levelOne < 103)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_music);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 96];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 111 && levelOne < 124)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_art);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 112];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 127 && levelOne < 132)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_social);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 128];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 143 && levelOne < 152)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_edu);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 142];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 159 && levelOne < 168)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_lei);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 160];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 175 && levelOne < 180)
        {
            String[] sports = mContext.getResources().getStringArray(R.array.epg_content_spe);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 176];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }

        return genreDescription;
    }

}
