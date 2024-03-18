package the.meteo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;

public class Meteo extends JFrame {
    private JTextArea outputTextArea;

    public Meteo() {
        setTitle("Meteo Information");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateOutput();
            }
        }, 0, 10 * 60 * 1000); // Refresh every 10 minutes
    }

    private void updateOutput() {
        try {
            String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m";
            double latestTemperature = getLatestTemperature(apiUrl);
            outputTextArea.setText("Latest temperature: " + latestTemperature + " °C");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            outputTextArea.setText("Error occurred: " + e.getMessage());
        }
    }

    public static double getLatestTemperature(String apiUrl) throws IOException, JSONException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the response into a string
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray hourlyData = jsonResponse.getJSONObject("hourly").getJSONArray("temperature_2m");

            // Get the latest temperature data from the array
            double latestTemperature = hourlyData.getDouble(hourlyData.length() - 1); // Assuming the last entry is the latest temperature
            return latestTemperature;
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
