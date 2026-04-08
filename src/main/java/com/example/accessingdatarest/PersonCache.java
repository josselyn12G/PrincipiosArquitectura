package com.example.accessingdatarest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Caché especializado para objetos Person.
 * Utiliza la clase Cache genérica para almacenar Persons por ID.
 * Proporciona métodos específicos para Person con tipo seguro.
 */
public class PersonCache {
    private static PersonCache instance;
    private final Cache<Long, Person> cache;

    private PersonCache() {
        this.cache = Cache.getCache();
    }

    /**
     * Obtiene la instancia única de PersonCache.
     */
    public static synchronized PersonCache getInstance() {
        if (instance == null) {
            instance = new PersonCache();
        }
        return instance;
    }

    /**
     * Cachea una Person por su ID.
     */
    public synchronized void cachePerson(Long id, Person person) {
        if (id != null && person != null) {
            cache.setCache(id, person);
        }
    }

    /**
     * Obtiene una Person del caché por su ID.
     */
    public synchronized Person getPerson(Long id) {
        return cache.getCacheValue(id);
    }

    /**
     * Obtiene todas las Persons almacenadas en caché.
     */
    public synchronized Collection<Person> getAllCached() {
        return new HashMap<>(cache.getAllCache()).values();
    }

    /**
     * Verifica si una Person está en caché.
     */
    public synchronized boolean isCached(Long id) {
        return cache.getCacheValue(id) != null;
    }

    /**
     * Elimina una Person del caché por su ID.
     */
    public synchronized void invalidate(Long id) {
        cache.removeCache(id);
    }

    /**
     * Limpia completamente el caché de Persons.
     */
    public synchronized void invalidateAll() {
        cache.clearCache();
    }

    /**
     * Obtiene el número de Persons en caché.
     */
    public synchronized int getCacheSize() {
        return cache.getCacheSize();
    }
}

