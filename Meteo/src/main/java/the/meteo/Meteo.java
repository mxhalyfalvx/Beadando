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
    private JTextField hourTextField;

    public Meteo() {
        setTitle("Meteo Information");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel inputPanel = new JPanel();
        JLabel hourLabel = new JLabel("Hour:");
        hourTextField = new JTextField(5);
        JButton showTemperatureButton = new JButton("Mutass hőmérsékletet");
        showTemperatureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int hour = Integer.parseInt(hourTextField.getText());
                updateOutput(hour);
            }
        });
        inputPanel.add(hourLabel);
        inputPanel.add(hourTextField);
        inputPanel.add(showTemperatureButton);
        add(inputPanel, BorderLayout.NORTH);

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void updateOutput(int hour) {
        try {
            String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m";
            double temperature = getTemperatureForHour(apiUrl, hour);
            outputTextArea.setText("Hőmérséklet (" + hour + ". óra): " + temperature + " °C");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            outputTextArea.setText("Error occurred: " + e.getMessage());
        }
    }

    public static double getTemperatureForHour(String apiUrl, int hour) throws IOException, JSONException {
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

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray hourlyData = jsonResponse.getJSONObject("hourly").getJSONArray("temperature_2m");

            if (hour >= 0 && hour < hourlyData.length()) {
                double temperature = hourlyData.getDouble(hour);
                return temperature;
            } else {
                throw new IllegalArgumentException("Invalid hour: " + hour);
            }
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