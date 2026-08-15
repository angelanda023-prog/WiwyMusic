# Sitio público WiwyMusic

Landing estática para presentar el servicio y descargar las aplicaciones oficiales.

## Vista local

```bash
python3 -m http.server 4173 --directory website
```

Abrir `http://127.0.0.1:4173`.

El enlace principal usa el endpoint estable de Android. macOS y Windows conservan canales separados.

## Medios cinematográficos

La página usa cinco imágenes originales generadas para WiwyMusic y cinco loops MP4 derivados de
ellas. Cada video dura 6 segundos, mide 1280 × 720 y tiene su PNG como `poster` de respaldo.

El icono original de la APK (`assets/wiwymusic-icon.png`) se usa como favicon, icono para iOS y
marca en navegación y pie de página.

Para regenerar un loop en macOS:

```bash
swiftc website/tools/make_loop.swift -o /tmp/wiwy-make-loop
/tmp/wiwy-make-loop website/assets/hero-cinematic.png website/assets/hero-cinematic.mp4
```
