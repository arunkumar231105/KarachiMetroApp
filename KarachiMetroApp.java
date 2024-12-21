import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class KarachiMetroApp {


    static class Station {
        String name;
        int distanceFromPrevious;
        int timeFromPrevious;

        Station(String name, int distance, int time) {
            this.name = name;
            this.distanceFromPrevious = distance;
            this.timeFromPrevious = time;
        }

        @Override
        public String toString() {
            return name + " | Distance: " + distanceFromPrevious + " km | Time: " + timeFromPrevious + " mins";
        }
    }

    // MetroMap class to manage stations and their routes
    static class MetroMap {
        List <Station> stations;
        Map<String, Integer> stationIndexMap;
        Map<Integer, Map<Integer, Integer>> distanceAdjacencyMatrix; // For Dijkstra's algorithm by distance
        Map<Integer, Map<Integer, Integer>> timeAdjacencyMatrix; // For Dijkstra's algorithm by time

        MetroMap() {
            stations = new ArrayList<>();
            stationIndexMap = new HashMap<>();
            distanceAdjacencyMatrix = new HashMap<>();
            timeAdjacencyMatrix = new HashMap<>();
        }

        // Add a new station to the metro map
        void addStation(String name, int distance, int time) {
            stations.add(new Station(name, distance, time));
            stationIndexMap.put(name.toLowerCase(), stations.size() - 1);
            distanceAdjacencyMatrix.put(stations.size() - 1, new HashMap<>());
            timeAdjacencyMatrix.put(stations.size() - 1, new HashMap<>());
        }

        // Remove a station from the metro map
        void removeStation(String name) {
            int index = stationIndexMap.get(name.toLowerCase());
            stations.remove(index);
            stationIndexMap.remove(name.toLowerCase());
            distanceAdjacencyMatrix.remove(index);
            timeAdjacencyMatrix.remove(index);
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : distanceAdjacencyMatrix.entrySet()) {
                entry.getValue().remove(index);
            }
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : timeAdjacencyMatrix.entrySet()) {
                entry.getValue().remove(index);
            }
            // Also update indices in the adjacency matrices for other stations
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : distanceAdjacencyMatrix.entrySet()) {
                if (entry.getKey() > index) {
                    distanceAdjacencyMatrix.put(entry.getKey() - 1, entry.getValue());
                    distanceAdjacencyMatrix.remove(entry.getKey());
                }
            }
            for (Map.Entry<Integer, Map<Integer, Integer>> entry : timeAdjacencyMatrix.entrySet()) {
                if (entry.getKey() > index) {
                    timeAdjacencyMatrix.put(entry.getKey() - 1, entry.getValue());
                    timeAdjacencyMatrix.remove(entry.getKey());
                }
            }
        }

        // Add a route between two stations
        void addRoute(int startStationIndex, int endStationIndex, int distance, int time) {
            distanceAdjacencyMatrix.get(startStationIndex).put(endStationIndex, distance);
            timeAdjacencyMatrix.get(startStationIndex).put(endStationIndex, time);
        }

        // Dijkstra's algorithm to find the shortest path between two stations
        public List<Station> dijkstra(int startStationIndex, int endStationIndex, boolean byTime) {
            int n = stations.size();
            int[] dist = new int[n];
            int[] prev = new int[n];
            Arrays.fill(dist, Integer.MAX_VALUE);
            Arrays.fill(prev, -1);
            dist[startStationIndex] = 0;

            boolean[] visited = new boolean[n];
            PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.comparingInt(i -> dist[i]));
            pq.add(startStationIndex);

            Map<Integer, Map<Integer, Integer>> adjMatrix = byTime ? timeAdjacencyMatrix : distanceAdjacencyMatrix;

            while (!pq.isEmpty()) {
                int u = pq.poll();
                if (visited[u]) continue;
                visited[u] = true;

                for (Map.Entry<Integer, Integer> neighbor : adjMatrix.get(u).entrySet()) {
                    int v = neighbor.getKey();
                    int weight = neighbor.getValue();

                    if (!visited[v] && dist[u] + weight < dist[v]) {
                        dist[v] = dist[u] + weight;
                        prev[v] = u;
                        pq.add(v);
                    }
                }
            }

            // Reconstruct the shortest path
            List<Station> path = new ArrayList<>();
            for (int i = endStationIndex; i != startStationIndex; i = prev[i]) {
                path.add(stations.get(i));
            }
            path.add(stations.get(startStationIndex));
            Collections.reverse(path);
            return path;
        }

        // Shortest path between two stations by distance or time
        String shortestDistanceBetweenStations(String startStation, String endStation, boolean byTime) {
            if (!stationIndexMap.containsKey(startStation.toLowerCase()) ||
                    !stationIndexMap.containsKey(endStation.toLowerCase())) {
                return "One or both stations not found!";
            }
            int startIndex = stationIndexMap.get(startStation.toLowerCase());
            int endIndex = stationIndexMap.get(endStation.toLowerCase());
            List<Station> path = dijkstra(startIndex, endIndex, byTime);

            int total = 0;
            for (int i = 1; i < path.size(); i++) {
                total += byTime ? path.get(i).timeFromPrevious : path.get(i).distanceFromPrevious;
            }
            return "Shortest " + (byTime ? "time" : "distance") + " from " + startStation + " to " + endStation + ": " + total + (byTime ? " minutes" : " km");
        }

        // Get route information between two stations
        String getRouteInfo(String startStation, String endStation) {
            if (!stationIndexMap.containsKey(startStation.toLowerCase()) ||
                    !stationIndexMap.containsKey(endStation.toLowerCase())) {
                return "One or both stations not found!";
            }
            int startIndex = stationIndexMap.get(startStation.toLowerCase());
            int endIndex = stationIndexMap.get(endStation.toLowerCase());
            if (startIndex >= endIndex) {
                return "Invalid order of stations!";
            }

            StringBuilder route = new StringBuilder();
            route.append("Route Information:\n");
            int totalDistance = 0;
            int totalTime = 0;
            for (int i = startIndex + 1; i <= endIndex; i++) {
                Station current = stations.get(i);
                totalDistance += current.distanceFromPrevious;
                totalTime += current.timeFromPrevious;
                route.append(" - " + current.name + "\n");
            }
            route.append("\nTotal Distance: ").append(totalDistance).append(" km");
            route.append("\nTotal Time: ").append(totalTime).append(" minutes");
            return route.toString();
        }

        // Search for a station by name
        String searchStationByName(String search) {
            for (Station station : stations) {
                if (station.name.toLowerCase().contains(search.toLowerCase())) {
                    return station.toString();
                }
            }
            return "Station not found!";
        }

        // List all stations
        String listAllStations() {
            StringBuilder stationList = new StringBuilder();
            for (int i = 0; i < stations.size(); i++) {
                stationList.append((i + 1) + ". " + stations.get(i).name + "\n");
            }
            return stationList.toString();
        }
    }

    public static void main(String[] args) {
        MetroMap metro = new MetroMap();

        // Add some stations for demonstration
        metro.addStation("Model Colony", 0, 0);
        metro.addStation("Malir Halt", 2, 5);
        metro.addStation("Malir City", 3, 7);
        metro.addStation("Jinnah Square", 4, 8);
        metro.addStation("Kala Board", 2, 4);
        metro.addStation("Star Gate", 3, 6);
        metro.addStation("Drigh Road", 5, 10);
        metro.addStation("Shah Faisal Colony", 4, 8);
        metro.addStation("Gulistan-e-Johar", 6, 12);
        metro.addStation("Gulshan-e-Iqbal", 4, 8);
        metro.addStation("Nipa", 3, 7);
        metro.addStation("Gulshan Chowrangi", 2, 4);
        metro.addStation("Askari Park", 3, 5);
        metro.addStation("Peoples Secretariat", 5, 10);
        metro.addStation("Karsaz", 6, 12);
        metro.addStation("Bahadurabad", 4, 7);
        metro.addStation("Cantt Station", 3, 5);
        metro.addStation("Tower", 5, 12);

        // Add routes (with distance and time)
        metro.addRoute(0, 1, 2, 5);
        metro.addRoute(1, 2, 3, 7);
        metro.addRoute(2, 3, 4, 8);
        metro.addRoute(3, 4, 2, 4);
        metro.addRoute(4, 5, 3, 6);
        metro.addRoute(5, 6, 5, 10);

        // Setup GUI for user interactions
        JFrame frame = new JFrame("Karachi Metro App");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create the UI elements
        JPanel menuPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        JPanel displayPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Karachi Metro App", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        // Buttons for different options
        JButton listStationsButton = new JButton("1. List All Stations");
        JButton shortestDistanceButton = new JButton("2. Shortest Distance");
        JButton shortestTimeButton = new JButton("3. Shortest Time");
        JButton routeInfoButton = new JButton("4. Route Info");
        JButton searchStationButton = new JButton("5. Search Station");
        JButton editStationButton = new JButton("6. Edit Station");
        JButton removeStationButton = new JButton("7. Remove Station");
        JButton exitButton = new JButton("8. Exit");

        // Add buttons to the menu
        menuPanel.add(listStationsButton);
        menuPanel.add(shortestDistanceButton);
        menuPanel.add(shortestTimeButton);
        menuPanel.add(routeInfoButton);
        menuPanel.add(searchStationButton);
        menuPanel.add(editStationButton);
        menuPanel.add(removeStationButton);
        menuPanel.add(exitButton);

        // Add title and display area
        displayPanel.add(titleLabel, BorderLayout.NORTH);
        displayPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(menuPanel, BorderLayout.WEST);
        frame.add(displayPanel, BorderLayout.CENTER);

        // Button functionalities
        listStationsButton.addActionListener(e -> {
            displayArea.setText(metro.listAllStations());
        });

        shortestDistanceButton.addActionListener(e -> {
            String start = JOptionPane.showInputDialog(frame, "Enter starting station:");
            String end = JOptionPane.showInputDialog(frame, "Enter destination station:");
            displayArea.setText(metro.shortestDistanceBetweenStations(start, end, false));
        });

        shortestTimeButton.addActionListener(e -> {
            String start = JOptionPane.showInputDialog(frame, "Enter starting station:");
            String end = JOptionPane.showInputDialog(frame, "Enter destination station:");
            displayArea.setText(metro.shortestDistanceBetweenStations(start, end, true));
        });

        routeInfoButton.addActionListener(e -> {
            String start = JOptionPane.showInputDialog(frame, "Enter starting station:");
            String end = JOptionPane.showInputDialog(frame, "Enter destination station:");
            displayArea.setText(metro.getRouteInfo(start, end));
        });

        searchStationButton.addActionListener(e -> {
            String search = JOptionPane.showInputDialog(frame, "Enter station name to search:");
            displayArea.setText(metro.searchStationByName(search));
        });

        editStationButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Enter the name of the station to edit:");
            // Add more options to edit station, e.g., change distance/time
        });

        removeStationButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Enter station name to remove:");
            metro.removeStation(name);
            displayArea.setText("Station removed successfully.");
        });

        exitButton.addActionListener(e -> System.exit(0));


        frame.setVisible(true);
    }
}
