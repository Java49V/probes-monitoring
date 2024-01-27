package telran.probes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import telran.probes.dto.SensorRange;
import telran.probes.service.SensorRangeProviderService;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class AnalyzerServiceTest {
	@Autowired
	InputDestination producer;
	@MockBean
	RestTemplate restTemplate;
	@Autowired
	SensorRangeProviderService providerService;
	static final long SENSOR_ID = 123l;
	private static final float MIN_VALUE = 10;
	private static final float MAX_VALUE = 20;
	static final SensorRange SENSOR_RANGE = new SensorRange(MIN_VALUE, MAX_VALUE);
	private static final long NON_EXISTING_SENSOR_ID = 0;
	private static final long EXISTING_SENSOR_ID = 123L; // Здесь можно использовать существующий ID датчика

	@SuppressWarnings("unchecked")
	@Test
	@Order(1)
	void normalFlowWithNoMapData() {
		ResponseEntity<SensorRange> responseEntity = new ResponseEntity<SensorRange>(SENSOR_RANGE, HttpStatus.OK);
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
				.thenReturn(responseEntity);
		SensorRange actual = providerService.getSensorRange(SENSOR_ID);
		assertEquals(SENSOR_RANGE, actual);

	}

	@Test
	@Order(2)
	void normalFlowWithMapData() {

		SensorRange actual = providerService.getSensorRange(SENSOR_ID);
		assertEquals(SENSOR_RANGE, actual);

	}


	@SuppressWarnings("unchecked")
	@Test
	void testNotFoundResponseFromRemoteService() {
	    // Устанавливаем поведение мока для возврата ResponseEntity с кодом статуса 404
	    when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(),
	            any(Class.class))).thenReturn(new ResponseEntity<>("Sensor not found", HttpStatus.NOT_FOUND));
	    
	    // Вызываем метод, который использует удаленный сервис
	    SensorRange range = providerService.getSensorRange(NON_EXISTING_SENSOR_ID);
	    
	    // Проверяем, что диапазон равен null, так как датчик не был найден
	    assertNull(range);
	}

	
	@SuppressWarnings("unchecked")
	@Test
	void testRemoteServiceUnavailable() {
	    // Устанавливаем поведение мока для выброса RestClientException при вызове restTemplate.exchange()
	    when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(),
	            any(Class.class))).thenThrow(new RestClientException("Service unavailable"));
	    
	    // Вызываем метод, который использует удаленный сервис
	    SensorRange range = providerService.getSensorRange(EXISTING_SENSOR_ID);
	    
	    // Проверяем, что диапазон равен null, так как сервис недоступен
	    assertNull(range);
	}
	
	@Test
	void testUpdateExistingSensorRangeInMap() {
	    // Устанавливаем начальное значение диапазона для существующего датчика
	    float initialMinValue = 0;
	    float initialMaxValue = 100;
	    SensorRange initialRange = new SensorRange(initialMinValue, initialMaxValue);
	    mapRanges.put(EXISTING_SENSOR_ID, initialRange);
	    
	    // Моделируем обновление диапазона для существующего датчика
	    float newMinValue = 10;
	    float newMaxValue = 200;
	    String updateMessage = "range-update#" + EXISTING_SENSOR_ID + "#" + newMinValue + "#" + newMaxValue;
	    configChangeConsumer.accept(updateMessage);
	    
	    // Получаем обновленный диапазон из карты
	    SensorRange updatedRange = mapRanges.get(EXISTING_SENSOR_ID);
	    
	    // Проверяем, что диапазон успешно обновлен
	    assertNotNull(updatedRange);
	    assertEquals(newMinValue, updatedRange.minValue());
	    assertEquals(newMaxValue, updatedRange.maxValue());
	}
	
	@Test
	void testUpdateNonExistingSensorRangeInMap() {
	    // Убеждаемся, что датчик отсутствует в карте перед обновлением
	    assertFalse(mapRanges.containsKey(NON_EXISTING_SENSOR_ID));
	    
	    // Моделируем обновление диапазона для несуществующего датчика
	    float newMinValue = 10;
	    float newMaxValue = 200;
	    String updateMessage = "range-update#" + NON_EXISTING_SENSOR_ID + "#" + newMinValue + "#" + newMaxValue;
	    configChangeConsumer.accept(updateMessage);
	    
	    // Получаем обновленный диапазон из карты
	    SensorRange updatedRange = mapRanges.get(NON_EXISTING_SENSOR_ID);
	    
	    // Проверяем, что диапазон успешно добавлен
	    assertNotNull(updatedRange);
	    assertEquals(newMinValue, updatedRange.minValue());
	    assertEquals(newMaxValue, updatedRange.maxValue());
	}

	
	@Test
	void testUpdateEmailWithoutChangingSensorRange() {
	    // Устанавливаем начальное значение диапазона для существующего датчика
	    float initialMinValue = 0;
	    float initialMaxValue = 100;
	    SensorRange initialRange = new SensorRange(initialMinValue, initialMaxValue);
	    mapRanges.put(EXISTING_SENSOR_ID, initialRange);
	    
	    // Моделируем обновление адреса электронной почты без изменения диапазона датчика
	    String updateMessage = "email-update#example@example.com";
	    configChangeConsumer.accept(updateMessage);
	    
	    // Получаем диапазон из карты
	    SensorRange updatedRange = mapRanges.get(EXISTING_SENSOR_ID);
	    
	    // Проверяем, что диапазон остался неизменным
	    assertNotNull(updatedRange);
	    assertEquals(initialMinValue, updatedRange.minValue());
	    assertEquals(initialMaxValue, updatedRange.maxValue());
	}

	
	
	
	

	








}
