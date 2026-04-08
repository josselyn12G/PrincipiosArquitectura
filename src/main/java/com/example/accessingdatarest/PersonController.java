package com.example.accessingdatarest;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST personalizado para Person.
 * Proporciona endpoints adicionales para caché y operaciones CRUD.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class PersonController {

    private final PersonRepository personRepository;
    private final PersonCache personCache;

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
        this.personCache = PersonCache.getInstance();
    }

    /**
     * GET /api/cache/stats - Obtiene estadísticas del caché
     */
    @GetMapping("/cache/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", personCache.getCacheSize());
        stats.put("cachedPeople", personCache.getAllCached());
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    /**
     * POST /api/cache/clear - Limpia el caché manualmente
     */
    @PostMapping("/cache/clear")
    public Map<String, String> clearCache() {
        personCache.invalidateAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Caché limpiado exitosamente");
        response.put("status", "success");
        return response;
    }

    /**
     * GET /api/health - Health check del servidor
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Person CRUD API");
        return response;
    }

    /**
     * PUT /api/persons/{id} - Actualiza una persona existente
     */
    @PutMapping("/persons/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @Valid @RequestBody Person person) {
        Optional<Person> existing = personRepository.findById(id);
        
        if (!existing.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Person p = existing.get();
        p.setFirstName(person.getFirstName());
        p.setLastName(person.getLastName());
        
        Person updated = personRepository.save(p);
        personCache.cachePerson(updated.getId(), updated);
        
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/persons/{id} - Elimina una persona
     */
    @DeleteMapping("/persons/{id}")
    public ResponseEntity<?> deletePerson(@PathVariable Long id) {
        Optional<Person> existing = personRepository.findById(id);
        
        if (!existing.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        personRepository.deleteById(id);
        personCache.invalidate(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Persona eliminada correctamente");
        response.put("id", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/persons - Obtiene todas las personas (alternativa a /people)
     */
    @GetMapping("/persons")
    public Iterable<Person> getAllPersons() {
        return personRepository.findAll();
    }

    /**
     * GET /api/persons/{id} - Obtiene una persona por ID
     */
    @GetMapping("/persons/{id}")
    public ResponseEntity<?> getPersonById(@PathVariable Long id) {
        Optional<Person> person = personRepository.findById(id);
        if (person.isPresent()) {
            personCache.cachePerson(id, person.get());
            return ResponseEntity.ok(person.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/persons - Crea una nueva persona
     */
    @PostMapping("/persons")
    public ResponseEntity<?> createPerson(@Valid @RequestBody Person person) {
        Person saved = personRepository.save(person);
        personCache.cachePerson(saved.getId(), saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * GET /api/persons/search?lastName=X - Busca personas por apellido
     */
    @GetMapping("/persons/search")
    public Iterable<Person> searchByLastName(@RequestParam(name = "lastName", required = false) String lastName) {
        if (lastName != null && !lastName.isEmpty()) {
            return personRepository.findByLastName(lastName);
        }
        return personRepository.findAll();
    }
}
