package com.example.fractalrangecalculator.Util;

import java.time.LocalDate;

public class DateStringFormatter {
    public static String getFormattedDateString(LocalDate localDate){

        String dateString = localDate.getYear() + "-";
        if(localDate.getMonthValue()<10){
            dateString = dateString + "0" + localDate.getMonthValue();
        }else{
            dateString = dateString + localDate.getMonthValue();
        }

        dateString = dateString + "-";
        if(localDate.getDayOfMonth()<10){
            dateString = dateString + "0" + localDate.getDayOfMonth();
        }else{
            dateString = dateString + localDate.getDayOfMonth();
        }
        return dateString;
    }
}
