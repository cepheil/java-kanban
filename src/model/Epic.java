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


    public Epic(Epic other) {
        super(other);
        this.subtaskIdList = new ArrayList<>(other.getSubtaskIdList());
        this.taskType = TaskType.EPIC;
    }


    public void setSubtaskIdList(int subtaskId) {
        if (subtaskId == this.getTaskID()) {
            return;
        }
        // проверка на дубликат айди в списке подзадач.
        if (this.subtaskIdList.contains(subtaskId)) {
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