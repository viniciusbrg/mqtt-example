package org.example;

import org.eclipse.paho.client.mqttv3.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ComputeAverageTemperatureService {
    private final MqttClient mqttClient;
    private float lastTemperature = 0;
    private float currentTemperature = 0;
    private final ConcurrentLinkedQueue<Integer> temperatureSamples;
    private final Timer timer;

    private final static int CHECK_INTERVAL_SECONDS = Sensor.NOTIFICATION_INTERVAL_SECONDS * 2;
    private final static int HIGH_TEMPERATURE_ALERT_THRESHOLD = 200;
    private final static int TEMPERATURE_DIFFERENCE_ALERT_THRESHOLD = 5;

    public ComputeAverageTemperatureService() throws MqttException {
        this.temperatureSamples = new ConcurrentLinkedQueue<>();
        this.mqttClient = MqttConnectionFactory.getConnectedClient("cat-service");
        this.mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Lost connection");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                temperatureSamples.add(Integer.parseInt(mqttMessage.toString()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
        });
        this.mqttClient.subscribe(Topics.TEMPERATURES.toString());
        this.timer = new Timer();
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                float sum = 0;
                int elements = 0;

                for (Integer temperatureSample : temperatureSamples) {
                    sum += temperatureSample;
                    elements += 1;
                }

                temperatureSamples.clear();

                try {
                    if (elements > 0) {
                        storeNewAverageTemperature(sum / elements);
                    } else {
                        storeNewAverageTemperature(currentTemperature);
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }
        }, 1000L, CHECK_INTERVAL_SECONDS * 1000);
    }

    private void storeNewAverageTemperature(float average) throws MqttException {
        this.lastTemperature = currentTemperature;
        this.currentTemperature = average;

        System.out.println("New average temperature: " + currentTemperature);

        if (Math.abs(currentTemperature - lastTemperature) >= TEMPERATURE_DIFFERENCE_ALERT_THRESHOLD) {
            this.sendAlert(Topics.TEMPERATURE_DIFFERENCE_ALARM, this.lastTemperature + "," + this.currentTemperature);
        }

        if (currentTemperature >= HIGH_TEMPERATURE_ALERT_THRESHOLD) {
            this.sendAlert(Topics.HIGH_TEMPERATURE_ALARM, String.valueOf(this.currentTemperature));
        }
    }

    private void sendAlert(Topics topic, String message) throws MqttException {
        var messageWithTime = System.currentTimeMillis() + "," + message;
        var mqttMessage = new MqttMessage(messageWithTime.getBytes());
        this.mqttClient.publish(topic.toString(), mqttMessage);
        System.out.println("Alert " + topic + " sent - Message: " + messageWithTime);
    }

    public static void main(String[] args) throws MqttException {
        new ComputeAverageTemperatureService().start();
    }
}
