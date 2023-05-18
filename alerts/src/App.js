import React, {useEffect, useState} from 'react';
import {Divider, List, Typography} from 'antd';
import mqtt from "precompiled-mqtt";

const URL = "ws://localhost:9001";
const client = mqtt.connect(URL);

const TOPICS = ["HIGH_TEMPERATURE_ALARM", "TEMPERATURE_DIFFERENCE_ALARM"]
client.on('connect', function () {
    client.subscribe(TOPICS, function (err) {
        if (err) console.error("Error subscribing to topics: " + err)
    })
})

function App() {
    const [data, setData] = useState([])

    function decodeHighTemperatureMessage(message) {
        const parts = message.split(',')
        const [time, temperature] = parts
        const timeInt = Number.parseInt(time)

        return `Temperatura: ${temperature} - Horário: ${new Date(timeInt).toLocaleString()}`
    }

    function decodeTemperatureChangeMessage(message) {
        const parts = message.split(',')
        const [time, lastTemperature, currentTemperature] = parts
        const timeInt = Number.parseInt(time)
        return `Temperatura antes: ${lastTemperature} - Temperatura atual: ${currentTemperature} - Horário: ${new Date(timeInt).toLocaleString()}`
    }

    useEffect(() => {
        client.on('message', function (topic, message) {
            const messageTransformFn = topic === 'HIGH_TEMPERATURE_ALARM' ?
                decodeHighTemperatureMessage : decodeTemperatureChangeMessage
            const transformedMessage = messageTransformFn(message.toString())
            setData( prev => [...prev, { topic, message: transformedMessage }])
        })
    }, [])

    return <>
        <Divider orientation="left">Alertas</Divider>
        <List
            bordered
            dataSource={data}
            renderItem={(item) => (
                <List.Item>
                    <Typography.Text mark>[{item.topic}]</Typography.Text> {item.message}
                </List.Item>
            )}
        />
    </>
}

export default App;