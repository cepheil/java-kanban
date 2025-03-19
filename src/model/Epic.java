package model;

import util.TaskStatus;
import util.TaskType;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIdList;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.subtaskIdList = new ArrayList<>();
        this.taskType = TaskType.EPIC;
    }

    public void setSubtaskIdList(int subtaskId) {
        if (this.subtaskIdList == null) {
            this.subtaskIdList = new ArrayList<>();
        }
        this.subtaskIdList.add(subtaskId);
    }

    public ArrayList<Integer> getSubtaskIdList() {
        return subtaskIdList;
    }

    public void removeSubtaskId(int subtaskId) {
        this.subtaskIdList.remove(Integer.valueOf(subtaskId));
    }
    public void clearSubtaskIdList () {
        this.subtaskIdList.clear();
    }
}