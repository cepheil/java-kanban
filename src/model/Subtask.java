package model;

import util.TaskStatus;
import util.TaskType;

public class Subtask extends model.Task {
    private int epicID;

    public Subtask(String name, String description, TaskStatus status, int epicID) {
        super(name, description, status);
        this.epicID = epicID;
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

    public void setEpicID(int epicID) {
        this.epicID = epicID;
    }
}
