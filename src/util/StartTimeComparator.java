package util;

import model.Task;

import java.time.LocalDateTime;
import java.util.Comparator;

public class StartTimeComparator implements Comparator<Task> {

    @Override
    public int compare(Task o1, Task o2) {
        LocalDateTime time1 = o1.getStartTime();
        LocalDateTime time2 = o2.getStartTime();
        int result = time1.compareTo(time2);
        if (result != 0) return result;
        return Integer.compare(o1.getTaskID(), o2.getTaskID());//
    }
}
