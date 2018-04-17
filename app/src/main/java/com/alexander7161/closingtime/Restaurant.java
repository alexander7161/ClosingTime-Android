package com.alexander7161.closingtime;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import java.util.ArrayList;

/**
Restaurant Model object
 */
public class Restaurant
{
    private String name;
    private String address;
    private String longAddress;
    private ArrayList<String> closingTimes;
    private ArrayList<String> halfOffPeriods;
    private ArrayList<Long> percentOffs;


    /*
    Constructor - creating a Restaurant object from scratch
     */
    public Restaurant(String name, String address, String longAddress, ArrayList<String> closingTimes, ArrayList<Long> percentOffs, ArrayList<String> halfOffPeriods)
    {
        this.name = name;
        this.address = address;
        this.longAddress = longAddress;
        this.closingTimes = closingTimes;
        this.halfOffPeriods = halfOffPeriods;
        this.percentOffs = percentOffs;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getLongAddress() {return longAddress;}

    public String getClosingTime(int index) {
        if(index>closingTimes.size()-1|| closingTimes.get(index).equals("")) {
            return "No discount.";
        }
        return LocalTime.parse(closingTimes.get(index)).toString("HH:mm");
    }

    public String getCurrentDiscount() {
        LocalTime localTime = new LocalTime();
        DateTime dateTime = new DateTime();
        int index = dateTime.getDayOfWeek() - 1;
        try {
            if(closingTimes.get(index).equals("")) {
                return "Restaurant Closed Today";
            }
        } catch (IndexOutOfBoundsException e) {
            return "Restaurant Closed Today";
        } catch (NullPointerException e) {
            return e.toString();
        }
        long percentOff = percentOffs.get(index);
        LocalTime closingTime = LocalTime.parse(closingTimes.get(index));
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix(":")
                .appendMinutes()
                .toFormatter();
        Period dur = Period.parse(halfOffPeriods.get(index), formatter);
        LocalTime startHalfOff = closingTime.minus(dur);

        if(localTime.isAfter(startHalfOff) && localTime.isBefore(closingTime)) {
            return percentOff + "% off from " + startHalfOff.toString("HH:mm") + " to " + closingTime.toString("HH:mm");
        } else if (localTime.isBefore(startHalfOff)) {
            return percentOff + "% off starts at " + startHalfOff.toString("HH:mm");
        } else if (localTime.isAfter(closingTime)) {
            return percentOff + "% ended at " + closingTime.toString("HH:mm");
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        return getName() + " " +  getAddress() + " " + " Monday: " + getClosingTime(1);
    }
}
