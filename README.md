# DynamicMenus

Plugin para **Paper 1.21.4** que permite crear menús de inventario dinámicos desde archivos YAML.

## Características

- Carga de menús desde `plugins/DynamicMenus/menus/*.yml`.
- Placeholders básicos (`%player%`, `%online%`, `%world%`, `%uuid%`, `%displayname%`).
- Acciones por click:
  - `MESSAGE:<texto>`
  - `CONSOLE:<comando>`
  - `PLAYER:<comando>`
  - `OPEN:<menuId>`
  - `CLOSE`
- Soporte para item filler en slots vacíos.
- Recarga en caliente (`/dynamicmenus reload`).

## Comandos

- `/dynamicmenus open <menuId> [jugador]`
- `/dynamicmenus reload`
- `/dynamicmenus list`

## Permisos

- `dynamicmenus.open`
- `dynamicmenus.open.others`
- `dynamicmenus.admin`

## Compilación

```bash
mvn package
```

El `.jar` se genera en `target/`.
