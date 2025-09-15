package pl.coderslab.locations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location,Long> {
    List<Location> findByIsActiveTrue();

    List<Location> findByCityContainingIgnoreCaseAndIsActiveTrue(String city);

    List<Location> findByCity(String city);

    @Query("SELECT l FROM Location l LEFT JOIN FETCH l.events WHERE l.id = :id")
    Optional<Location> findByIdWithEvents(@PathVariable Long id);

    List<Location> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    List<Location> findByCapacityBetweenAndIsActiveTrue(Integer minCapacity, Integer maxCapacity);

    List<Location> findByCapacityGreaterThanEqualAndIsActiveTrue(Integer minCapacity);

    @Query("SELECT DISTINCT l.city FROM Location l WHERE l.city IS NOT NULL ORDER BY l.city")
    List<String> findAllCities();

    @Query("SELECT CASE WHEN COUNT(e) = 0 THEN true ELSE false END FROM Event e " +
            "WHERE e.location.id = :locationId " +
            "AND ((e.startAt BETWEEN :startTime AND :endTime) " +
            "OR (e.endAt BETWEEN :startTime AND :endTime) " +
            "OR (e.startAt <= :startTime AND e.endAt >= :endTime))")
    boolean isLocationAvailableInTimeRange(@Param("locationId") Long locationId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    @Query("SELECT l FROM Location l WHERE l.isActive = true " +
            "AND (l.capacity IS NULL OR l.capacity >= :requiredCapacity) " +
            "AND l.id NOT IN (" +
            "SELECT DISTINCT e.location.id FROM Event e " +
            "WHERE ((e.startAt BETWEEN :startTime AND :endTime) " +
            "OR (e.endAt BETWEEN :startTime AND :endTime) " +
            "OR (e.startAt <= :startTime AND e.endAt >= :endTime)))")
    List<Location> findAvailableLocations(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("requiredCapacity") Integer requiredCapacity);

    @Query("SELECT l FROM Location l LEFT JOIN l.events e " +
            "WHERE l.isActive = true " +
            "GROUP BY l.id " +
            "ORDER BY COUNT(e) DESC")
    List<Location> findMostPopularLocations(Pageable pageable);

}
