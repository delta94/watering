package com.coderteam.watering.device.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class SoilMoistureTest {
    
    @Test
    void testSoilMoisture() {
        SoilMoistureSensor sensor = SoilMoistureSensor.builder()
                .id(10L)
                .deviceId("device_1")
                .build();

        SoilMoisture soilMoisture = SoilMoisture.builder()
                .value((short)1023)
                .sensor(sensor)
                .build();
        
        Assertions.assertEquals(soilMoisture.getValue(), (short)1023);
        Assertions.assertEquals(soilMoisture.getSensor(), sensor);
        Assertions.assertNotNull(soilMoisture.getPublishTime());
    }

}