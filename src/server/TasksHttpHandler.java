package server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import controllers.TaskManager;
import exceptions.IntersectTaskException;
import model.Task;
import util.TaskType;

import java.io.IOException;
import java.util.List;

public class TasksHttpHandler extends BaseHttpHandler {

    public TasksHttpHandler(TaskManager taskManager) {
        super(taskManager);
    }


    @Override
    protected void processGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getTasks();
            String responseText = gson.toJson(tasks);
            sendText(exchange, responseText);   //code 200
        } else {
            int taskId = parsePathId(path);
            if (taskId < 0) {
                sendResponse(exchange, 400, "Bad Request");
                return;
            }
            Task task = taskManager.getTaskById(taskId);
            if (task != null) {
                String responseText = gson.toJson(task);
                sendText(exchange, responseText);  //code 200
            } else {
                sendNotFound(exchange, "Task Not Found");
            }
        }
    }

    @Override
    protected void processPost(HttpExchange exchange, String path) throws IOException {
        if (!path.equals("/tasks")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        String body = readRequestBody(exchange);
        if (body.isEmpty()) {
            sendResponse(exchange, 400, "Empty request body");
            return;
        }
        try {
            Task task = gson.fromJson(body, Task.class);
            if (task == null || task.getName() == null || task.getDescription() == null || task.getStatus() == null) {
                sendResponse(exchange, 400, "Invalid task JSON");
                return;
            }
            int id = task.getTaskID();
            Task existing = taskManager.getTaskById(id);

            if (id != 0 && existing != null) {
                taskManager.updateTask(task);
                sendText(exchange, "Task updated successfully");  //code 200
            } else {
                int createdId = taskManager.addTask(task);
                sendCreated(exchange, "Task created successfully, Task ID: " + createdId);  //code 201
            }
        } catch (JsonSyntaxException e) {
            sendResponse(exchange, 400, "Malformed JSON: " + e.getMessage());
        } catch (IntersectTaskException e) {
            sendHasInteractions(exchange, "Task time intersects with existing task: " + e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    @Override
    protected void processDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            taskManager.removeAllTasks(TaskType.TASK);
            sendText(exchange, "All tasks deleted");  //code 200
        } else {
            int taskId = parsePathId(path);
            if (taskId < 0) {
                sendResponse(exchange, 400, "Bad Request");
                return;
            }
            Task task = taskManager.getTaskById(taskId);
            if (task == null) {
                sendNotFound(exchange, "Task Not Found");
            } else {
                taskManager.removeTaskById(taskId);
                sendText(exchange, "Task deleted");   //code 200
            }
        }
    }
}
