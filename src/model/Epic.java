package model;

import util.TaskStatus;
import util.TaskType;

import java.util.ArrayList;

public class Epic extends model.Task {
    private ArrayList<Integer> subtaskIdList;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.subtaskIdList = new ArrayList<>();
        this.taskType = TaskType.EPIC;
    }

    public void setSubtaskIdList(int subtaskId) {
        if (subtaskId == this.getTaskID()) {
            return;
        }
        if (this.subtaskIdList == null) {
            this.subtaskIdList = new ArrayList<>();
        }
        this.subtaskIdList.add(subtaskId);
    }

    public void setSubtaskIdList(ArrayList<Integer> newSubtaskIdList) {
        this.subtaskIdList = newSubtaskIdList;
    }


    public ArrayList<Integer> getSubtaskIdList() {
        return subtaskIdList;
    }

    public void removeSubtaskId(int subtaskId) {
        this.subtaskIdList.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtaskIdList() {
        this.subtaskIdList.clear();
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
}