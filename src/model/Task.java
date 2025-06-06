package model;

import util.CustomFormatter;
import util.TaskStatus;
import util.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    private int taskID;
    protected TaskType taskType;
    private String name;
    private String description;
    private TaskStatus status;
    private Duration duration;
    private LocalDateTime startTime;


    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.taskType = TaskType.TASK;
    }


    public Task(String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this(name, description, status);
        this.startTime = startTime;
        this.duration = duration;
    }

    //конструктор для копирования задач
    public Task(Task other) {
        this.name = other.name;
        this.description = other.description;
        this.status = other.status;
        this.taskType = other.taskType;
        this.taskID = other.taskID;
        this.startTime = other.startTime;
        this.duration = other.duration;
    }


    public TaskType getType() {
        return TaskType.TASK;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getTaskID() {
        return taskID;
    }

    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "[" + taskType + "]" + " [ID:" + taskID + "]" +
                " name: " + name + " [" + status + "] " +
                "Description: " + description + " [" +
                "StartTime: " + (startTime != null ? startTime.format(CustomFormatter.getFormatter()) : " ") +
                " Duration: " + (duration != null ? duration.toMinutes() : "0") +
                " EndTime: " + (getEndTime() != null ? getEndTime().format(CustomFormatter.getFormatter()) : " ") + "] " + "\n";
    }

    public String toCsv() {
        return taskID + "," +
                taskType + "," +
                name + "," +
                status + "," +
                description + "," +
                (startTime != null ? startTime.format(CustomFormatter.getFormatter()) : " ") + "," +
                (duration != null ? duration.toMinutes() : 0) + "," +
                (getEndTime() != null ? getEndTime().format(CustomFormatter.getFormatter()) : " ") + ", " + "\n";
    }

    ////sprint_8-solution-time-and-duration
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return (startTime != null && duration != null) ? startTime.plus(duration) : null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskID == task.taskID &&
                taskType == task.taskType &&
                Objects.equals(name, task.name) &&
                Objects.equals(description, task.description) &&
                status == task.status &&
                Objects.equals(duration, task.duration) &&
                Objects.equals(startTime, task.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskID, taskType, name, description, status, duration, startTime);
    }
}
