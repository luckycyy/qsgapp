package com.cyy.qsg;

public class Student {
    private String name;
    private int imageId;
    private String id;
    private String arrive;
    private String arriveTime;
    private String grade;
    private String clazz;
    public Student(){
        this.name = "";
        this.imageId = 0;
    }
    public String getName() {
        return name;
    }
    public int getImageId() {
        return imageId;
    }
    public String getId() {
        return id;
    }
    public String getArriveTime() {
        return arriveTime;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setId(String stuId) {
        this.id = stuId;
    }
    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }
    public String getArrive() {
        return arrive;
    }
    public void setArrive(String arriveStatus) {
        this.arrive = arriveStatus;
    }
    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public String getClazz() {
        return clazz;
    }
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}