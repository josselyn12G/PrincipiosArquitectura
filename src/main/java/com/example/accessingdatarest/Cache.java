package com.example.accessingdatarest;

import java.util.HashMap;
import java.util.Map;

// Clase Singleton para gestionar el caché de objetos Person
public class Cache {

    // Instancia única del caché
    private static Cache localCache;

    // Estructura de almacenamiento: ID - Objeto Person
    private Map<Long, Person> cacheMap;

    // Constructor privado
    private Cache() {
        this.cacheMap = new HashMap<>();
    }

    // Método para obtener la instancia única (Singleton)
    public static Cache getCache() {
        // Si no existe una instancia, crear una nueva
        if (localCache == null) {
            // Crear una nueva instancia de la clase Cache
            localCache = new Cache();
        }
        // Devolver la instancia de la clase Cache
        return localCache;
    }

    // Guardar una persona en caché
    public void setCache(Long id, Person person) {
        cacheMap.put(id, person);
    }

    // Obtener una persona desde caché
    public Person getPerson(Long id) {
        return cacheMap.get(id);
    }

    // Eliminar una persona del caché
    public void delete(Long id) {
        cacheMap.remove(id);
    }

    // Limpiar todo el caché
    public void deleteAll() {
        cacheMap.clear();
    }

    // Obtener todos los elementos en caché
    public Map<Long, Person> getAllCached() {
        return cacheMap;
    }

    // Obtener tamaño del caché
    public int getCacheSize() {
        return cacheMap.size();
    }
}