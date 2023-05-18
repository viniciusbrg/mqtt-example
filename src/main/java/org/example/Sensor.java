package org.example;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Sensor {

    private final String sensorId;
    private final MqttClient mqttClient;
    private final Timer timer;
    private int currentTemperature = 0;

    private int temperatureVariation = 0;

    public static final int NOTIFICATION_INTERVAL_SECONDS = 5;
    private final Random random = new Random();

    public Sensor(int initialTemperature, int variation) throws MqttException {
        this.sensorId = UUID.randomUUID().toString();
        this.mqttClient = MqttConnectionFactory.getConnectedClient(sensorId);
        this.currentTemperature = initialTemperature;
        this.temperatureVariation = variation;
        this.timer = new Timer();
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                var message = new MqttMessage(String.valueOf(currentTemperature).getBytes());
                message.setQos(0);
                try {
                    mqttClient.publish(Topics.TEMPERATURES.toString(), message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                var upperTemperatureBound = currentTemperature + temperatureVariation;
                var lowerTemperatureBound = currentTemperature - temperatureVariation;

                currentTemperature = random.nextInt(lowerTemperatureBound, upperTemperatureBound + 1);
                System.out.println("New temperature: " + currentTemperature);
            }
        }, 1000, NOTIFICATION_INTERVAL_SECONDS * 1000);


    }
}
