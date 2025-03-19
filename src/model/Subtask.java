package model;

import util.TaskStatus;
import util.TaskType;

public class Subtask extends Task {
    private int epicID;

    public Subtask(String name, String description, TaskStatus status, int epicID) {
        super(name, description, status);
        this.epicID = epicID;
        this.taskType = TaskType.SUBTASK;
    }

    public int getEpicID() {
        return epicID;
    }
}
