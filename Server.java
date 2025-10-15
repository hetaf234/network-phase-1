/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Network_Project;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SERVER (super simple, student style)
 * - No Server object
 * - All shared state is static
 * - Inventory seeded inline in main
 */
public class Server {

    // ===== shared state (static) =====
    public static final Map<String, Car> cars = new ConcurrentHashMap<>();                 // carId -> Car
    public static final Map<LocalDate, Set<String>> occupancyByDate = new ConcurrentHashMap<>(); // date -> set(carIds)

    private static final ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // ---- seed inventory inline (20 cars) ----
        // AUTO
        cars.put("A01", new Car("A01","AUTO",2));
        cars.put("A02", new Car("A02","AUTO",2));
        cars.put("A03", new Car("A03","AUTO",2));
        cars.put("A04", new Car("A04","AUTO",5));
        cars.put("A05", new Car("A05","AUTO",5));
        cars.put("A06", new Car("A06","AUTO",5));
        cars.put("A07", new Car("A07","AUTO",5));
        cars.put("A08", new Car("A08","AUTO",8));
        cars.put("A09", new Car("A09","AUTO",8));
        cars.put("A10", new Car("A10","AUTO",8));
        // MANUAL
        cars.put("M01", new Car("M01","MANUAL",2));
        cars.put("M02", new Car("M02","MANUAL",2));
        cars.put("M03", new Car("M03","MANUAL",2));
        cars.put("M04", new Car("M04","MANUAL",5));
        cars.put("M05", new Car("M05","MANUAL",5));
        cars.put("M06", new Car("M06","MANUAL",5));
        cars.put("M07", new Car("M07","MANUAL",5));
        cars.put("M08", new Car("M08","MANUAL",8));
        cars.put("M09", new Car("M09","MANUAL",8));
        cars.put("M10", new Car("M10","MANUAL",8));
        System.out.println("[Server] 20 cars added.");

        // ---- listen/accept ----
        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            System.out.println("[Server] Listening on 9090...");
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("[Server] Client connected");
                ClientHandler ch = new ClientHandler(client, clients); // CHANGED: no Server param
                clients.add(ch);
                new Thread(ch).start();
            }
        }
    }
}
