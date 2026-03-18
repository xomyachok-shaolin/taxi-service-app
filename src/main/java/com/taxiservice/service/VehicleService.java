package com.taxiservice.service;

import com.taxiservice.model.Vehicle;
import com.taxiservice.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления транспортными средствами.
 * Обеспечивает CRUD-операции и поиск автомобилей.
 *
 * @version 1.0
 */
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Сохранение транспортного средства.
     *
     * @param vehicle транспортное средство
     * @return сохранённый объект
     * @throws IllegalArgumentException если госномер уже занят
     */
    @Transactional
    public Vehicle save(Vehicle vehicle) {
        if (vehicle.getId() == null && vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new IllegalArgumentException(
                    "Автомобиль с госномером '" + vehicle.getLicensePlate() + "' уже зарегистрирован");
        }
        return vehicleRepository.save(vehicle);
    }

    /**
     * Поиск транспортного средства по ID.
     *
     * @param id идентификатор
     * @return Optional с транспортным средством
     */
    public Optional<Vehicle> findById(Long id) {
        return vehicleRepository.findById(id);
    }

    /**
     * Получение всех транспортных средств.
     *
     * @return список ТС
     */
    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    /**
     * Получение транспортных средств водителя.
     *
     * @param driverId ID водителя
     * @return список ТС водителя
     */
    public List<Vehicle> findByDriverId(Long driverId) {
        return vehicleRepository.findByDriverId(driverId);
    }

    /**
     * Поиск по марке автомобиля.
     *
     * @param brand марка
     * @return список найденных ТС
     */
    public List<Vehicle> searchByBrand(String brand) {
        return vehicleRepository.findByBrandContainingIgnoreCase(brand);
    }

    /**
     * Удаление транспортного средства.
     *
     * @param id идентификатор
     */
    @Transactional
    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }
}
