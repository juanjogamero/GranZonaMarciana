# Mejoras técnicas · Gran Zona Marciana

## Resumen

Se ha conservado la arquitectura nativa existente: Java, actividades Android, adaptadores, servicios, DAO y Room/SQLite. No se ha sustituido la aplicación por una página web.

### Cambios sobre el proyecto original

- **Cuentas iniciales:** ya no se introducen tres usuarios con contraseñas conocidas. En una instalación nueva se solicita crear la primera cuenta administrativa en el propio dispositivo.
- **Inicio de sesión:** comprobación BCrypt y carga de datos desde Room en la cola de base de datos, no en el hilo gráfico.
- **Room:** se ha eliminado el acceso permitido al hilo principal. La clase Actor hereda su clave primaria de DomainEntity y se evita declararla dos veces.
- **Votos únicos:** restricción de unicidad por espectador, concursante y gala, con inserción \`IGNORE\` para informar si ya se votó.
- **Ventana de votación:** reglas centralizadas para permitir votar desde el inicio de la gala hasta antes de que transcurran 24 horas.
- **Solicitud de participación:** solo los espectadores pueden enviar solicitudes, una por edición. El cupo y las transiciones se verifican dentro de una única transacción.
- **Aprobación:** al aceptar a un candidato su rol cambia a CONCURSANTE; si se completa el aforo, las solicitudes pendientes restantes pasan a RECHAZADA.
- **Cuentas de usuario:** el formulario público ya no permite registrarse como concursante o administrador.
- **Validaciones:** fechas y cupos positivos en las ediciones; control del usuario en formularios de administración.
- **Automatización:** GitHub Actions ejecuta pruebas unitarias y compilación \`assembleDebug\` sobre Android SDK 34 y Java 17.

## Migración a Room 8

La migración declarada \`MIGRATION_7_8\` limpia duplicados por agrupación y crea índices únicos en:
- \`actor(username)\`
- \`puntuacion(idEspectador,idGala,idConcursante)\`
- \`solicitud(idConcursante,idEdicion)\`

**Atención:** las puntuaciones/solicitudes duplicadas heredadas pierden las copias posteriores a la primera. El índice de username exige que no existan dos cuentas con el mismo identificador; si las hay, la actualización requerirá limpieza manual. La configuración antigua permite migración destructiva cuando no existe ruta desde versiones anteriores a 7, por lo que debe realizarse copia antes de actualizar.

## Limitaciones pendientes para una aplicación comercial

- La base de datos es local a cada dispositivo; no hay sincronización ni control de identidades entre móviles.
- Las pantallas ocultan acciones según el rol de sesión, pero no existe un servidor que impida a otros clientes manipular datos.
- No hay gestión remota de tokens, registro centralizado de auditoría ni protección contra manipulación de dispositivos.
- El flujo de primera cuenta administrativa requiere conservar la contraseña: no existe recuperación automática de contraseñas.
- La visualización web de GitHub Pages es independiente del APK. Permite probar escenarios ficticios en el navegador pero no escribe en Room.

Estas limitaciones son deliberadamente explícitas para no presentar un ejercicio académico como una solución de seguridad de producción.
