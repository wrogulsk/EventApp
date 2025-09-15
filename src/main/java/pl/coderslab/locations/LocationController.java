package pl.coderslab.locations;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Location>> getActiveLocations() {
        List<Location> locations = locationService.getActiveLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        Location location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        Location createdLocation = locationService.createLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @RequestBody Location location) {
        Location updatedLocation = locationService.updateLocation(id, location);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Location> deactivateLocation(@PathVariable Long id) {
        Location location = locationService.deactivateLocation(id);
        return ResponseEntity.ok(location);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Location> activateLocation(@PathVariable Long id) {
        Location location = locationService.activateLocation(id);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/search/city")
    public ResponseEntity<List<Location>> searchByCity(@RequestParam String city) {
        List<Location> locations = locationService.searchLocationsByCity(city);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Location>> searchByName(@RequestParam String name) {
        List<Location> locations = locationService.searchLocationsByName(name);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/capacity")
    public ResponseEntity<List<Location>> getLocationsByCapacity(
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity) {

        List<Location> locations;
        if (minCapacity != null && maxCapacity != null) {
            locations = locationService.getLocationsByCapacityRange(minCapacity, maxCapacity);
        } else if (minCapacity != null) {
            locations = locationService.getLocationsByMinCapacity(minCapacity);
        } else {
            locations = locationService.getActiveLocations();
        }

        return ResponseEntity.ok(locations);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Location>> getAvailableLocations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Integer requiredCapacity) {

        List<Location> locations = locationService.getAvailableLocations(startTime, endTime, requiredCapacity);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        boolean available = locationService.isLocationAvailable(id, startTime, endTime);
        return ResponseEntity.ok(available);
    }

}


