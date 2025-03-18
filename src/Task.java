import java.util.Objects;

public class Task {

    private final int taskID;
    protected TaskType taskType;
    private String name;
    private String description;
    private TaskStatus status;
    private static int counter = 0;


    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.taskID = ++counter;
        this.taskType = TaskType.TASK;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name) &&
                Objects.equals(description, task.description);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, description);
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
                " name: " + name + "[" + status + "]"  +
                "Description: " + description + "\n";
    }
}
