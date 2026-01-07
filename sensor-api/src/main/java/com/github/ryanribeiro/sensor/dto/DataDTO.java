package com.github.ryanribeiro.sensor.dto;

public class DataDTO {
    private Integer year;
    private Integer month;
    private Integer day;
    private Integer hour;
    private Integer minute;
    private Integer second;
    private Integer nanoseconds;
    private String local;
    
    public DataDTO() {
    }

    
    
    public DataDTO(Integer year, Integer month, Integer day, Integer hour, Integer minute, Integer second,
            Integer nanoseconds, String local) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nanoseconds = nanoseconds;
        this.local = local;
    }



    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    public Integer getDay() {
        return day;
    }
    
    public void setDay(Integer day) {
        this.day = day;
    }
    
    public Integer getHour() {
        return hour;
    }
    
    public void setHour(Integer hour) {
        this.hour = hour;
    }
    
    public Integer getMinute() {
        return minute;
    }
    
    public void setMinute(Integer minute) {
        this.minute = minute;
    }
    
    public Integer getSecond() {
        return second;
    }
    
    public void setSecond(Integer second) {
        this.second = second;
    }

    public Integer getNanoseconds() {
        return nanoseconds;
    }
    
    public void setNanoseconds(Integer nanoseconds) {
        this.nanoseconds = nanoseconds;
    }
    
    public String getLocal() {
        return local;
    }
    
    public void setLocal(String local) {
        this.local = local;
    }
}
