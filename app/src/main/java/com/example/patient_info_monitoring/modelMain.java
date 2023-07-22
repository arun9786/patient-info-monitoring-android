package com.example.patient_info_monitoring;

public class modelMain {
    String name,lastVisited;
    Boolean medicine,reports;

    public modelMain(){

    }

    public modelMain(String name, String lastVisited, Boolean medicine, Boolean reports) {
        this.name = name;
        this.lastVisited = lastVisited;
        this.medicine = medicine;
        this.reports = reports;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastVisited() {
        return lastVisited;
    }

    public void setLastVisited(String lastVisited) {
        this.lastVisited = lastVisited;
    }

    public Boolean getMedicine() {
        return medicine;
    }

    public void setMedicine(Boolean medicine) {
        this.medicine = medicine;
    }

    public Boolean getReports() {
        return reports;
    }

    public void setReports(Boolean reports) {
        this.reports = reports;
    }
}
