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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
                int hour = extractHour(selectedHour);
                updateOutput(hour);
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
        String[] options = new String[72]; // 24 hours for today, tomorrow, and the day after
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDay = now.plusDays(1);
        LocalDateTime nextTwoDays = now.plusDays(2);
        
        for (int i = 0; i < 24; i++) {
            options[i] = "Today " + formatter.format(now.plusHours(i));
            options[i + 24] = "Tomorrow " + formatter.format(nextDay.plusHours(i));
            options[i + 48] = "Tomorrow+1 " + formatter.format(nextTwoDays.plusHours(i));
        }
        
        return options;
    }
    
    // Extract hour from the selected option
    private int extractHour(String selectedOption) {
        String[] parts = selectedOption.split(" ");
        String timePart = parts[1];
        String[] timeParts = timePart.split(":");
        return Integer.parseInt(timeParts[0]);
    }

    private void updateOutput(int hour) {
        try {
            JSONObject jsonData = new JSONObject(getJsonData(apiUrl));
            double temperature = getTemperatureForHour(jsonData, hour);
            // Get current date and time
            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(currentDate);
            // Display the current date and time along with the hour and temperature
            outputTextArea.setText("Date and Time: " + currentDateTime + "\n" +
                                   "Temperature (" + hour + ". hour): " + temperature + " °C");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            outputTextArea.setText("Error occurred: " + e.getMessage());
        }
    }

    public double getTemperatureForHour(JSONObject jsonData, int hour) throws JSONException {
        try {
            JSONObject hourly = jsonData.getJSONObject("hourly");
            JSONArray temperatureArray = hourly.getJSONArray("temperature_2m");

            if (hour >= 0 && hour < temperatureArray.length()) {
                return temperatureArray.getDouble(hour);
            } else {
                throw new IllegalArgumentException("Invalid hour: " + hour);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Meteo frame = new Meteo();
            frame.setVisible(true);
        });
    }
}
