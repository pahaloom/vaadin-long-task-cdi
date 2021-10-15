package org.vaadin.example;

import java.util.Objects;

public class TaskPojo {
    private final int id;
    private String result;

    static int EVIL_COUNTER = 0;

    public TaskPojo() {
        this.id = ++EVIL_COUNTER;
    }

    public int getId() {
        return id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskPojo taskPojo = (TaskPojo) o;
        return id == taskPojo.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TaskPojo{" +
                "id=" + id +
                ", result='" + result + '\'' +
                "} : " + super.toString();
    }
}
