package pl.coderslab.locations;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    // CRUD Operations
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public List<Location> getActiveLocations() {
        return locationRepository.findByIsActiveTrue();
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + id));
    }

    public Location createLocation(Location location) {
        validateLocation(location);
        location.setIsActive(true);
        return locationRepository.save(location);
    }

    public Location updateLocation(Long id, Location locationDetails) {
        Location location = getLocationById(id);

        validateLocation(locationDetails);

        location.setName(locationDetails.getName());
        location.setAddress(locationDetails.getAddress());
        location.setCity(locationDetails.getCity());
        location.setLatitude(locationDetails.getLatitude());
        location.setLongitude(locationDetails.getLongitude());
        location.setCapacity(locationDetails.getCapacity());
        location.setDescription(locationDetails.getDescription());
        location.setContactInfo(locationDetails.getContactInfo());

        return locationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        Location location = getLocationById(id);

        if (!location.getEvents().isEmpty()) {
            throw new IllegalStateException("Cannot delete location with assigned events. Deactivate instead.");
        }

        locationRepository.delete(location);
    }

    public Location deactivateLocation(Long id) {
        Location location = getLocationById(id);
        location.setIsActive(false);
        return locationRepository.save(location);
    }

    public Location activateLocation(Long id) {
        Location location = getLocationById(id);
        location.setIsActive(true);
        return locationRepository.save(location);
    }

    public List<Location> searchLocationsByCity(String city) {
        return locationRepository.findByCityContainingIgnoreCaseAndIsActiveTrue(city);
    }

    public List<Location> searchLocationsByName(String name) {
        return locationRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
    }

    public List<Location> getLocationsByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        return locationRepository.findByCapacityBetweenAndIsActiveTrue(minCapacity, maxCapacity);
    }

    public List<Location> getLocationsByMinCapacity(Integer minCapacity) {
        return locationRepository.findByCapacityGreaterThanEqualAndIsActiveTrue(minCapacity);
    }

    public boolean isLocationAvailable(Long locationId, LocalDateTime startTime, LocalDateTime endTime) {
        Location location = getLocationById(locationId);

        if (!location.getIsActive()) {
            return false;
        }

        return locationRepository.isLocationAvailableInTimeRange(locationId, startTime, endTime);
    }

    public List<Location> getAvailableLocations(LocalDateTime startTime, LocalDateTime endTime, Integer requiredCapacity) {
        return locationRepository.findAvailableLocations(startTime, endTime, requiredCapacity);
    }

    // Validation
    private void validateLocation(Location location) {
        if (location.getName() == null || location.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Location name is required");
        }

        if (location.getName().length() > 100) {
            throw new IllegalArgumentException("Location name cannot exceed 100 characters");
        }

        if (location.getAddress() != null && location.getAddress().length() > 100) {
            throw new IllegalArgumentException("Address cannot exceed 100 characters");
        }

        if (location.getCity() != null && location.getCity().length() > 20) {
            throw new IllegalArgumentException("City cannot exceed 20 characters");
        }

        if (location.getCapacity() != null && location.getCapacity() < 1) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        if (location.getLatitude() != null && (location.getLatitude().compareTo(new BigDecimal("-90")) < 0 || location.getLatitude().compareTo(new BigDecimal("90")) > 0)) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }

        if (location.getLongitude() != null && (location.getLongitude().compareTo(new BigDecimal("-180")) < 0 || location.getLongitude().compareTo(new BigDecimal("180")) > 0)) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        if (location.getDescription() != null && location.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }

        if (location.getContactInfo() != null) {
            if (location.getContactInfo().length() > 255) {
                throw new IllegalArgumentException("Contact info cannot exceed 255 characters");
            }

            if (location.getContactInfo().contains("@") &&
                    !location.getContactInfo().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format for contact info");
            }
        }
    }

    public long getTotalLocationsCount() {
        return locationRepository.count();
    }
}
