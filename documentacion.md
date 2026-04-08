# Documentación Técnica de Arquitectura de Caché
## Sistema de Gestión de Personas - REST API

---

## 1. Introducción

Este documento describe la arquitectura de cache implementada en el sistema de gestión de personas. La solución proporciona un mecanismo eficiente y thread-safe de almacenamiento en memoria utilizando patrones de diseño avanzados como Singleton genérico y delegación.

---

## 2. Descripción General de la Arquitectura

### 2.1 Objetivos de Diseño

- Proporcionar un sistema de caché genérico reutilizable para cualquier tipo de dato
- Garantizar seguridad en entornos multihilo (thread-safety)
- Eliminar código duplicado mediante herencia y delegación
- Mejorar el rendimiento de lectura mediante almacenamiento en memoria
- Proporcionar una interfaz limpia y específica para casos de uso particulares

### 2.2 Componentes Principales

El sistema está compuesto por tres clases fundamentales:

1. **Cache (Genérico)**: Almacenamiento base singleton thread-safe
2. **PersonCache**: Wrapper especializado para entities Person
3. **Person**: Entidad de dominio persistente

---

## 3. Clase Cache - Singleton Genérico

### 3.1 Propósito

Proporciona un contenedor de caché genérico thread-safe que puede almacenar cualquier combinación de tipos clave-valor. Implementa el patrón Singleton para garantizar una única instancia en toda la aplicación.

### 3.2 Señatura de la Clase

```java
public class Cache<K, V>
```

Parámetros de tipo:
- `K`: Tipo de la clave
- `V`: Tipo del valor

### 3.3 Estructura Interna

#### Variables de Instancia

```java
private static Cache<?, ?> localCache;  // Instancia Singleton
private final Map<K, V> keymap;         // Almacenamiento subyacente
```

- `localCache`: Mantiene la única instancia en memoria
- `keymap`: HashMap que almacena los pares clave-valor

#### Constructor

```java
private Cache() {
    keymap = new HashMap<>();
}
```

El constructor es privado para prevenir instanciación directa desde código externo, reforzando el patrón Singleton.

### 3.4 Métodos Públicos

#### 3.4.1 getCache() - Obtener Instancia Singleton

```java
@SuppressWarnings("unchecked")
public static <K, V> Cache<K, V> getCache()
```

**Responsabilidad**: Obtiene la única instancia de Cache, creándola si no existe.

**Parámetros**: Ninguno

**Retorna**: Instancia singleton de tipo `Cache<K, V>`

**Thread-Safety**: El método no es sincronizado. En aplicaciones multihilo, considere sincronización adicional o eager initialization.

**Implementación**:
```java
if (localCache == null) {
    localCache = new Cache<>();
}
return (Cache<K, V>) localCache;
```

#### 3.4.2 setCache() - Almacenar Valor

```java
public synchronized void setCache(K key, V value)
```

**Responsabilidad**: Almacena un valor asociado a una clave en el caché.

**Parámetros**:
- `key`: Clave única de tipo K
- `value`: Valor de tipo V

**Retorna**: Void

**Thread-Safety**: Sincronizado a nivel de método

**Implementación**:
```java
keymap.put(key, value);
```

#### 3.4.3 getCacheValue() - Recuperar Valor

```java
public synchronized V getCacheValue(K key)
```

**Responsabilidad**: Obtiene el valor asociado a una clave específica.

**Parámetros**:
- `key`: Clave de búsqueda de tipo K

**Retorna**: Valor de tipo V o null si no existe

**Thread-Safety**: Sincronizado a nivel de método

#### 3.4.4 getAllCache() - Obtener Copia del Caché Completo

```java
public synchronized Map<K, V> getAllCache()
```

**Responsabilidad**: Devuelve una copia completa del contenido del caché.

**Parámetros**: Ninguno

**Retorna**: HashMap con todos los datos de tipo `Map<K, V>`

**Thread-Safety**: Retorna copia para evitar modificaciones concurrentes

**Nota**: La copia se realiza con `new HashMap<>(keymap)` para isolamiento

#### 3.4.5 clearCache() - Limpiar Caché Completo

```java
public synchronized void clearCache()
```

**Responsabilidad**: Elimina todos los datos del caché.

**Parámetros**: Ninguno

**Retorna**: Void

**Thread-Safety**: Sincronizado a nivel de método

#### 3.4.6 removeCache() - Eliminar Entrada Específica

```java
public synchronized void removeCache(K key)
```

**Responsabilidad**: Elimina una entrada específica identificada por su clave.

**Parámetros**:
- `key`: Clave de la entrada a eliminar

**Retorna**: Void

**Thread-Safety**: Sincronizado a nivel de método

#### 3.4.7 getCacheSize() - Obtener Tamaño

```java
public synchronized int getCacheSize()
```

**Responsabilidad**: Retorna el número total de entradas en el caché.

**Parámetros**: Ninguno

**Retorna**: Integer con la cantidad de elementos

**Thread-Safety**: Sincronizado a nivel de método

### 3.5 Características de Thread-Safety

- Todos los métodos de modificación y lectura están sincronizados
- El HashMap subyacente es protegido por sincronización de métodos
- No se requiere sincronización externa al acceder desde múltiples threads

### 3.6 Limitaciones Actuales

1. El método `getCache()` no está sincronizado (double-checked locking no implementado)
2. En caso de competencia en la inicialización, puede ocurrir race condition
3. Para aplicaciones altamente concurrentes, considere implementar eager initialization o sincronización

---

## 4. Clase PersonCache - Wrapper Especializado

### 4.1 Propósito

Proporciona una interfaz especializada y type-safe para cachear objetos de tipo Person, delegando toda la funcionalidad al Cache genérico subyacente.

### 4.2 Señatura de la Clase

```java
public class PersonCache
```

### 4.3 Estructura Interna

#### Variables de Instancia

```java
private static PersonCache instance;      // Instancia Singleton
private final Cache<Long, Person> cache;  // Delegado genérico
```

- `instance`: Única instancia de PersonCache
- `cache`: Referencia al Cache genérico con tipos específicos

#### Constructor

```java
private PersonCache() {
    this.cache = Cache.getCache();
}
```

Constructor privado que obtiene la instancia singleton del Cache genérico.

### 4.4 Métodos Públicos

#### 4.4.1 getInstance() - Obtener Instancia Singleton

```java
public static synchronized PersonCache getInstance()
```

**Responsabilidad**: Obtiene la única instancia de PersonCache.

**Retorna**: Instancia singleton de PersonCache

**Thread-Safety**: Método sincronizado

#### 4.4.2 cachePerson() - Cachear Persona

```java
public synchronized void cachePerson(Long id, Person person)
```

**Responsabilidad**: Almacena una Person en el caché asociada a su ID.

**Parámetros**:
- `id`: Identificador único de tipo Long
- `person`: Objeto Person a cachear

**Retorna**: Void

**Thread-Safety**: Sincronizado a nivel de método

**Validación**: Verifica que ambos parámetros no sean null antes de cachear

**Implementación**:
```java
if (id != null && person != null) {
    cache.setCache(id, person);
}
```

#### 4.4.3 getPerson() - Recuperar Persona

```java
public synchronized Person getPerson(Long id)
```

**Responsabilidad**: Obtiene una Person del caché por su ID.

**Parámetros**:
- `id`: Identificador único de la Person

**Retorna**: Objeto Person o null si no existe

**Thread-Safety**: Sincronizado a nivel de método

#### 4.4.4 getAllCached() - Obtener Todas las Personas

```java
public synchronized Collection<Person> getAllCached()
```

**Responsabilidad**: Retorna todas las Persons almacenadas en caché.

**Parámetros**: Ninguno

**Retorna**: Colección de objetos Person

**Thread-Safety**: Retorna colección a partir de copia del caché

**Nota**: Convierte el Map a sus valores para obtener una Collection

#### 4.4.5 isCached() - Verificar Existencia

```java
public synchronized boolean isCached(Long id)
```

**Responsabilidad**: Determina si una Person está almacenada en el caché.

**Parámetros**:
- `id`: Identificador a verificar

**Retorna**: true si existe, false en caso contrario

**Thread-Safety**: Sincronizado a nivel de método

#### 4.4.6 invalidate() - Invalidar Entrada Específica

```java
public synchronized void invalidate(Long id)
```

**Responsabilidad**: Elimina una Person específica del caché.

**Parámetros**:
- `id`: Identificador de la Person a eliminar

**Retorna**: Void

**Thread-Safety**: Sincronizado a nivel de método

#### 4.4.7 invalidateAll() - Invalidar Todo el Caché

```java
public synchronized void invalidateAll()
```

**Responsabilidad**: Limpia completamente el caché de Persons.

**Parámetros**: Ninguno

**Retorna**: Void

**Thread-Safety**: Sincronizado a nivel de método

#### 4.4.8 getCacheSize() - Obtener Cantidad de Personas

```java
public synchronized int getCacheSize()
```

**Responsabilidad**: Retorna el número de Persons en caché.

**Parámetros**: Ninguno

**Retorna**: Integer con la cantidad de Persons

**Thread-Safety**: Sincronizado a nivel de método

### 4.5 Patrón de Delegación

PersonCache implementa el patrón Decorator mediante delegación:

```
PersonCache
    |
    +-- Cache<Long, Person>
            |
            +-- HashMap<Long, Person>
```

Todas las operaciones se delegan al Cache genérico subyacente, proporcionando:
- Abstracción específica para Person
- Type-safety en tiempo de compilación
- Reutilización completa de lógica genérica
- Interfaz clara y especializada

---

## 5. Clase Person - Entidad de Dominio

### 5.1 Propósito

Representa la entidad de negocio Person con persistencia en base de datos y validaciones.

### 5.2 Anotaciones JPA

```java
@Entity
public class Person
```

Mapeo a tabla de base de datos

### 5.3 Atributos

#### id
```java
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```

- Clave primaria auto-generada
- Estrategia: AUTO (base de datos decide)

#### firstName
```java
@NotBlank(message = "El nombre es requerido")
private String firstName;
```

- Nombre de la persona
- Validación: No puede ser null o vacío

#### lastName
```java
@NotBlank(message = "El apellido es requerido")
private String lastName;
```

- Apellido de la persona
- Validación: No puede ser null o vacío

### 5.4 Constructores

```java
public Person()                              // Constructor sin argumentos (JPA)
public Person(String firstName, String lastName)  // Constructor con datos
```

### 5.5 Métodos de Acceso

Getters y setters para todos los atributos con mantención de encapsulación.

---

## 6. Diagrama de Interacción

### 6.1 Flujo de Operación: Almacenar Person

```
PersonController
    |
    v
PersonCache.cachePerson(id, person)
    |
    v
Cache<Long, Person>.setCache(id, person)
    |
    v
HashMap<Long, Person>.put(id, person)
```

### 6.2 Flujo de Operación: Recuperar Person

```
PersonController
    |
    v
PersonCache.getPerson(id)
    |
    v
Cache<Long, Person>.getCacheValue(id)
    |
    v
HashMap<Long, Person>.get(id)
```

---

## 7. Patrones de Diseño Implementados

### 7.1 Singleton

**Ubicación**: Cache y PersonCache

**Propósito**: Garantizar una única instancia en toda la aplicación

**Implementación**:
- Constructor privado
- Variable estática privada
- Método estático para obtener instancia

### 7.2 Generics

**Ubicación**: Clase Cache

**Propósito**: Proporcionar type-safety y reutilización para diferentes tipos

**Beneficios**:
- Prevención de errores de tipo en compilación
- Evita casting innecesarios
- Permite reutilización para Long-Person, String-String, etc.

### 7.3 Template Method

**Ubicación**: PersonCache delega a Cache

**Propósito**: Reutilizar lógica común de almacenamiento

**Beneficio**: Una única implementación de HashMap para múltiples tipos

### 7.4 Decorator/Wrapper

**Ubicación**: PersonCache envuelve Cache

**Propósito**: Proporcionar interfaz especializada sin modificar Cache

**Beneficio**: Mantenimiento y evolucción separados

---

## 8. Consideraciones de Performance

### 8.1 Complejidad Temporal

- `setCache(K, V)`: O(1) - operación de HashMap
- `getCacheValue(K)`: O(1) - búsqueda directa
- `getSize()`: O(1) - mantiene contador
- `clearCache()`: O(n) - itera todos los elementos

### 8.2 Complejidad Espacial

- O(n) donde n es el número de elementos cacheados

### 8.3 Ventajas de Caché

- Evita consultas repetidas a base de datos
- Mejora tiempo de respuesta en operaciones GET
- Reduce carga en la base de datos

### 8.4 Desventajas Actuales

- Datos en memoria se pierden al reiniciar
- Sin expiración de datos (TTL)
- Sin límite de tamaño (OutOfMemoryError potencial)

---

## 9. Integración con el Controlador

### 9.1 PersonController

El controlador REST utiliza PersonCache para:

1. **Almacenar en caché**: Al crear o actualizar Persons
2. **Invalidar caché**: Al eliminar Persons o limpiar manualmente
3. **Proporcionar estadísticas**: Via endpoint `/api/cache/stats`

### 9.2 Endpoints de Caché

- `GET /api/cache/stats`: Estadísticas del caché
- `POST /api/cache/clear`: Limpia el caché
- `GET /api/health`: Estado de la aplicación

---

## 10. Configuración de CORS (Cross-Origin Resource Sharing)

### 10.1 ¿Qué es CORS?

**CORS (Cross-Origin Resource Sharing)** es un mecanismo de seguridad implementado por los navegadores web que controla cómo los recursos de un servidor web pueden ser accedidos desde scripts ejecutados en diferentes orígenes (dominios, protocolos o puertos).

#### 10.1.1 El Problema de Same-Origin Policy

Por motivos de seguridad, los navegadores web implementan la **Same-Origin Policy** que prohíbe:

- Scripts en `http://frontend.com` realizar peticiones XHR/Fetch a `http://api.com`
- Scripts en `http://localhost:3000` acceder a recursos en `http://localhost:8080`
- Peticiones desde protocolo `https://` a `http://`

Esto es esencial para prevenir:
- Cross-Site Scripting (XSS) attacks
- Acceso no autorizado a cookies y sesiones de usuarios
- Robo de datos sensibles

#### 10.1.2 Cómo Funciona CORS

CORS permite que los servidores especifiquen explícitamente qué orígenes pueden acceder a sus recursos:

```
1. Cliente (navegador) en origin A realiza XMLHttpRequest a origin B
2. Navegador envía header: Origin: http://origin-a.com
3. Servidor en B responde con headers CORS:
   - Access-Control-Allow-Origin: http://origin-a.com
   - Access-Control-Allow-Methods: GET, POST, PUT, DELETE
   - Access-Control-Allow-Headers: Content-Type, Authorization
4. Si los headers coinciden, navegador permite la respuesta
5. Si no coinciden, navegador bloquea la respuesta (error CORS)
```

### 10.2 Resolución en Este Proyecto

#### 10.2.1 Implementación con @CrossOrigin

En el proyecto se utilizó la anotación `@CrossOrigin` de Spring Framework para resolver el problema de CORS. Esta anotación se aplicó al controlador REST:

```java
@RestController
@RequestMapping("/api")
@CrossOrigin(
    origins = "*",                                    // Permite todos los orígenes
    allowedHeaders = "*",                             // Permite todos los headers
    methods = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE
    }                                                 // Métodos HTTP permitidos
)
public class PersonController {
    // ... controlador
}
```

#### 10.2.2 Parámetros de @CrossOrigin

| Parámetro | Valor | Significado |
|---|---|---|
| `origins` | `"*"` | Permite solicitudes desde cualquier origen |
| `allowedHeaders` | `"*"` | Permite cualquier header en la solicitud |
| `methods` | `GET, POST, PUT, DELETE` | Métodos HTTP permitidos |

#### 10.2.3 Ubicación en el Código

La anotación se encuentra en [src/main/java/com/example/accessingdatarest/PersonController.java](src/main/java/com/example/accessingdatarest/PersonController.java):

```java
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {...})
public class PersonController {
    // Todos los endpoints aquí están protegidos por CORS
    
    @GetMapping("/cache/stats")
    public Map<String, Object> getCacheStats() { ... }
    
    @PostMapping("/cache/clear")
    public Map<String, String> clearCache() { ... }
    
    // ... más endpoints
}
```

### 10.3 Flujo de Solicitud CORS

#### 10.3.1 Preflight Request (Solicitudes complejas)

Para solicitudes POST, PUT, DELETE con headers personalizados, el navegador envía un preflight:

```
OPCIONES /api/cache/stats HTTP/1.1
Host: localhost:8080
Origin: http://localhost:3000
Access-Control-Request-Method: POST
Access-Control-Request-Headers: Content-Type

---

HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: *
```

#### 10.3.2 Solicitud Actual

Una vez aprobado el preflight, se envía la solicitud real:

```
POST /api/cache/clear HTTP/1.1
Host: localhost:8080
Origin: http://localhost:3000
Content-Type: application/json

{}

---

HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Content-Type: application/json

{"message": "Caché limpiado exitosamente", "status": "success"}
```

### 10.4 Ventajas de Esta Configuración

| Ventaja | Descripción |
|---|---|
| **Acceso universal** | `origins = "*"` permite que cualquier cliente frontend acceda |
| **Flexibilidad** | Soporta múltiples métodos HTTP (GET, POST, PUT, DELETE) |
| **Headers flexibles** | `allowedHeaders = "*"` permite autenticación y content-type |
| **Configuración centralizada** | Un solo lugar para gestionar permisos CORS |
| **Simplicidad** | La anotación maneja todo automáticamente sin código adicional |

### 10.5 Consideraciones de Seguridad

#### 10.5.1 Configuración Actual (Desarrollo)

La configuración `origins = "*"` es adecuada para **desarrollo y testing**:

- ✅ Permite debugging desde múltiples clientes frontend
- ✅ Facilita pruebas cruzadas de dominios
- ✅ Sin restricciones que compliquen el desarrollo

#### 10.5.2 Recomendaciones para Producción

Para **producción**, se recomienda ser más restrictivo:

```java
@CrossOrigin(
    origins = {
        "https://app.example.com",
        "https://www.example.com"
    },
    allowedHeaders = {
        "Content-Type",
        "Authorization"
    },
    methods = {RequestMethod.GET, RequestMethod.POST}
)
```

#### 10.5.3 Headers Específicos

```java
@CrossOrigin(
    origins = {"https://secure.example.com"},
    allowedHeaders = {"Content-Type", "Authorization", "X-API-Key"},
    exposedHeaders = {"X-Total-Count"},
    allowCredentials = true,
    maxAge = 3600
)
```

| Parámetro | Descripción |
|---|---|
| `allowedHeaders` | Headers que el cliente puede enviar |
| `exposedHeaders` | Headers que el cliente puede leer en respuesta |
| `allowCredentials` | Permite envío de cookies y credenciales |
| `maxAge` | Segundos que el navegador cachea la respuesta preflight |

### 10.6 Headers CORS Generados

Spring Framework automáticamente añade estos headers en las respuestas:

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: *
Access-Control-Max-Age: 3600
```

### 10.7 Ejemplo de Uso desde Cliente Frontend

#### JavaScript/Fetch

```javascript
// Sin CORS, esto fallaría depuis origen diferente
fetch('http://localhost:8080/api/cache/stats')
    .then(response => response.json())
    .then(data => console.log(data))
    .catch(error => console.error('CORS Error:', error));

// Con POST
fetch('http://localhost:8080/api/cache/clear', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({})
})
.then(response => response.json())
.then(data => console.log(data));
```

#### React/Axios

```javascript
import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api'
});

// Automáticamente incluye headers necesarios para CORS
api.get('/cache/stats')
    .then(response => console.log(response.data))
    .catch(error => console.error(error));
```

### 10.8 Troubleshooting de CORS

#### Error Común: "Access to XMLHttpRequest has been blocked by CORS policy"

**Causa**: Origin no permitido en servidor

**Solución 1**: Verificar que @CrossOrigin tiene el dominio correcto

```java
@CrossOrigin(origins = "http://localhost:3000")  // Si el frontend corre aquí
```

**Solución 2**: Permitir todos los orígenes (solo desarrollo)

```java
@CrossOrigin(origins = "*")
```

#### Error: "Credentials mode of requests is 'include', but Access-Control-Allow-Credentials header is missing"

**Causa**: Cliente envía credenciales pero servidor no los permite

**Solución**:

```java
@CrossOrigin(
    origins = "http://localhost:3000",
    allowCredentials = true  // Habilitar credenciales
)
```

---

## 12. Inicialización de Datos

### 12.1 AccessingDataRestApplication

Al iniciar la aplicación:

```java
@Bean
public CommandLineRunner initializeCache(PersonRepository personRepository) {
    return args -> {
        // 1. Inicializa Cache legacy
        Cache.getCache().setCache("01", "Información General");
        Cache.getCache().setCache("02", "Configuración del Sistema");
        
        // 2. Crea Persons en base de datos
        Person p1 = new Person("Juan", "García");
        Person saved1 = personRepository.save(p1);
        
        // 3. Cachea las Persons
        PersonCache.getInstance().cachePerson(saved1.getId(), saved1);
    };
}
```

---

## 13. Flujo Completo de Operación

### 13.1 Crear Nueva Person

```
1. POST /api/persons {firstName, lastName}
2. PersonController.addPerson() recibe solicitud
3. PersonRepository.save() persiste en BD
4. PersonCache.cachePerson() almacena en caché
5. Respuesta: Objeto Person con ID
```

### 13.2 Buscar Person

```
1. GET /api/persons/search?lastName=García
2. PersonController.searchByLastName() consulta BD
3. PersonRepository.findByLastNameContainingIgnoreCase() retorna resultados
4. Respondse: Lista de Persons encontrados
```

### 13.3 Actualizar Person

```
1. PUT /api/persons/1 {firstName, lastName}
2. PersonController.updatePerson() recibe solicitud
3. PersonRepository.findById() verifica existencia
4. PersonRepository.save() actualiza en BD
5. PersonCache.cachePerson() actualiza en caché
6. Respuesta: Objeto Person actualizado
```

### 13.4 Eliminar Person

```
1. DELETE /api/persons/1
2. PersonController.deletePerson() recibe solicitud
3. PersonRepository.deleteById() elimina de BD
4. PersonCache.invalidate() elimina de caché
5. Respuesta: Confirmación de eliminación
```

---

## 14. Seguridad y Thread-Safety

### 14.1 Mecanismos Implementados

- Sincronización de métodos en Cache
- Sincronización de métodos en PersonCache
- Sincronización en getInstance()
- Copias de datos para aislar modificaciones externas

### 14.2 Consideraciones Residuales

- getCache() en Cache no está sincronizado (posible race condition en inicialización)
- Recomendación: Implementar eager initialization o double-checked locking

### 14.3 Recomendaciones Futuras

```java
// Opción 1: Eager Initialization
private static final Cache<?, ?> localCache = new Cache<>();

// Opción 2: Double-Checked Locking
public static <K, V> Cache<K, V> getCache() {
    if (localCache == null) {
        synchronized (Cache.class) {
            if (localCache == null) {
                localCache = new Cache<>();
            }
        }
    }
    return (Cache<K, V>) localCache;
}
```

---

## 15. Resumen de Funcionalidades

| Funcionalidad | Clase | Método | Descripción |
|---|---|---|---|
| Almacenamiento genérico | Cache | setCache() | Estira cualquier tipo K-V |
| Recuperación genérica | Cache | getCacheValue() | Obtiene valor por clave |
| Limpieza selectiva | Cache | removeCache() | Elimina una entrada |
| Limpieza completa | Cache | clearCache() | Vacía todo el caché |
| Estadísticas | Cache | getCacheSize() | Cantidad de elementos |
| Cacheo de Person | PersonCache | cachePerson() | Almacena Person por ID |
| Búsqueda de Person | PersonCache | getPerson() | Obtiene Person por ID |
| Validación | PersonCache | isCached() | Verifica existencia |
| Invalidación selectiva | PersonCache | invalidate() | Elimina Person específica |
| Invalidación total | PersonCache | invalidateAll() | Vacía caché de Persons |

---

## 16. Conclusiones

La arquitectura implementada proporciona:

1. Un sistema de caché genérico reutilizable mediante tipos parametrizados
2. Interfaces especializadas para casos de uso particulares
3. Thread-safety garantizado mediante sincronización
4. Eliminación de código duplicado mediante delegación
5. Mejora de performance mediante almacenamiento en memoria
6. Mantenibilidad mediante separación de responsabilidades

La solución balance entre simplicidad, funcionalidad y extensibilidad, proporcionando una base sólida para futuros enhancements del sistema.

---

**Documento Técnico**  
Gestión de Personas - REST API  
Versión 2.0 - 2026
