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
import java.text.ParseException;
import java.util.Date;

public class Meteo extends JFrame {
    private JTextArea outputTextArea;
    private JComboBox<String> hourComboBox;
    private JLabel locationLabel;
    private String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m";

    public Meteo() {
        setTitle("Meteo Information");
        setSize(400, 350); // Increased height to accommodate the location label
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
                updateOutput(selectedHour);
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

        // Add location label
        locationLabel = new JLabel("Location: Latitude 52.52, Longitude 13.41");
        add(locationLabel, BorderLayout.SOUTH);
        locationLabel = new JLabel("Location: Berlin");
        add(locationLabel, BorderLayout.EAST);
    }

    // Create dropdown options with all hours
    private String[] createHourOptions() {
        String[] options = new String[72]; // 24 hours for today, tomorrow, and the day after
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
        Date now = new Date();
        for (int i = 0; i < 24; i++) {
            options[i] = formatter.format(now);
            now = new Date(now.getTime() + 3600000); // Increment hour by one
        }
        now = new Date(now.getTime() + 86400000); // Move to tomorrow
        for (int i = 0; i < 24; i++) {
            options[i + 24] = formatter.format(now);
            now = new Date(now.getTime() + 3600000); // Increment hour by one
        }
        now = new Date(now.getTime() + 86400000); // Move to the day after tomorrow
        for (int i = 0; i < 24; i++) {
            options[i + 48] = formatter.format(now);
            now = new Date(now.getTime() + 3600000); // Increment hour by one
        }
        return options;
    }

    private void updateOutput(String selectedHour) {
        try {
            JSONObject jsonData = new JSONObject(getJsonData(apiUrl));
            int hour = extractHour(selectedHour);
            double temperature = getTemperatureForHour(jsonData, hour);
            // Get date and time corresponding to the selected hour
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
            Date selectedDate = formatter.parse(selectedHour);
            // Display the selected date and time along with the hour and temperature
            outputTextArea.setText("Date and Time: " + selectedHour + "\n" +
                    "Temperature (" + selectedHour + "): " + temperature + " °C");
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
            outputTextArea.setText("Error occurred: " + e.getMessage());
        }
    }

    // Extract hour from the selected option
    private int extractHour(String selectedOption) {
        String[] parts = selectedOption.split(" ");
        String timePart = parts[1];
        return Integer.parseInt(timePart.split(":")[0]);
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
