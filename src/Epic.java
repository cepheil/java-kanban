import java.util.ArrayList;

public class Epic extends Task{

    private ArrayList<Integer> subtaskIdList;

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
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
}