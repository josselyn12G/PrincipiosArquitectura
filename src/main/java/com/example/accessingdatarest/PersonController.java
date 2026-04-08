package com.example.accessingdatarest;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Controlador REST encargado de gestionar las operaciones CRUD de la entidad Person
// Incluye funcionalidades adicionales como gestión de caché y verificación del servicio
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*",allowedHeaders = "*",methods = {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class PersonController {

    // Repositorio utilizado para la persistencia de datos
    private final PersonRepository personRepository;

    // Instancia del sistema de caché para optimizar consultas
    private final Cache personCache;

    // Constructor que implementa inyección de dependencias
    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
        this.personCache = Cache.getCache();
    }

    // Endpoint para obtener estadísticas del caché
    @GetMapping("/cache/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", personCache.getCacheSize());
        stats.put("cachedPeople", personCache.getAllCached());
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    // Endpoint para limpiar completamente el caché
    @PostMapping("/cache/clear")
    public Map<String, String> clearCache() {
        personCache.deleteAll();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Caché limpiado exitosamente");
        response.put("status", "success");

        return response;
    }

    // Endpoint de verificación del estado del servicio (health check)
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Person CRUD API");
        return response;
    }

    // Endpoint para actualizar una persona existente
    @PutMapping("/persons/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @Valid @RequestBody Person person) {

        Optional<Person> existing = personRepository.findById(id);

        // Verifica si la persona existe
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Actualiza los datos
        Person p = existing.get();
        p.setFirstName(person.getFirstName());
        p.setLastName(person.getLastName());

        Person updated = personRepository.save(p);

        // Actualiza el caché
        personCache.setCache(updated.getId(), updated);

        return ResponseEntity.ok(updated);
    }

    // Endpoint para eliminar una persona
    @DeleteMapping("/persons/{id}")
    public ResponseEntity<?> deletePerson(@PathVariable Long id) {

        Optional<Person> existing = personRepository.findById(id);

        // Verifica si existe antes de eliminar
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        personRepository.deleteById(id);

        // Elimina del caché
        personCache.delete(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Persona eliminada correctamente");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    // Endpoint para obtener todas las personas
    @GetMapping("/persons")
    public Iterable<Person> getAllPersons() {
        return personRepository.findAll();
    }

    // Endpoint para obtener una persona por ID
    @GetMapping("/persons/{id}")
    public ResponseEntity<?> getPersonById(@PathVariable Long id) {

        // 1. Buscar en caché primero
        Person cached = personCache.getPerson(id);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        // 2. Si no está en caché, buscar en base de datos
        Optional<Person> person = personRepository.findById(id);

        if (person.isPresent()) {
            // Guardar en caché
            personCache.setCache(id, person.get());
            return ResponseEntity.ok(person.get());
        }

        return ResponseEntity.notFound().build();
    }

    // Endpoint para crear una nueva persona
    @PostMapping("/persons")
    public ResponseEntity<?> createPerson(@Valid @RequestBody Person person) {

        Person saved = personRepository.save(person);

        // Se almacena en caché
        personCache.setCache(saved.getId(), saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Endpoint para buscar personas por apellido
    @GetMapping("/persons/search")
    public Iterable<Person> searchByLastName(
            @RequestParam(name = "lastName", required = false) String lastName) {

        // Si se proporciona el parámetro, filtra por apellido
        if (lastName != null && !lastName.isEmpty()) {
            return personRepository.findByLastName(lastName);
        }

        // Caso contrario, devuelve todos los registros
        return personRepository.findAll();
    }
}