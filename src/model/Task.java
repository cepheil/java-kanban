package model;

import util.TaskStatus;
import util.TaskType;

import java.util.Objects;

public class Task {

    private int taskID;
    protected TaskType taskType;
    private String name;
    private String description;
    private TaskStatus status;


    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.taskType = TaskType.TASK;
    }

    //конструктор для копирования задач
    public Task(Task other) {
        this.name = other.name;
        this.description = other.description;
        this.status = other.status;
        this.taskType = other.taskType;
        this.taskID = other.taskID;
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
                "Description: " + description + "\n";

    }

    public String toCsv() {
        return taskID + "," +
                taskType + "," +
                name + "," +
                status + "," +
                description + ", " + "\n";
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
                status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskID, taskType, name, description, status);
    }
}
