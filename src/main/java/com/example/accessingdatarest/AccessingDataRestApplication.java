package com.example.accessingdatarest;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AccessingDataRestApplication {

  public static void main(String[] args) {
    SpringApplication.run(AccessingDataRestApplication.class, args);
  }

  /**
   * CommandLineRunner para inicializar datos y caché al arrancar la aplicación.
   */
  @Bean
  public CommandLineRunner initializeCache(PersonRepository personRepository) {
    return args -> {
      // Inicializar caché legacy (String-String)
      Cache.getCache().setCache("01", "Información General");
      Cache.getCache().setCache("02", "Configuración del Sistema");
      System.out.println("✓ Caché legacy inicializado");
      System.out.println("  - Cache01: " + Cache.getCache().getCacheValue("01"));
      System.out.println("  - Cache02: " + Cache.getCache().getCacheValue("02"));

      // Inicializar PersonCache con datos de prueba
      PersonCache personCache = PersonCache.getInstance();
      
      // Crear y guardar Persons en base de datos
      Person p1 = new Person("Juan", "García");
      Person p2 = new Person("María", "López");
      Person p3 = new Person("Carlos", "Martínez");
      
      Person saved1 = personRepository.save(p1);
      Person saved2 = personRepository.save(p2);
      Person saved3 = personRepository.save(p3);
      
      // Cachear las personas
      personCache.cachePerson(saved1.getId(), saved1);
      personCache.cachePerson(saved2.getId(), saved2);
      personCache.cachePerson(saved3.getId(), saved3);
      
      System.out.println("\n✓ PersonCache inicializado con " + personCache.getCacheSize() + " personas");
      System.out.println("  - Personas en caché: " + personCache.getAllCached());
    };
  }
}
