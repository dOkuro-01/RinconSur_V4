# Qué se conservó de V2 y V3 para crear V4

| Aspecto | Origen elegido | V4 |
| --- | --- | --- |
| UI, Material 3 y navegación con animaciones | V3 | Conservados |
| ViewModels separados y validaciones | V3 | Conservados |
| Login trabajador demo en Room/SQLite | V3 | Conservado (no apto para producción) |
| Cabinas, fila, asignación automática, alertas, reportes y colación | V3 | Conservados |
| Historial QR con CRUD, búsqueda y migraciones | Ambas | Conservados |
| QR real de la cámara | V2 tenía lectura real, pero con Google Code Scanner | Reemplazado por **CameraX Preview + ImageAnalysis + ZXing Core**. No abre interfaz externa |
| Ubicación aproximada opcional, guardada en SQLite | V2 | Restaurada usando **LocationManager nativo** en lugar de Play Services |
| Visualización de coordenadas e historial | V2 | Restaurada sobre la UI de V3 |
| Foto de QR y selector manual de sucursal | V3 | **Eliminados**: no equivalían a lectura real |
| Estructura de proyecto limpia, `.gitignore` y `README` | Ambas | Consolidada para GitHub |
| QR impresos para pruebas | V2 | Conservados y ampliados con pruebas V4 |

## Limitaciones pendientes de comprobar

- Compilar y ejecutar el código en Android Studio con SDK 35 y JDK 17.
- Probar lectura QR física en Samsung S25 (permiso, orientación y iluminación).
- Probar ambos caminos de ubicación: autorizada y denegada.
- Comprobar persistencia SQLite después de cerrar y reabrir la app.
- Aportar historial real de commits y planificación Trello; estos no se pueden generar automáticamente de forma honesta desde ZIP.
