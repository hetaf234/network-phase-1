/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Network_Project;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClientHandler (student-simple)
 * - No Server reference
 * - Reads commands and operates on Server.cars / Server.occupancyByDate
 */
public class ClientHandler implements Runnable {

    private final Socket client;
    private final BufferedReader in;
    private final PrintWriter out;
    private final ArrayList<ClientHandler> clients;
    private String username = null; // harmless, kept for compatibility with GUI flow

    public ClientHandler(Socket c, ArrayList<ClientHandler> clients) throws IOException {
        this.client = c;
        this.clients = clients;
        this.in  = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            out.println("Connected successfully to the Car Rental Server!");
            String line;
            while ((line = in.readLine()) != null) {
                String response = handle(line.trim());
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("[Server] Client disconnected.");
        } finally {
            try { in.close(); } catch (IOException ignore) {}
            out.close();
        }
    }

    private String handle(String cmd) {
        try {
            // REGISTER (always OK except empty)
            if (cmd.startsWith("REGISTER ")) {
                String[] p = cmd.substring(9).split(" ");
                if (p.length != 2) return "Please enter both email and password.";
                if (p[0].isBlank() || p[1].isBlank()) return "Please enter both email and password.";
                username = p[0];
                System.out.println("[Server] Registered: " + username);
                return "Registration successful! You can now book cars.";
            }

            // SEARCH type=AUTO/MANUAL seats=2/5/8 start=yyyy-MM-dd days=1..7
            if (cmd.startsWith("SEARCH ")) {
                Map<String,String> kv = parseKV(cmd.substring(7));
                String type  = u(kv.get("type"));
                int seats    = toInt(kv.get("seats"), -1);
                LocalDate st = toDate(kv.get("start"));
                int days     = toInt(kv.get("days"), 0);

                if (!"AUTO".equals(type) && !"MANUAL".equals(type)) return "Please select a valid car type.";
                if (!(seats == 2 || seats == 5 || seats == 8))     return "Invalid seat number.";
                if (days < 1 || days > 7)                          return "Number of days must be between 1 and 7.";
                if (st == null)                                    return "Invalid start date.";

                List<String> available = new ArrayList<>();
                for (Car c : Server.cars.values()) {
                    if (!c.getType().equals(type) || c.getNumOfSeates() != seats) continue;
                    if (isRangeAvailable(c.getCarId(), st, days)) available.add(c.getCarId());
                }
                if (available.isEmpty()) return "No cars available for your selection.";
                Collections.sort(available);
                return "Available cars: " + String.join(", ", available);
            }

            // RESERVE car=ID start=yyyy-MM-dd days=1..7
            if (cmd.startsWith("RESERVE ")) {
                Map<String,String> kv = parseKV(cmd.substring(8));
                String carId  = val(kv.get("car"));
                LocalDate st  = toDate(kv.get("start"));
                int days      = toInt(kv.get("days"), 0);

                if (!Server.cars.containsKey(carId))              return "Car not found.";
                if (days < 1 || days > 7)                         return "Days must be between 1 and 7.";
                if (st == null)                                    return "Invalid start date.";
                if (!isRangeAvailable(carId, st, days))           return "Sorry, this car is not available for these dates.";

                // mark [st, st+days)
                for (int i = 0; i < days; i++) {
                    LocalDate d = st.plusDays(i);
                    Server.occupancyByDate
                          .computeIfAbsent(d, k -> ConcurrentHashMap.newKeySet())
                          .add(carId);
                }
                System.out.println("[Server] Reserved " + carId + " from " + st + " for " + days + " day(s).");
                return "Booking confirmed.";
            }

            return "Unknown command. Please try again.";

        } catch (Exception e) {
            return "Server error: " + e.getClass().getSimpleName();
        }
    }

    // ----- helpers (local to handler) -----
    private Map<String,String> parseKV(String s) {
        Map<String,String> m = new HashMap<>();
        for (String part : s.split(" ")) {
            if (part.isBlank()) continue;
            String[] kv = part.split("=", 2);
            if (kv.length == 2) m.put(kv[0], kv[1]);
        }
        return m;
    }

    private boolean isRangeAvailable(String carId, LocalDate start, int days) {
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            Set<String> set = Server.occupancyByDate.get(d);
            if (set != null && set.contains(carId)) return false;
        }
        return true;
    }

    private String u(String v)   { return v == null ? "" : v.toUpperCase(); }
    private String val(String v) { return v == null ? "" : v; }
    private int toInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private LocalDate toDate(String s)   { try { return LocalDate.parse(s); } catch (Exception e) { return null; } }
}
