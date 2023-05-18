package org.example;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttConnectionFactory {

    public static final String broker = "tcp://localhost:2883";

    public static MqttClient getConnectedClient(String clientId) throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        return getConnectedClient(clientId, connOpts);
    }

    public static MqttClient getConnectedClient(String clientId, MqttConnectOptions options) throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
        sampleClient.connect(options);

        return sampleClient;
    }
}
