/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.core.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author fhenry
 */
public class DateUtils 
{    
    public DateUtils()
    {}

    public static long parseDate(String date) throws Exception
    {
    	String strDate = DateUtils.dateHourCompletion(date);
    	long timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").parse(strDate).getTime();
    	return timestamp;
    }
    
    private static String dateHourCompletion(String date) throws Exception
    {
    	String[] space = Utils.splitNoRegex(date, " ");
    	String[] twopoints = null;
    	String[] slash = null;

        boolean dayToComplete = true;
        boolean monthToComplete = true;
        boolean yearToComplete = true;
        
        boolean hourToComplete = false;
        boolean minuteToComplete = false;
        boolean secondToComplete = false;
        boolean milliToComplete = false;
    	
    	switch(space.length)
        {
            case 0 :
                throw new RuntimeException("unsupported date");
            case 1 ://if just date or hour
                if (space[0].contains("/")){//if date
                    slash = Utils.splitNoRegex(space[0], "/");
                    twopoints = new String[0];
                }
                else {//if hour, minute, and by default if its only a number, its considered as an hour
                    slash = new String[0];
                    twopoints = Utils.splitNoRegex(space[0], ":");
                }
                break;
            case 2 ://if date and hour are present
                slash = Utils.splitNoRegex(space[0], "/");
                twopoints = Utils.splitNoRegex(space[1], ":");
                break;
    	}

        //hour::minute::seconds::milliseconds
        //tp[0]::tp[1]::tp[2]::tp[3]
    	String[] tp = new String[4];
        tp[3] = "000";
        tp[2] = "00";
        tp[1] = "00";
        tp[0] = "00";
        
        if ((twopoints.length > 0) && (ValueOfString(twopoints[0])>23))
            throw new Exception("hour must be inferior to 23");
        else if ((twopoints.length > 1) &&(ValueOfString(twopoints[1])>59))
            throw new Exception("minute must be inferior to 59");
        else if ((twopoints.length > 2) &&(ValueOfString(twopoints[2])>59))
            throw new Exception("second must be inferior to 59");
        else if ((twopoints.length > 3) &&(ValueOfString(twopoints[3])>999))
            throw new Exception("millisecond must be inferior to 999");

        if(twopoints.length > 0)
        {
            if(twopoints[0].length() == 0)
                hourToComplete = true;
            else if(twopoints[0].length() == 1)
                tp[0]= "0"+twopoints[0];
            else
                tp[0]= twopoints[0];
        }
        if(twopoints.length > 1)
        {
            if(twopoints[1].length() == 0)
                minuteToComplete = true;
            else if(twopoints[1].length() == 1)
                tp[1]= "0"+twopoints[1];
            else
                tp[1]= twopoints[1];
        }
        if(twopoints.length > 2)
        {
            if(twopoints[2].length() == 0)
                secondToComplete = true;
            else if(twopoints[2].length() == 1)
                tp[2]= "0"+twopoints[2];
            else
                tp[2]= twopoints[2];
        }
        if(twopoints.length > 3)
        {
            if(twopoints[3].length() == 0)
                milliToComplete = true;
            else if(twopoints[3].length() == 1)
                tp[3] = "00" + twopoints[3];
            else if (twopoints[3].length() == 2)
                tp[3] = "0" + twopoints[3];
            else
                tp[3] = twopoints[3];
        }

        if(hourToComplete && minuteToComplete && secondToComplete)
        {
            //important to begin by the lsb
            tp[2] = hourCompletion(false, 0, false, 0, false, 0, true, Integer.valueOf(tp[3]), "second");
            tp[1] = hourCompletion(false, 0, false, 0, true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "minute");
            tp[0] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "hour");
        }
        else if(hourToComplete && minuteToComplete && milliToComplete)
        {
            //important to begin by the lsb
            tp[3] = hourCompletion(false, 0, false, 0, true, Integer.valueOf(tp[2]), false, 0, "milli");
            tp[1] = hourCompletion(false, 0, false, 0, true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "minute");
            tp[0] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "hour");
        }
        else if(hourToComplete && secondToComplete && milliToComplete)
        {
            //important to begin by the lsb
            tp[3] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), false, 0, false, 0, "milli");
            tp[2] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), false, 0, true, Integer.valueOf(tp[3]), "second");
            tp[0] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "hour");
        }
        else if(minuteToComplete && secondToComplete && milliToComplete)
        {
            //important to begin by the lsb
            tp[3] = hourCompletion(true, Integer.valueOf(tp[0]), false, 0, false, 0, false, 0, "milli");
            tp[2] = hourCompletion(true, Integer.valueOf(tp[0]), false, 0, false, 0, true, Integer.valueOf(tp[3]), "second");
            tp[1] = hourCompletion(true, Integer.valueOf(tp[0]), false, 0, true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "minute");
        }
        else if(hourToComplete && minuteToComplete)
        {
            //important to begin by minute(the lsb)
            tp[1] = hourCompletion(false, 0, false, 0, true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "minute");
            tp[0] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "hour");
        }
        else if(hourToComplete && secondToComplete)
        {
            //important to begin by second(the lsb)
            tp[2] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), false, 0, true, Integer.valueOf(tp[3]), "second");
            tp[0] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "hour");
        }
        else if(hourToComplete && milliToComplete)
        {
            //important to begin by second(the lsb)
            tp[3] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), false, 0, "milli");
            tp[0] = hourCompletion(false, 0, true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "hour");
        }
        else if(minuteToComplete && secondToComplete)
        {
            //important to begin by second(the lsb)
            tp[2] = hourCompletion(true, Integer.valueOf(tp[0]), false, 0, false, 0, true, Integer.valueOf(tp[3]), "second");
            tp[1] = hourCompletion(true, Integer.valueOf(tp[0]), false, 0, true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "minute");
        }
        else if(minuteToComplete && milliToComplete)//TODO:check
        {
            //important to begin by second(the lsb)
            tp[3] = hourCompletion(true, Integer.valueOf(tp[0]), false, 0, true, Integer.valueOf(tp[2]), false, 0, "milli");
            tp[1] = hourCompletion(true, Integer.valueOf(tp[0]), false, 0, true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "minute");
        }
        else if(secondToComplete && milliToComplete)
        {
            //important to begin by second(the lsb)
            tp[3] = hourCompletion(true, Integer.valueOf(tp[0]), true, Integer.valueOf(tp[1]), false, 0, false, 0, "milli");
            tp[2] = hourCompletion(true, Integer.valueOf(tp[0]), true, Integer.valueOf(tp[1]), false, 0, true, Integer.valueOf(tp[3]), "second");
        }
        else if(hourToComplete)
            tp[0]= hourCompletion(false, 0, true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "hour");
        else if(minuteToComplete)
            tp[1]= hourCompletion(true, Integer.valueOf(tp[0]), false, 0, true, Integer.valueOf(tp[2]), true, Integer.valueOf(tp[3]), "minute");
        else if(secondToComplete)
            tp[2]= hourCompletion(true, Integer.valueOf(tp[0]), true, Integer.valueOf(tp[1]), false, 0, true, Integer.valueOf(tp[3]), "second");
        else if(milliToComplete)
            tp[3]= hourCompletion(true, Integer.valueOf(tp[0]), true, Integer.valueOf(tp[1]), true, Integer.valueOf(tp[2]), false, 0, "milli");


        String[] sl = new String[3];

        if((slash.length > 0) && (ValueOfString(slash[0]) > 31) && (ValueOfString(slash[0]) <= 0))
            throw new Exception("day must be in the range [1-31]");
        else if((slash.length > 1) && (ValueOfString(slash[1]) > 12) && (ValueOfString(slash[1]) <= 0))
            throw new Exception("month must be in the range [1-12]");
        else if((slash.length > 2) && (ValueOfString(slash[2]) > 9999) && (ValueOfString(slash[2]) < 0))
            throw new Exception("year must be in the range [0-9999]");

        if(slash.length > 0)
        {
            if(slash[0].length() == 1)
            {
                sl[0]= "0"+slash[0];
                dayToComplete = false;
            }
            else if(slash[0].length() == 2)
            {
                sl[0]= slash[0];
                dayToComplete = false;
            }
        }
        if(slash.length > 1)
        {
            if(slash[1].length() == 1)
            {
                sl[1]= "0"+slash[1];
                monthToComplete = false;
            }
            else if(slash[1].length() == 2)
            {
                sl[1]= slash[1];
                monthToComplete = false;
            }
        }
        if(slash.length > 2)
        {
            if(slash[2].length() == 1)
            {
                sl[2]= "200"+slash[2];
                yearToComplete = false;
            }
            else if(slash[2].length() == 2)
            {
                sl[2]= "20"+slash[2];
                yearToComplete = false;
            }
            else if(slash[2].length() == 3)
            {
                throw new Exception("Year must be given in the format yyyy or yy");
            }
            else if (slash[2].length() == 4)// convert date in format 2009 in format 09
            {
                sl[2]= slash[2];
                yearToComplete = false;
            }
        }

        if(dayToComplete && monthToComplete && yearToComplete)
        {
            //important to begin by the lsb
            sl[0]= dateCompletion(tp, false, 0, false, 0, false, 0, "day");
            sl[1]= dateCompletion(tp, true, Integer.valueOf(sl[0]), false, 0, false, 0, "month");
            sl[2]= dateCompletion(tp, true, Integer.valueOf(sl[0]), true, Integer.valueOf(sl[1]), false, 0, "year");
        }
        else if(dayToComplete && monthToComplete)
        {
            //important to begin by the lsb
            sl[0]= dateCompletion(tp, false, 0, false, 0, true, Integer.valueOf(sl[2]), "day");
            sl[1]= dateCompletion(tp, true, Integer.valueOf(sl[0]), false, 0, true, Integer.valueOf(sl[2]), "month");
        }
        else if(dayToComplete && yearToComplete)
        {
            //important to begin by the lsb
            sl[0]= dateCompletion(tp, false, 0, true, Integer.valueOf(sl[1]), false, 0, "day");
            sl[2]= dateCompletion(tp, true, Integer.valueOf(sl[0]), true, Integer.valueOf(sl[1]), false, 0, "year");
        }
        else if(monthToComplete && yearToComplete)
        {
            //important to begin by the lsb
            sl[1]= dateCompletion(tp, true, Integer.valueOf(sl[0]), false, 0, false, 0, "month");
            sl[2]= dateCompletion(tp, true, Integer.valueOf(sl[0]), true, Integer.valueOf(sl[1]), false, 0, "year");
        }
        else if(dayToComplete)
        {
            //important to begin by the lsb
            sl[0]= dateCompletion(tp, false, 0, true, Integer.valueOf(sl[1]), true, Integer.valueOf(sl[2]), "day");
        }
        else if(monthToComplete)
        {
            //important to begin by the lsb
            sl[1]= dateCompletion(tp, true, Integer.valueOf(sl[0]), false, 0, true, Integer.valueOf(sl[2]), "month");
        }
        else if(yearToComplete)
        {
            sl[2]= dateCompletion(tp, true, Integer.valueOf(sl[0]), true, Integer.valueOf(sl[1]), false, 0, "year");
        }
        
    	date = sl[0] + "/" + sl[1] + "/" + sl[2] + " " + tp[0] + ":" + tp[1] + ":" + tp[2] + ":" + tp[3];
        return date;
	}

	private static String dateCompletion(String hour[], boolean dayPresent, int day, boolean monthPresent, int month, boolean yearPresent, int year, String choice)
	{
        if (!choice.equals("year") && !choice.equals("month") && !choice.equals("day"))
            throw new RuntimeException("Unrecognize parameter 'choice'");

    	month = month - 1;//to be compliant with Calendar class in java(from 0 to 11 in month)
    	String result = new String();
    	Calendar testCalendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        
    	if (dayPresent) {
    		if (monthPresent) {
    			if (yearPresent) //all is set, but check if the date is in the pass
                    calendar.set(year, month, day, Integer.parseInt(hour[0]), Integer.parseInt(hour[1]), Integer.parseInt(hour[2]));
    			else //year is not present
                    calendar.set(testCalendar.get(Calendar.YEAR), month, day, Integer.parseInt(hour[0]), Integer.parseInt(hour[1]), Integer.parseInt(hour[2]));
    		}
    		else {
    			if (yearPresent) //month is not present
                    calendar.set(year, testCalendar.get(Calendar.MONTH), day, Integer.parseInt(hour[0]), Integer.parseInt(hour[1]), Integer.parseInt(hour[2]));
    			else //year and month are not present
                    calendar.set(testCalendar.get(Calendar.YEAR), testCalendar.get(Calendar.MONTH), day, Integer.parseInt(hour[0]), Integer.parseInt(hour[1]), Integer.parseInt(hour[2]));
    		}
    	}
    	else {
    		if (monthPresent) {
    			if (yearPresent) //day is not present
                    calendar.set(year, month, testCalendar.get(Calendar.DAY_OF_MONTH), Integer.parseInt(hour[0]), Integer.parseInt(hour[1]), Integer.parseInt(hour[2]));
    			else //year and day are not present
                    calendar.set(testCalendar.get(Calendar.YEAR), month, testCalendar.get(Calendar.DAY_OF_MONTH), Integer.parseInt(hour[0]), Integer.parseInt(hour[1]), Integer.parseInt(hour[2]));
    		}
    		else {
    			if (yearPresent) //month and day are not present
                    calendar.set(year, testCalendar.get(Calendar.MONTH), testCalendar.get(Calendar.DAY_OF_MONTH), Integer.parseInt(hour[0]), Integer.parseInt(hour[1]), Integer.parseInt(hour[2]));
    			else //nothing is present
                    calendar.set(testCalendar.get(Calendar.YEAR), testCalendar.get(Calendar.MONTH), testCalendar.get(Calendar.DAY_OF_MONTH), Integer.parseInt(hour[0]), Integer.parseInt(hour[1]), Integer.parseInt(hour[2]));
    		}
    	}

        if(calendar.before(testCalendar))//if calendar is before currentCalendar
        {
            if(choice.equals("year"))
            {
                currentCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR) + 1);
                result = String.valueOf(currentCalendar.get(Calendar.YEAR));
            }
            else if (choice.equals("month"))
            {
                currentCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH) + 2);
                result = String.valueOf(currentCalendar.get(Calendar.MONTH));
            }
            else if (choice.equals("day"))
            {
                //check if today or tommorow with the hour
                calendar.clear();
                calendar.set(0, 0, 0, Integer.valueOf(hour[0]), Integer.valueOf(hour[1]), Integer.valueOf(hour[2]));
                testCalendar.set(0, 0, 0);
                if(calendar.before(testCalendar))//if calendar is after currentCalendar, set the new day
                    currentCalendar.set(Calendar.DAY_OF_MONTH, currentCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                result = String.valueOf(currentCalendar.get(Calendar.DAY_OF_MONTH));
            }
        }
        else
        {
            if(choice.equals("year"))
            {
                if(yearPresent)
                    result = String.valueOf(year);
                else
                    result = String.valueOf(currentCalendar.get(Calendar.YEAR));
            }
            else if (choice.equals("month"))
            {
                if(monthPresent)
                    result = String.valueOf(month+1);
                else
                    result = String.valueOf(currentCalendar.get(Calendar.MONTH) + 1);
            }
            else if (choice.equals("day"))
            {
                if(dayPresent)
                    result = String.valueOf(day);
                else
                    result = String.valueOf(currentCalendar.get(Calendar.DAY_OF_MONTH));
            }
        }

    	return result;
    }
	
	private static String hourCompletion(boolean hourPresent, int hour, boolean minutePresent, int minute, boolean secondPresent, int second, boolean milliPresent, int milli, String choice)
	{
		String result = new String();
        if (!choice.equals("hour") && !choice.equals("minute") && !choice.equals("second") && !choice.equals("milli"))
            throw new RuntimeException("Unrecognize parameter 'choice'");

        Calendar calendar = Calendar.getInstance();
		int state;
        
		if (hourPresent) {
			if (minutePresent) {
				if (secondPresent) {
					if (milliPresent) 
						state=0;
					else 
						state=1;//milli is not present
				}
				else {
					if (milliPresent) 
						state=2;//second is not present
					else 
						state=3;//milli and second are not present
				}
			}
			else {
				if (secondPresent) {
					if (milliPresent) 
						state=4;//minute is not present
					else 
						state=5;//milli and minute are not present
				}
				else {
					if (milliPresent) 
						state=6;//second and minute are not present
					else 
						state=7;//milli, second and minute are not present
				}
			}
		}
		else {
			if (minutePresent) {
				if (secondPresent) {
					if (milliPresent) 
						state=8;//hour is not present
					else 
						state=9;//milli and hour are not present
				}
				else {
					if (milliPresent) 
						state=10;//hour and second are not present
					else 
						state=11;//milli, second and hour are not present
				}
			}
			else {
				if (secondPresent) {
					if (milliPresent) 
						state=12;//hour and minute are not present
					else
						state=13;//milli, minute and hour are not present
				}
				else {
					if (milliPresent) 
						state=14;//second, minute and hour are not present
					else 
						state=15;//nothing is not present
				}
			}
		}
		switch (state) {
            case 0 :
                if (choice.equals("hour")) {
                    result=String.valueOf(hour);
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(minute);
                }
                else if (choice.equals("second")) {
                    result=String.valueOf(second);
                }
                else if (choice.equals("milli")) {
                    result=String.valueOf(milli);
                }
                break;
            case 1 ://milli is not present
                if (choice.equals("hour")) {
                    result=String.valueOf(hour);
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(minute);
                }
                else if (choice.equals("second")) {
                    result=String.valueOf(second);
                }
                else if (choice.equals("milli")) {
                    result="000";
                }
                break;
            case 2 ://second is not present
                if (choice.equals("hour")) {
                    result=String.valueOf(hour);
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(minute);
                }
                else if (choice.equals("second")) {
                    result="00";
                }
                else if (choice.equals("milli")) {
                    result=String.valueOf(milli);
                }
                break;
            case 3 ://milli and second are not present
                if (choice.equals("hour")) {
                    result=String.valueOf(hour);
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(minute);
                }
                else if (choice.equals("second")) {
                    result="00";
                }
                else if (choice.equals("milli")) {
                    result="000";
                }
                break;
            case 4 ://minute is not present
                if (choice.equals("hour")) {
                    result=String.valueOf(hour);
                }
                else if (choice.equals("minute")) {
                    if (calendar.get(Calendar.HOUR_OF_DAY)<hour || calendar.get(Calendar.HOUR_OF_DAY)>hour) {
                        result="00";
                    }else {
                        if (calendar.get(Calendar.SECOND)<second) {
                            result=String.valueOf(calendar.get(Calendar.MINUTE));
                        }
                        else {
                            result=String.valueOf(calendar.get(Calendar.MINUTE)+1);
                        }
                    }
                }
                else if (choice.equals("second")) {
                    result=String.valueOf(second);
                }
                else if (choice.equals("milli")) {
                    result=String.valueOf(milli);
                }
                break;
            case 5 ://milli and minute are not present
                if (choice.equals("hour")) {
                    result=String.valueOf(hour);
                }
                else if (choice.equals("minute")) {
                    if (calendar.get(Calendar.SECOND)<second) {
                        result=String.valueOf(calendar.get(Calendar.MINUTE));
                    }
                    else {
                        result=String.valueOf(calendar.get(Calendar.MINUTE)+1);
                    }

                }
                else if (choice.equals("second")) {
                    result=String.valueOf(second);
                }
                else if (choice.equals("milli")) {
                    result="000";
                }
                break;
            case 6 ://second and minute are not present
                if (choice.equals("hour")) {
                    result=String.valueOf(hour);
                }
                else if (choice.equals("minute")) {
                    if (calendar.get(Calendar.HOUR_OF_DAY)==0) {
                        if (calendar.get(Calendar.SECOND)<second) {
                            result=String.valueOf(calendar.get(Calendar.MINUTE));
                        }
                        else {
                            result=String.valueOf(calendar.get(Calendar.MINUTE)+1);
                        }
                    }else {
                        result="00";
                    }
                }
                else if (choice.equals("second")) {
                    if (calendar.get(Calendar.HOUR_OF_DAY)==hour) {
                        result=String.valueOf(calendar.get(Calendar.SECOND)+1);
                    }else {
                        result="00";
                    }
                }
                else if (choice.equals("milli")) {
                    result=String.valueOf(milli);
                }
                break;
            case 7 ://milli, second, minute are not present
                if (choice.equals("hour")) {
                    result=String.valueOf(hour);
                }
                else if (choice.equals("minute")) {
                    result="00";
                }
                else if (choice.equals("second")) {
                    result="00";
                }
                else if (choice.equals("milli")) {
                    result="000";
                }
                break;
            case 8 ://hour is not present
                if (choice.equals("hour")) {
                    if (calendar.get(Calendar.MINUTE)<minute || (calendar.get(Calendar.MINUTE)==minute && (calendar.get(Calendar.SECOND)<=second))) {
                        result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                    }
                    else {
                        result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)+1);
                    }
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(minute);
                }
                else if (choice.equals("second")) {
                    result=String.valueOf(second);
                }
                else if (choice.equals("milli")) {
                    result=String.valueOf(milli);
                }
                break;
            case 9 ://milli and hour are not present
                if (choice.equals("hour")) {
                    if (calendar.get(Calendar.MINUTE)<minute || (calendar.get(Calendar.MINUTE)==minute && (calendar.get(Calendar.SECOND)<=second))) {
                        result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                    }
                    else {
                        result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)+1);
                    }
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(minute);
                }
                else if (choice.equals("second")) {
                    result=String.valueOf(second);
                }
                else if (choice.equals("milli")) {
                    result="000";
                }
                break;
            case 10 ://hour and second are not present
                if (choice.equals("hour")) {
                    if (calendar.get(Calendar.MINUTE)<minute || (calendar.get(Calendar.MINUTE)==minute && (calendar.get(Calendar.SECOND)==second))) {
                        result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                    }
                    else {
                        result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)+1);
                    }
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(minute);
                }
                else if (choice.equals("second")) {
                    result="00";
                }
                else if (choice.equals("milli")) {
                    result=String.valueOf(milli);
                }
                break;
            case 11 ://milli, second and hour are not present
                if (choice.equals("hour")) {
                    if (calendar.get(Calendar.MINUTE)<minute) {
                        result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                    }
                    else{
                        result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)+1);
                    }
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(minute);
                }
                else if (choice.equals("second")) {
                    result="00";
                }
                else if (choice.equals("milli")) {
                    result="000";
                }
                break;
            case 12 :
                if (choice.equals("hour")) {
                    result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                }
                else if (choice.equals("minute")) {
                    if (calendar.get(Calendar.SECOND)<second) {
                        result=String.valueOf(calendar.get(Calendar.MINUTE));
                    }
                    else {
                        result=String.valueOf(calendar.get(Calendar.MINUTE)+1);
                    }
                }
                else if (choice.equals("second")) {
                    result=String.valueOf(second);
                }
                else if (choice.equals("milli")) {
                    result=String.valueOf(milli);
                }
                break;
            case 13 :
                if (choice.equals("hour")) {
                    result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                }
                else if (choice.equals("minute")) {
                    if (calendar.get(Calendar.SECOND)<second) {
                        result=String.valueOf(calendar.get(Calendar.MINUTE));
                    }
                    else{
                        result=String.valueOf(calendar.get(Calendar.MINUTE)+1);
                    }
                }
                else if (choice.equals("second")) {
                    result=String.valueOf(second);
                }
                else if (choice.equals("milli")) {
                    result="000";
                }
                break;
            case 14 :
                if (choice.equals("hour")) {
                    result=String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                }
                else if (choice.equals("minute")) {
                    result=String.valueOf(calendar.get(Calendar.MINUTE));
                }
                else if (choice.equals("second")) {
                    if (calendar.getTime().getTime()%1000<milli) {
                        result=String.valueOf(calendar.get(Calendar.SECOND));
                    }
                    else {
                        result=String.valueOf(calendar.get(Calendar.SECOND)+1);
                    }
                }
                else if (choice.equals("milli")) {
                    result=String.valueOf(milli);
                }
                break;
            case 15 ://nothing is present
                if (choice.equals("hour")) {
                    result="00";
                }
                else if (choice.equals("minute")) {
                    result="00";
                }
                else if (choice.equals("second")) {
                    result="00";
                }
                else if (choice.equals("milli")) {
                    result="000";
                }
                break;
		}
		return result;
	}	
	
	private static int ValueOfString(String value)
	{
		if (value.length()==0) 
			return 0;
		else 
			return Integer.valueOf(value);
	}
	
}
