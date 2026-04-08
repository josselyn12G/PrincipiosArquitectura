package com.example.accessingdatarest;

import java.util.HashMap;
import java.util.Map;

/**
 * Caché genérico singleton thread-safe.
 * Puede almacenar cualquier tipo de dato con clave y valor genéricos.
 * @param <K> Tipo de la clave
 * @param <V> Tipo del valor
 */
public class Cache<K, V> {
    // Crear una instancia privada estática de la clase Cache
	private static Cache<?, ?> localCache; 
	// Crear un mapa para almacenar los datos en caché
	private final Map<K, V> keymap;
    // Hacer el constructor privado para evitar que se puedan crear instancias desde fuera de la clase
	private Cache() {
		keymap = new HashMap<>();
	} 
    // Método público estático para obtener la instancia de la clase Cache
	@SuppressWarnings("unchecked")
	public static <K, V> Cache<K, V> getCache() { 
        // Si no existe una instancia, crear una nueva
		if (localCache == null) { 
            // Crear una nueva instancia de la clase Cache
			localCache = new Cache<>();
		}
        // Devolver la instancia de la clase Cache
		return (Cache<K, V>) localCache;
	}
	
	// Método público sincronizado para almacenar un valor en la caché
	public synchronized void setCache(K key, V value) {
		keymap.put(key, value);
	}
	// Método público sincronizado para obtener un valor de la caché dado una clave
	public synchronized V getCacheValue(K key) {
		return keymap.get(key);
	}
	// Método público sincronizado para obtener todas las claves
	public synchronized Map<K, V> getAllCache() {
		return new HashMap<>(keymap);
	}
	// Método público sincronizado para limpiar toda la caché
	public synchronized void clearCache() {
		keymap.clear();
	}
	// Método público sincronizado para eliminar una clave específica
	public synchronized void removeCache(K key) {
		keymap.remove(key);
	}
	// Método público sincronizado para obtener el tamaño del caché
	public synchronized int getCacheSize() {
		return keymap.size();
	}
}