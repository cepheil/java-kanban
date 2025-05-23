package model;

import util.TaskStatus;
import util.TaskType;

import java.time.format.DateTimeFormatter;

public class Subtask extends model.Task {
    private int epicID;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");


    public Subtask(String name, String description, TaskStatus status, int epicID) {
        super(name, description, status);
        this.epicID = epicID;
        this.taskType = TaskType.SUBTASK;
    }

    public Subtask(Subtask other) {
        super(other);
        this.epicID = other.epicID;
        this.taskType = TaskType.SUBTASK;
    }


    public int getEpicID() {
        return epicID;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public void setTaskID(int taskID) {
        super.setTaskID(taskID);
    }

    @Override
    public String toString() {
        return super.toString().trim() + " epicID: " + epicID + "\n";
    }

    @Override
    public String toCsv() {
        return getTaskID() + "," +
                taskType + "," +
                getName() + "," +
                getStatus() + "," +
                getDescription() + "," +
                (getStartTime() != null ? getStartTime().format(formatter) : " ") + "," +
                (getDuration() != null ? getDuration().toMinutes() : 0) + "," +
                (getEndTime() != null ? getEndTime().format(formatter) : " ") + "," +
                epicID + "\n";
    }


}
