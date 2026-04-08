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

    // Inicializa datos de prueba y carga el caché al iniciar la aplicación
    @Bean
    public CommandLineRunner initializeCache(PersonRepository personRepository) {
        return args -> {
            // Crear objetos Person de prueba
            Person p1 = new Person("Juan", "García");
            Person p2 = new Person("María", "López");
            Person p3 = new Person("Carlos", "Martínez");

            // Guardar en base de datos
            Person saved1 = personRepository.save(p1);
            Person saved2 = personRepository.save(p2);
            Person saved3 = personRepository.save(p3);

            // Almacenar en caché
            Cache.getCache().setCache(saved1.getId(), saved1);
            Cache.getCache().setCache(saved2.getId(), saved1);
            Cache.getCache().setCache(saved3.getId(), saved1);

            // Mostrar información en consola
            System.out.println("Caché inicializado correctamente");
            System.out.println("Total en caché: " + Cache.getCache().getCacheSize());
            System.out.println("Contenido: " + Cache.getCache().getAllCached());
        };
    }
}