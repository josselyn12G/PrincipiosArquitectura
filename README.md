# CORS (Cross-Origin Resource Sharing) en Ambientes de Desarrollo


## ¿Qué es CORS?

**CORS (Cross-Origin Resource Sharing)** es un mecanismo de seguridad implementado por los navegadores web que controla cómo los recursos de un servidor web pueden ser accedidos desde scripts ejecutados en diferentes orígenes (dominios, protocolos o puertos).

### El Problema de Same-Origin Policy

Por motivos de seguridad, los navegadores web implementan la **Same-Origin Policy** que prohíbe:

- Scripts en `http://frontend.com` realizar peticiones XHR/Fetch a `http://api.com`
- Scripts en `http://localhost:3000` acceder a recursos en `http://localhost:8080`
- Peticiones desde protocolo `https://` a `http://`

Esto es esencial para prevenir:
- **Cross-Site Scripting (XSS) attacks**: Inyección de código malicioso
- **Acceso no autorizado a cookies y sesiones**: Robo de autenticación
- **Robo de datos sensibles**: Información privada del usuario

---

## ¿Por Qué se Genera?

CORS surge como solución porque en el desarrollo moderno es común tener:

1. **Microservicios separados**: Frontend en un puerto (ej: 3000) y backend en otro (ej: 8080)
2. **Desarrollo distribuido**: Equipos que trabajan en diferentes dominios
3. **APIs compartidas**: Un mismo servicio API usado por múltiples clientes
4. **Testing cruzado**: Necesidad de hacer peticiones desde diferentes orígenes

Sin CORS, cualquier petición HTTP desde un origen diferente al servidor sería bloqueada automáticamente por el navegador.

### Cómo Funciona CORS

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

---

## Cómo lo Solucionamos

En este proyecto utilizamos la anotación `@CrossOrigin` de **Spring Framework** para resolver el problema de CORS de manera elegante y centralizada.

### Implementación con @CrossOrigin

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
    // Todos los endpoints aquí están protegidos por CORS
    
    @GetMapping("/cache/stats")
    public Map<String, Object> getCacheStats() { ... }
    
    @PostMapping("/cache/clear")
    public Map<String, String> clearCache() { ... }
    
    // ... más endpoints
}
```

### Ubicación en el Código

La anotación se encuentra en:
```
src/main/java/com/example/accessingdatarest/PersonController.java
```

---

## Configuración Detallada

### Parámetros de @CrossOrigin

| Parámetro | Valor | Significado |
|---|---|---|
| `origins` | `"*"` | Permite solicitudes desde cualquier origen |
| `allowedHeaders` | `"*"` | Permite cualquier header en la solicitud |
| `methods` | `GET, POST, PUT, DELETE` | Métodos HTTP permitidos |

### Headers CORS Generados

Spring Framework automáticamente añade estos headers en las respuestas:

```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: *
Access-Control-Max-Age: 3600
```

---

## Consideraciones de Seguridad

### Configuración Actual (Desarrollo)

La configuración `origins = "*"` es adecuada para **desarrollo y testing**:

- Permite debugging desde múltiples clientes frontend
- Facilita pruebas cruzadas de dominios
- Sin restricciones que compliquen el desarrollo

### Recomendaciones para Producción

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


## Referencias

### Documentación Oficial

- [MDN - CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Spring Framework - @CrossOrigin](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/CrossOrigin.html)
- [W3C - CORS Specification](https://www.w3.org/TR/cors/)

### Artículos Relacionados

- [Understanding CORS - Mozilla](https://developer.mozilla.org/en-US/docs/Glossary/CORS)
- [How to Fix Common CORS Issues](https://www.digitalocean.com/community/tutorials/how-to-handle-cors-using-node-js)
- [Security Considerations for CORS](https://owasp.org/www-community/CORS)

### Buenas Prácticas

- [OWASP - Cross-Site Request Forgery (CSRF)](https://owasp.org/www-community/attacks/csrf)
- [Node.js CORS Middleware](https://github.com/expressjs/cors)
- [Spring Boot CORS Configuration](https://spring.io/guides/gs/rest-service-cors/)

---

**Última actualización**: Abril 2026

