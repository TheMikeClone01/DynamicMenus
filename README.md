# Voxel Survival Infinito

Prototipo web inspirado en Minecraft con:

- Mundo de voxels generado proceduralmente por chunks alrededor del jugador.
- Sensación de mundo infinito: los chunks se crean y descargan dinámicamente.
- Mecánicas de supervivencia básicas: vida, hambre y ciclo día/noche.
- Interacción voxel: romper y colocar bloques en primera persona.

## Ejecutar

Como usa imports ES modules, sirve el proyecto con un servidor estático:

```bash
python3 -m http.server 4173
```

Luego abre `http://localhost:4173`.
