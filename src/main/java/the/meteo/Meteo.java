package the.meteo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Meteo extends JFrame {
    private JTextArea outputTextArea;
    private JComboBox<String> hourComboBox;
    private String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m";

    public Meteo() {
        setTitle("Meteo Information");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel inputPanel = new JPanel();
        JLabel hourLabel = new JLabel("Hour:");

        // Create dropdown options
        String[] hourOptions = createHourOptions();
        hourComboBox = new JComboBox<>(hourOptions);

        JButton showTemperatureButton = new JButton("Hőmérséklet lekérése");
        showTemperatureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedHour = (String) hourComboBox.getSelectedItem();
                String selectedDate = extractDate(selectedHour);
                int hour = extractHour(selectedHour);
                updateOutput(hour, selectedDate);
            }
        });
        inputPanel.add(hourLabel);
        inputPanel.add(hourComboBox);
        inputPanel.add(showTemperatureButton);
        add(inputPanel, BorderLayout.NORTH);

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    // Create dropdown options
    private String[] createHourOptions() {
        String[] hourOptions = new String[48];
        for (int i = 0; i < 48; i++) {
            int currentHour = i % 24;
            String day = (i < 24) ? "Today" : "Tomorrow";
            int hour = (i < 24) ? i : i - 24;
            hourOptions[i] = day + " " + String.format("%02d", hour) + ":00";
        }
        return hourOptions;
    }

    // Extract hour from the selected option
    private int extractHour(String selectedOption) {
        String[] parts = selectedOption.split(" ");
        String timePart = parts[1];
        String[] timeParts = timePart.split(":");
        return Integer.parseInt(timeParts[0]);
    }

    private String extractDate(String selectedOption) {
        String[] parts = selectedOption.split(" ");
        return parts[0].equals("Today") ? LocalDateTime.now().toLocalDate().toString() : LocalDateTime.now().plusDays(1).toLocalDate().toString();
    }

    private void updateOutput(int hour, String selectedDate) {
        try {
            JSONObject jsonData = new JSONObject(getJsonData(apiUrl));
            double temperature = getTemperatureForHourAndDay(jsonData, hour, selectedDate);
            String locationName = getLocationName(jsonData);

            // Format date and time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String currentDateStr = selectedDate + " " + String.format("%02d", hour) + ":00";

            // Display the location name, date and time, and temperature
            outputTextArea.setText("Location: " + locationName + "\n" +
                    "Date and Time: " + currentDateStr + "\n" +
                    "Temperature (" + hour + ". hour): " + temperature + " °C");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            outputTextArea.setText("Error occurred: " + e.getMessage());
        }
    }

    public double getTemperatureForHourAndDay(JSONObject jsonData, int hour, String selectedDate) throws JSONException {
        try {
            JSONObject hourly = jsonData.getJSONObject("hourly");
            JSONArray timeArray = hourly.getJSONArray("time");
            JSONArray temperatureArray = hourly.getJSONArray("temperature_2m");

            // Find the index corresponding to the selected date and hour
            int index = -1;
            for (int i = 0; i < timeArray.length(); i++) {
                String time = timeArray.getString(i);
                LocalDateTime dateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
                int currentHour = dateTime.getHour();
                String currentDate = dateTime.toLocalDate().toString();

                if (currentHour == hour && currentDate.equals(selectedDate)) {
                    index = i;
                    break;
                }
            }

            if (index != -1 && index < temperatureArray.length()) {
                return temperatureArray.getDouble(index);
            } else {
                throw new IllegalArgumentException("Temperature data not found for the specified hour and date.");
            }
        } catch (JSONException e) {
            throw new JSONException("Error occurred while parsing JSON data: " + e.getMessage());
        }
    }

    public String getJsonData(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }
            return response.toString();
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }

    public String getLocationName(JSONObject jsonData) throws JSONException {
        return "Berlin"; // Modify this to extract the location name from the JSON data
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Meteo frame = new Meteo();
            frame.setVisible(true);
        });
    }
}
