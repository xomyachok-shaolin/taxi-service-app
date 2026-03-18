package com.taxiservice.repository;

import com.taxiservice.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Vehicle}.
 * Предоставляет методы доступа к транспортным средствам.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Поиск транспортных средств по ID водителя.
     *
     * @param driverId ID водителя
     * @return список транспортных средств
     */
    List<Vehicle> findByDriverId(Long driverId);

    /**
     * Поиск транспортного средства по госномеру.
     *
     * @param licensePlate госномер
     * @return Optional с транспортным средством
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    /**
     * Проверка существования транспортного средства по госномеру.
     *
     * @param licensePlate госномер
     * @return true если существует
     */
    boolean existsByLicensePlate(String licensePlate);

    /**
     * Поиск по марке (без учёта регистра).
     *
     * @param brand марка
     * @return список транспортных средств
     */
    List<Vehicle> findByBrandContainingIgnoreCase(String brand);
}
