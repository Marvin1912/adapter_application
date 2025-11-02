package com.marvin.plants.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marvin.plants.dto.PlantDTO;
import com.marvin.plants.dto.PlantLocation;
import com.marvin.plants.entity.Plant;
import com.marvin.plants.mapper.PlantMapper;
import com.marvin.plants.repository.PlantRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class PlantServiceTest {

  private final String testMailUsername = "test@example.com";
  private final String testImageUuid = "test-uuid-123";
  @Mock
  private PlantRepository plantRepository;
  @Mock
  private PlantMapper plantMapper;
  @Mock
  private JavaMailSender mailSender;
  @InjectMocks
  private PlantService plantService;
  private Plant testPlant;
  private PlantDTO testPlantDTO;

  @BeforeEach
  void setUp() {
    testPlant = new Plant();
    testPlant.setId(1);
    testPlant.setName("Test Plant");
    testPlant.setSpecies("Test Species");
    testPlant.setDescription("Test Description");
    testPlant.setCareInstructions("Test Care Instructions");
    testPlant.setLocation(PlantLocation.LIVING_ROOM);
    testPlant.setWateringFrequency(7);
    testPlant.setLastWateredDate(LocalDate.now().minusDays(3));
    testPlant.setNextWateredDate(LocalDate.now().plusDays(4));
    testPlant.setImage("test-image.jpg");

    testPlantDTO = new PlantDTO(
        1L,
        "Test Plant",
        "Test Species",
        "Test Description",
        "Test Care Instructions",
        PlantLocation.LIVING_ROOM,
        7,
        LocalDate.now().minusDays(3),
        LocalDate.now().plusDays(4),
        "test-image.jpg"
    );
  }

  @Test
  void createPlant_ShouldReturnPlantId_WhenValidInput() {
    // Given
    final PlantDTO plantDto = new PlantDTO(
        0L,
        "New Plant",
        "New Species",
        "New Description",
        "New Care Instructions",
        PlantLocation.BEDROOM,
        5,
        null,
        null,
        null
    );

    final Plant newPlant = new Plant();
    newPlant.setId(2);
    newPlant.setName("New Plant");
    newPlant.setSpecies("New Species");
    newPlant.setDescription("New Description");
    newPlant.setCareInstructions("New Care Instructions");
    newPlant.setLocation(PlantLocation.BEDROOM);
    newPlant.setWateringFrequency(5);
    newPlant.setImage(testImageUuid);

    when(plantMapper.toPlant(plantDto, testImageUuid)).thenReturn(newPlant);
    when(plantRepository.save(newPlant)).thenReturn(newPlant);

    // When
    final long result = plantService.createPlant(plantDto, testImageUuid);

    // Then
    assertEquals(2L, result);
    verify(plantMapper).toPlant(plantDto, testImageUuid);
    verify(plantRepository).save(newPlant);
  }

  @Test
  void getPlant_ShouldReturnPlantDTO_WhenPlantExists() {
    // Given
    when(plantRepository.findById(1L)).thenReturn(Optional.of(testPlant));
    when(plantMapper.toPlantDTO(testPlant)).thenReturn(testPlantDTO);

    // When
    final PlantDTO result = plantService.getPlant(1L);

    // Then
    assertNotNull(result);
    assertEquals(testPlantDTO.id(), result.id());
    assertEquals(testPlantDTO.name(), result.name());
    assertEquals(testPlantDTO.species(), result.species());
    verify(plantRepository).findById(1L);
    verify(plantMapper).toPlantDTO(testPlant);
  }

  @Test
  void getPlant_ShouldReturnNull_WhenPlantNotExists() {
    // Given
    when(plantRepository.findById(999L)).thenReturn(Optional.empty());
    when(plantMapper.toPlantDTO(null)).thenReturn(null);

    // When
    final PlantDTO result = plantService.getPlant(999L);

    // Then
    assertNull(result);
    verify(plantRepository).findById(999L);
    verify(plantMapper).toPlantDTO(null);
  }

  @Test
  void getPlants_ShouldReturnAllPlantsAsDTOs() {
    // Given
    final Plant plant2 = new Plant();
    plant2.setId(2);
    plant2.setName("Plant 2");
    plant2.setSpecies("Species 2");
    plant2.setDescription("Description 2");
    plant2.setCareInstructions("Care 2");
    plant2.setLocation(PlantLocation.KITCHEN);
    plant2.setWateringFrequency(10);

    final PlantDTO plantDTO2 = new PlantDTO(
        2L,
        "Plant 2",
        "Species 2",
        "Description 2",
        "Care 2",
        PlantLocation.KITCHEN,
        10,
        null,
        null,
        null
    );

    final List<Plant> plants = List.of(testPlant, plant2);
    when(plantRepository.findAll()).thenReturn(plants);
    when(plantMapper.toPlantDTO(testPlant)).thenReturn(testPlantDTO);
    when(plantMapper.toPlantDTO(plant2)).thenReturn(plantDTO2);

    // When
    final var result = plantService.getPlants();

    // Then
    assertNotNull(result);
    final List<PlantDTO> resultList = result.collectList().block();
    assertEquals(2, resultList.size());
    assertEquals(testPlantDTO.name(), resultList.get(0).name());
    assertEquals(plantDTO2.name(), resultList.get(1).name());
    verify(plantRepository).findAll();
    verify(plantMapper, times(2)).toPlantDTO(any(Plant.class));
  }

  @Test
  void deletePlant_ShouldCallRepositoryDeleteById() {
    // When
    plantService.deletePlant(1L);

    // Then
    verify(plantRepository).deleteById(1L);
  }

  @Test
  void updatePlant_ShouldUpdateExistingPlant_WhenPlantExists() {
    // Given
    final LocalDate waterDate = LocalDate.now();
    final PlantDTO updateDTO = new PlantDTO(
        1L,
        "Updated Plant",
        "Updated Species",
        "Updated Description",
        "Updated Care Instructions",
        PlantLocation.KITCHEN,
        10,
        waterDate,
        waterDate.plusDays(10),
        "updated-image.jpg"
    );

    when(plantRepository.findById(1L)).thenReturn(Optional.of(testPlant));
    doNothing().when(plantMapper).toPlant(testPlant, updateDTO);

    // When
    plantService.updatePlant(updateDTO);

    // Then
    verify(plantRepository).findById(1L);
    verify(plantMapper).toPlant(testPlant, updateDTO);
    assertEquals(waterDate, testPlant.getLastWateredDate());
    // The next watering date should be calculated using the plant's existing watering frequency (7 days)
    assertEquals(waterDate.plusDays(7), testPlant.getNextWateredDate());
  }

  @Test
  void updatePlant_ShouldThrowException_WhenPlantNotExists() {
    // Given
    final PlantDTO updateDTO = new PlantDTO(
        999L,
        "Updated Plant",
        "Updated Species",
        "Updated Description",
        "Updated Care Instructions",
        PlantLocation.KITCHEN,
        10,
        LocalDate.now(),
        LocalDate.now().plusDays(10),
        "updated-image.jpg"
    );

    when(plantRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    final IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> plantService.updatePlant(updateDTO)
    );

    assertEquals("Plant with id 999 not found", exception.getMessage());
    verify(plantRepository).findById(999L);
    verify(plantMapper, never()).toPlant(any(Plant.class), any(PlantDTO.class));
  }

  @Test
  void waterPlant_ShouldUpdateWateringDatesAndReturnDTO_WhenPlantExists() {
    // Given
    final LocalDate waterDate = LocalDate.now();
    final LocalDate expectedNextWaterDate = waterDate.plusDays(testPlant.getWateringFrequency());

    when(plantRepository.findById(1L)).thenReturn(Optional.of(testPlant));
    when(plantMapper.toPlantDTO(testPlant)).thenReturn(testPlantDTO);

    // When
    final PlantDTO result = plantService.waterPlant(1L, waterDate);

    // Then
    assertNotNull(result);
    assertEquals(waterDate, testPlant.getLastWateredDate());
    assertEquals(expectedNextWaterDate, testPlant.getNextWateredDate());
    verify(plantRepository).findById(1L);
    verify(plantMapper).toPlantDTO(testPlant);
  }

  @Test
  void waterPlant_ShouldThrowException_WhenPlantNotExists() {
    // Given
    final LocalDate waterDate = LocalDate.now();
    when(plantRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(RuntimeException.class, () -> plantService.waterPlant(999L, waterDate));
    verify(plantRepository).findById(999L);
    verify(plantMapper, never()).toPlantDTO(any(Plant.class));
  }

  @Test
  void sendWateringNotification_ShouldNotSendEmail_WhenNoPlantsToWater() {
    // Given
    final LocalDate today = LocalDate.now();
    when(plantRepository.findByNextWateredDate(today)).thenReturn(List.of());

    // When
    plantService.sendWateringNotification();

    // Then
    verify(plantRepository).findByNextWateredDate(today);
    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendWateringNotification_ShouldSendEmail_WhenPlantsToWaterExist() {
    // Given
    final LocalDate today = LocalDate.now();

    final Plant plantToWater1 = new Plant();
    plantToWater1.setId(1);
    plantToWater1.setName("Rose");
    plantToWater1.setLocation(PlantLocation.LIVING_ROOM);

    final Plant plantToWater2 = new Plant();
    plantToWater2.setId(2);
    plantToWater2.setName("Tulip");
    plantToWater2.setLocation(PlantLocation.KITCHEN);

    final Collection<Plant> plantsToWater = List.of(plantToWater1, plantToWater2);
    when(plantRepository.findByNextWateredDate(today)).thenReturn(plantsToWater);

    // Use reflection to set the private mailUsername field
    try {
      final java.lang.reflect.Field field = PlantService.class.getDeclaredField("mailUsername");
      field.setAccessible(true);
      field.set(plantService, testMailUsername);
    } catch (Exception e) {
      fail("Failed to set mailUsername field: " + e.getMessage());
    }

    // When
    plantService.sendWateringNotification();

    // Then
    verify(plantRepository).findByNextWateredDate(today);
    verify(mailSender).send(any(SimpleMailMessage.class));

    final ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(
        SimpleMailMessage.class);
    verify(mailSender).send(messageCaptor.capture());

    final SimpleMailMessage sentMessage = messageCaptor.getValue();
    assertEquals(testMailUsername, sentMessage.getFrom());
    assertEquals(testMailUsername, sentMessage.getTo()[0]);
    assertEquals("Plants to water at: " + today, sentMessage.getSubject());
    assertTrue(sentMessage.getText().contains("Plant: Rose, Location: LIVING_ROOM"));
    assertTrue(sentMessage.getText().contains("Plant: Tulip, Location: KITCHEN"));
  }

  @Test
  void waterPlant_ShouldCalculateNextWateringDateCorrectly() {
    // Given
    final Plant localTestPlant = new Plant();
    localTestPlant.setId(1);
    localTestPlant.setWateringFrequency(5);

    final LocalDate waterDate = LocalDate.of(2023, 6, 15);
    final LocalDate expectedNextWaterDate = LocalDate.of(2023, 6, 20);

    when(plantRepository.findById(1L)).thenReturn(Optional.of(localTestPlant));
    when(plantMapper.toPlantDTO(localTestPlant)).thenReturn(testPlantDTO);

    // When
    plantService.waterPlant(1L, waterDate);

    // Then
    assertEquals(waterDate, localTestPlant.getLastWateredDate());
    assertEquals(expectedNextWaterDate, localTestPlant.getNextWateredDate());
  }

  @Test
  void updatePlant_ShouldThrowException_WhenLastWateredDateIsNull() {
    // Given
    final PlantDTO updateDTO = new PlantDTO(
        1L,
        "Updated Plant",
        "Updated Species",
        "Updated Description",
        "Updated Care Instructions",
        PlantLocation.KITCHEN,
        10,
        null,
        null,
        "updated-image.jpg"
    );

    when(plantRepository.findById(1L)).thenReturn(Optional.of(testPlant));
    doNothing().when(plantMapper).toPlant(testPlant, updateDTO);

    // When & Then
    assertThrows(NullPointerException.class, () -> plantService.updatePlant(updateDTO));
    verify(plantRepository).findById(1L);
    verify(plantMapper).toPlant(testPlant, updateDTO);
  }

  @Test
  void sendWateringNotification_ShouldHandleMultiplePlantsWithSameLocation() {
    // Given
    final LocalDate today = LocalDate.now();

    final Plant plant1 = new Plant();
    plant1.setId(1);
    plant1.setName("Rose");
    plant1.setLocation(PlantLocation.LIVING_ROOM);

    final Plant plant2 = new Plant();
    plant2.setId(2);
    plant2.setName("Lily");
    plant2.setLocation(PlantLocation.LIVING_ROOM);

    final Collection<Plant> plantsToWater = List.of(plant1, plant2);
    when(plantRepository.findByNextWateredDate(today)).thenReturn(plantsToWater);

    // Use reflection to set the private mailUsername field
    try {
      final java.lang.reflect.Field field = PlantService.class.getDeclaredField("mailUsername");
      field.setAccessible(true);
      field.set(plantService, testMailUsername);
    } catch (Exception e) {
      fail("Failed to set mailUsername field: " + e.getMessage());
    }

    // When
    plantService.sendWateringNotification();

    // Then
    final ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(
        SimpleMailMessage.class);
    verify(mailSender).send(messageCaptor.capture());

    final SimpleMailMessage sentMessage = messageCaptor.getValue();
    final String emailText = sentMessage.getText();
    assertTrue(emailText.contains("Plant: Rose, Location: LIVING_ROOM"));
    assertTrue(emailText.contains("Plant: Lily, Location: LIVING_ROOM"));
  }

  @Test
  void createPlant_ShouldHandleNullImageUuid() {
    // Given
    final PlantDTO plantDto = new PlantDTO(
        0L,
        "New Plant",
        "New Species",
        "New Description",
        "New Care Instructions",
        PlantLocation.BEDROOM,
        5,
        null,
        null,
        null
    );

    final Plant newPlant = new Plant();
    newPlant.setId(2);
    newPlant.setName("New Plant");
    newPlant.setSpecies("New Species");
    newPlant.setDescription("New Description");
    newPlant.setCareInstructions("New Care Instructions");
    newPlant.setLocation(PlantLocation.BEDROOM);
    newPlant.setWateringFrequency(5);
    newPlant.setImage(null);

    when(plantMapper.toPlant(plantDto, null)).thenReturn(newPlant);
    when(plantRepository.save(newPlant)).thenReturn(newPlant);

    // When
    final long result = plantService.createPlant(plantDto, null);

    // Then
    assertEquals(2L, result);
    verify(plantMapper).toPlant(plantDto, null);
    verify(plantRepository).save(newPlant);
  }
}
