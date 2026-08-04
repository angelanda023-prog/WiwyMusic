# Estado de seguridad de las aplicaciones

Fecha: 4 de agosto de 2026

## Objetivo

Conservar lo aprendido durante el análisis de Movie y usarlo como referencia para
implementar protección contra APK modificadas en aplicaciones propias de películas
y música.

## Resultado del análisis de Movie

- Aplicación: Movie! Plus
- Paquete: `com.dvex.movp`
- Versión comprobada: `16.9` (`versionCode 90315`)
- La APK oficial utiliza una firma de desarrollador y servicios remotos que esperan
  esa firma.
- Al descompilar, modificar y volver a firmar la APK, cambia su certificado.
- Los servicios de Google/Firebase rechazaron la copia modificada porque su firma no
  estaba autorizada.
- La aplicación mostró un aviso de seguridad y dejó de permitir el flujo normal.
- Esto demuestra que quitar comprobaciones locales no basta cuando la autorización
  también se valida en el servidor.

## Estado de los dispositivos

- Pixel: se eliminó la copia modificada y se restauró la APK oficial Movie 16.9.
- La APK oficial arrancó correctamente y no registró el rechazo de firma observado
  en la copia modificada.
- Smart TV: conserva la instalación original; no fue reemplazada.
- No se configuró DNS privado, VPN, root ni cambios permanentes del sistema.

## Archivos relevantes del análisis

- APK original recibida: `Movie`
- Copia original con extensión válida:
  `build/Movie-16.9-original.apk`
- Proyecto descompilado de análisis: `Movie-apktool/`
- Las compilaciones modificadas de `build/` no deben instalarse: llevan una firma
  diferente y pueden activar nuevamente el rechazo de seguridad.

## Diseño recomendado para aplicaciones propias

La protección robusta debe implementarse en la aplicación y en el servidor.

1. Firmar las versiones de producción con una clave controlada y protegida.
2. Mantener en el servidor el nombre de paquete y las huellas permitidas de los
   certificados de producción.
3. Usar Play Integrity para obtener una prueba de integridad vinculada a una acción,
   usuario y nonce de un solo uso.
4. Verificar esa prueba únicamente en el servidor antes de entregar sesiones,
   catálogos privados, enlaces de reproducción o contenido premium.
5. Si se usa Firebase, añadir App Check y autorizar solamente las aplicaciones
   oficiales.
6. Emitir tokens de reproducción de corta duración, ligados al usuario y al recurso.
7. Aplicar límites de solicitudes, detección de repetición y registro de eventos.
8. Ofuscar el código de producción y evitar guardar secretos permanentes dentro de
   la APK. La ofuscación es una capa adicional, no la defensa principal.
9. Ante una validación fallida, bloquear solo la sesión o funciones protegidas de la
   aplicación. Nunca intentar bloquear el teléfono.
10. Mostrar un código de error claro y ofrecer recuperación para falsos positivos.

## Política de bloqueo sugerida

- Un fallo aislado: rechazar la operación y solicitar una validación nueva.
- Fallos repetidos: cerrar la sesión y aplicar una espera temporal.
- Evidencia consistente de APK alterada: denegar contenido protegido y registrar el
  certificado presentado.
- No crear bloqueos permanentes automáticos basados en una sola señal.
- Permitir desbloqueo administrativo y conservar un historial de auditoría.

## Requisitos para implementarlo en otro proyecto

- Código fuente Android de las aplicaciones.
- Tecnología utilizada: Kotlin, Java, Flutter, React Native u otra.
- Acceso al backend que autoriza usuarios y entrega contenido.
- Proyecto Firebase, si existe.
- Configuración de firma de producción sin copiar claves privadas al repositorio.
- Nombre de paquete de cada aplicación y lista de entornos: desarrollo, pruebas y
  producción.

## Próximo paso al cambiar de proyecto

Abrir este archivo en el proyecto nuevo y comenzar con una revisión de:

1. Flujo de inicio de sesión.
2. API que entrega enlaces o permisos de reproducción.
3. Configuración de firma.
4. Integración actual de Firebase.
5. Punto del backend donde se verificará la integridad antes de emitir tokens.

No implementar la protección únicamente como una comparación dentro de la APK:
esa comprobación puede eliminarse al descompilar. La decisión final siempre debe
estar respaldada por el servidor.
