package the.meteo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileWriter;
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
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:00:00");

    public Meteo() {
        setTitle("Meteo Information");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame on the screen

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        locationPanel.setBackground(new Color(235, 235, 235));

        JLabel hourLabel = new JLabel("Hour:");
        hourComboBox = new JComboBox<>(createHourOptions());
        JButton showTemperatureButton = new JButton("Get Temperature");
        JButton exitButton = new JButton("Exit");

        showTemperatureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedHour = (String) hourComboBox.getSelectedItem();
                updateOutput(selectedHour);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Set 'Get Temperature' button as default button
        getRootPane().setDefaultButton(showTemperatureButton);

        inputPanel.add(hourLabel);
        inputPanel.add(hourComboBox);
        inputPanel.add(showTemperatureButton);
        inputPanel.add(exitButton);

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(locationPanel, BorderLayout.SOUTH);
        add(mainPanel);

        JLabel locationLabel = new JLabel("Berlin | Latitude: 52.52, Longitude: 13.41");
        locationPanel.add(locationLabel);

        setVisible(true);
    }

    private String[] createHourOptions() {
        String[] options = new String[72];
        Date now = new Date();
        for (int i = 0; i < 72; i++) {
            options[i] = formatter.format(now);
            now = new Date(now.getTime() + 3600000);
        }
        return options;
    }

    private void updateOutput(String selectedHour) {
        try {
            JSONObject jsonData = new JSONObject(getJsonData(apiUrl));
            int hour = extractHour(selectedHour);
            double temperature = getTemperatureForHour(jsonData, hour);
            Date selectedDate = formatter.parse(selectedHour);
            outputTextArea.setText("Date and Time: " + selectedHour + "\n" +
                    "Temperature : " + temperature + " °C");

            // Write to CSV
            writeDataToCSV(selectedHour, temperature);
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
            outputTextArea.setText("Error occurred: " + e.getMessage());
        }
    }

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

    private void writeDataToCSV(String selectedHour, double temperature) {
        try (FileWriter writer = new FileWriter("C:\\Users\\szupe\\OneDrive\\Asztali gép\\Meteo.txt", true)) {
            writer.append(selectedHour).append(",");
            writer.append(String.valueOf(temperature)).append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            outputTextArea.setText("Error occurred while writing to CSV: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Meteo frame = new Meteo();
            frame.setVisible(true);
        });
    }
}
