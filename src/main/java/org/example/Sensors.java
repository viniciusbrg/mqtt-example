package org.example;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Random;

public class Sensors {
    public static void main(String[] args) throws MqttException {
        int sensors = 5;
        int temperatureLowerRange = 180;
        int temperatureUpperRange = 250;
        int temperatureVariation = 15;
        var random = new Random();

        for (int i = 0; i < sensors; i++) {
            var sensor = new Sensor(random.nextInt(temperatureLowerRange, temperatureUpperRange + 1), temperatureVariation);
            sensor.start();
        }
    }
}
