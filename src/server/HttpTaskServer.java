package server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

import controllers.Managers;
import controllers.TaskManager;


public class HttpTaskServer {
    private final TaskManager taskManager;
    private static final int PORT = 8080;
    private HttpServer httpServer;


    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        setHandlers();
    }


    public void setHandlers() {
        httpServer.createContext("/tasks", new TasksHttpHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHttpHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHttpHandler(taskManager));
        httpServer.createContext("/history", new HistoryHttpHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHttpHandler(taskManager));
    }


    public void start() {
        System.out.println("Server started on port: " + PORT);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
        System.out.println("Server stopped on port: " + PORT);
    }

    public static void main(String[] args) {
        try {
            TaskManager taskManager = Managers.getDefault();
            HttpTaskServer server = new HttpTaskServer(taskManager);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
