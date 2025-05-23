package model;

import util.TaskStatus;
import util.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends model.Task {
    private ArrayList<Integer> subtaskIdList;
    private LocalDateTime endTime;


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

    @Override
    public String toString() {
        return super.toString();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    //Время начала — дата старта самой ранней подзадачи
    public LocalDateTime getStartTime(List<Subtask> subtasks) {
        return subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    //время завершения — время окончания самой поздней из задач
    public LocalDateTime getEndTime(List<Subtask> subtasks) {
        return subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    //Продолжительность эпика — сумма продолжительностей всех его подзадач.
    public Duration getDuration(List<Subtask> subtasks) {
        long totalDurationMinutes = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();
        return Duration.ofMinutes(totalDurationMinutes);
    }

    public TaskStatus updateStatus(List<Subtask> subtasks) {
        long newCounter = subtasks.stream()
                .map(Subtask::getStatus)
                .filter(status -> status.equals(TaskStatus.NEW))
                .count();

        long doneCounter = subtasks.stream()
                .map(Subtask::getStatus)
                .filter(status -> status.equals(TaskStatus.DONE))
                .count();

        TaskStatus result;
        if (newCounter == subtasks.size()) {
            result = TaskStatus.NEW;
        } else if (doneCounter == subtasks.size()) {
            result = TaskStatus.DONE;
        } else {
            result = TaskStatus.IN_PROGRESS;
        }
        return result;
    }

}