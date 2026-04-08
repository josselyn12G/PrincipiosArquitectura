// ============================================
// CONFIGURACIÓN Y VARIABLES GLOBALES
// ============================================
const API_BASE_URL = 'http://localhost:8080';
const PEOPLE_API = '/api/persons';  // Usar el endpoint personalizado más confiable
let currentEditingId = null;
let editModal = null;

// Inicializar cuando carga el DOM
document.addEventListener('DOMContentLoaded', function() {
    editModal = new bootstrap.Modal(document.getElementById('editModal'));
    loadAllPeople();
    updateCacheStatus();
    // Actualizar caché cada 5 segundos
    setInterval(updateCacheStatus, 5000);
});

// ============================================
// FUNCIONES DE CARGA DE DATOS
// ============================================

/**
 * Carga todas las personas desde la API
 */
function loadAllPeople() {
    showLoading();
    fetch(`${API_BASE_URL}${PEOPLE_API}`)
        .then(response => {
            if (!response.ok) throw new Error('Error al cargar personas');
            return response.json();
        })
        .then(data => {
            // El endpoint /api/persons retorna directamente un array
            const people = Array.isArray(data) ? data : [];
            renderPeople(people);
            updateTableCount(people.length);
            showAlert('Datos cargados correctamente', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Error al cargar las personas: ' + error.message, 'danger');
            document.getElementById('peopleTable').innerHTML = `
                <tr>
                    <td colspan="4" class="text-center text-danger py-4">
                        ❌ Error al cargar datos
                    </td>
                </tr>
            `;
        });
}

/**
 * Busca personas por apellido
 */
/**
 * Busca personas por apellido
 */
function searchByLastName() {
    const lastName = document.getElementById('searchLastName').value.trim();
    
    if (!lastName) {
        showAlert('Por favor ingresa un apellido', 'warning');
        return;
    }

    showLoading();
    fetch(`${API_BASE_URL}/api/persons/search?lastName=${encodeURIComponent(lastName)}`)
        .then(response => {
            if (!response.ok) throw new Error('Error en la búsqueda');
            return response.json();
        })
        .then(data => {
            // El endpoint personalizado retorna directamente un array
            const people = Array.isArray(data) ? data : [];
            if (people.length === 0) {
                showAlert(`No se encontraron personas con apellido: ${lastName}`, 'info');
            }
            renderPeople(people);
            updateTableCount(people.length);
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Error en la búsqueda: ' + error.message, 'danger');
        });
}

/**
 * Renderiza la tabla de personas
 */
function renderPeople(people) {
    const tbody = document.getElementById('peopleTable');
    
    if (!people || people.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center text-muted py-4">
                    📭 No hay personas registradas
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = people.map(person => `
        <tr class="align-middle fade-in">
            <td>
                <span class="badge bg-secondary">${person.id}</span>
            </td>
            <td>${escapeHtml(person.firstName)}</td>
            <td>${escapeHtml(person.lastName)}</td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editPerson(${person.id}, '${escapeAttr(person.firstName)}', '${escapeAttr(person.lastName)}')">
                    ✏️ Editar
                </button>
                <button class="btn btn-sm btn-danger" onclick="deletePerson(${person.id})">
                    🗑️ Eliminar
                </button>
            </td>
        </tr>
    `).join('');
}

/**
 * Actualiza el contador de registros
 */
function updateTableCount(count) {
    document.getElementById('tableCount').textContent = `${count} registro${count !== 1 ? 's' : ''}`;
}

// ============================================
// FUNCIONES CRUD
// ============================================

/**
 * Agrega una nueva persona
 */
function addPerson(event) {
    event.preventDefault();

    const firstName = document.getElementById('firstName').value.trim();
    const lastName = document.getElementById('lastName').value.trim();

    if (!firstName || !lastName) {
        showAlert('Por favor completa todos los campos', 'warning');
        return;
    }

    const person = { firstName, lastName };

    fetch(`${API_BASE_URL}${PEOPLE_API}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(person)
    })
        .then(response => {
            if (!response.ok) throw new Error('Error al crear persona');
            return response.json();
        })
        .then(data => {
            showAlert('✅ Persona creada exitosamente', 'success');
            resetForm();
            loadAllPeople();
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Error al crear la persona: ' + error.message, 'danger');
        });
}

/**
 * Abre el modal de edición
 */
function editPerson(id, firstName, lastName) {
    currentEditingId = id;
    document.getElementById('editFirstName').value = firstName;
    document.getElementById('editLastName').value = lastName;
    editModal.show();
}

/**
 * Guarda los cambios de edición
 */
function saveEdit() {
    const firstName = document.getElementById('editFirstName').value.trim();
    const lastName = document.getElementById('editLastName').value.trim();

    if (!firstName || !lastName) {
        showAlert('Por favor completa todos los campos', 'warning');
        return;
    }

    // Spring Data REST requiere enviar el ID en el body para PUT
    const person = { 
        id: currentEditingId,
        firstName, 
        lastName 
    };

    fetch(`${API_BASE_URL}${PEOPLE_API}/${currentEditingId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(person)
    })
        .then(response => {
            // Spring Data REST puede no retornar JSON en PUT, solo verificar status
            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            // No intentes parsear JSON si la respuesta está vacía
            const contentType = response.headers.get('content-type');
            return contentType && contentType.includes('application/json') 
                ? response.json() 
                : response;
        })
        .then(data => {
            editModal.hide();
            showAlert('✅ Persona actualizada exitosamente', 'success');
            loadAllPeople();
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Error al actualizar: ' + error.message, 'danger');
        });
}

/**
 * Elimina una persona
 */
/**
 * Elimina una persona
 */
function deletePerson(id) {
    if (!confirm('¿Estás seguro de que deseas eliminar esta persona?')) {
        return;
    }

    showLoading();
    fetch(`${API_BASE_URL}${PEOPLE_API}/${id}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Error ${response.status}: ${response.statusText}`);
            }
            // Spring Data REST DELETE no retorna contenido
            showAlert('✅ Persona eliminada exitosamente', 'success');
            loadAllPeople();
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Error al eliminar: ' + error.message, 'danger');
            loadAllPeople(); // Recargar de todas formas
        });
}

/**
 * Limpia el formulario
 */
function resetForm() {
    document.getElementById('personForm').reset();
    document.getElementById('firstName').focus();
}

// ============================================
// FUNCIONES DE CACHÉ
// ============================================

/**
 * Actualiza el estado del caché
 */
function updateCacheStatus() {
    fetch(`${API_BASE_URL}/api/cache/stats`)
        .then(response => response.json())
        .then(data => {
            const cacheStatus = document.getElementById('cacheStatus');
            cacheStatus.textContent = `Cache: ${data.cacheSize} ${data.cacheSize !== 1 ? 'items' : 'item'}`;
        })
        .catch(error => console.log('Cache status unavailable:', error));
}

/**
 * Limpia el caché del servidor
 */
function clearCache() {
    if (!confirm('¿Estás seguro de que deseas limpiar el caché?')) {
        return;
    }

    fetch(`${API_BASE_URL}/api/cache/clear`, { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            showAlert('✅ Caché limpiado correctamente', 'success');
            updateCacheStatus();
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Error al limpiar caché', 'danger');
        });
}

// ============================================
// FUNCIONES DE UTILIDAD
// ============================================

/**
 * Muestra una alerta
 */
function showAlert(message, type = 'info') {
    const alertContainer = document.getElementById('alertContainer');
    const alertId = 'alert-' + Date.now();

    const alertHTML = `
        <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;

    alertContainer.innerHTML = alertHTML;

    // Auto-cerrar después de 5 segundos
    setTimeout(() => {
        const alertElement = document.getElementById(alertId);
        if (alertElement) {
            const alert = new bootstrap.Alert(alertElement);
            alert.close();
        }
    }, 5000);
}

/**
 * Muestra estado de carga
 */
function showLoading() {
    const tbody = document.getElementById('peopleTable');
    tbody.innerHTML = `
        <tr>
            <td colspan="4" class="text-center py-4">
                <div class="spinner-border spinner-border-sm text-primary" role="status">
                    <span class="visually-hidden">Cargando...</span>
                </div>
                <p class="mt-2">Cargando datos...</p>
            </td>
        </tr>
    `;
}

/**
 * Escapa caracteres especiales en HTML
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Escapa caracteres para atributos HTML
 */
function escapeAttr(text) {
    return text.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}
