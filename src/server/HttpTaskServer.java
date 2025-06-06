package server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;


import com.google.gson.*;
import controllers.Managers;
import controllers.TaskManager;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;

public class HttpTaskServer {

    private final TaskManager taskManager;
    private static final int PORT = 8080;
    private HttpServer httpServer;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();


    public static Gson getGson() {
        return gson;
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        setHandlers();
    }


    public void setHandlers() {
        httpServer.createContext("/tasks", new TasksHttpHandler(taskManager, gson));
        httpServer.createContext("/subtasks", new SubtasksHttpHandler(taskManager, gson));
        httpServer.createContext("/epics", new EpicsHttpHandler(taskManager, gson));
        httpServer.createContext("/history", new HistoryHttpHandler(taskManager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHttpHandler(taskManager, gson));
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
            //server.stop();

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
