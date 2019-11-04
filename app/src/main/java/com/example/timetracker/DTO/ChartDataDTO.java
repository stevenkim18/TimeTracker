package com.example.timetracker.DTO;

import java.util.Date;

public class ChartDataDTO {

    Date date;
    String textDate;
    String categoryName;
    int durationTime;
    int color;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTextDate() {
        return textDate;
    }

    public void setTextDate(String textDate) {
        this.textDate = textDate;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(int durationTime) {
        this.durationTime = durationTime;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
