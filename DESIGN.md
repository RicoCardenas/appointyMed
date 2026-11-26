# Design

Superficie: **Operate** (app para completar tareas). Dirección: clínica serena y precisa, restringida. El acento y la forma sirven al estado; la marca vive en detalles medidos, no en decoración.

## Color (OKLCH, restringido)
- Fondo `--bg` oklch(0.985 0.004 95); panel `--panel` blanco; segunda capa neutra más fría para barra y lateral `--rail` oklch(0.965 0.01 195).
- Tinta `--ink` oklch(0.24 0.02 220); secundaria `--ink-2` oklch(0.46 0.02 220); líneas `--line` oklch(0.9 0.008 210).
- Acento (teal clínico, solo acciones primarias, selección y foco): `--accent` oklch(0.5 0.1 195), hover `--accent-strong` oklch(0.43 0.095 195), suave `--accent-soft` oklch(0.95 0.03 195).
- Semánticos: éxito oklch(0.5 0.12 150), aviso oklch(0.58 0.12 75), peligro oklch(0.52 0.17 25), cada uno con fondo suave y texto de la misma matiz.
- Modo oscuro con los mismos roles (escena de uso: administrativos de día, pacientes de noche en el celular). Cada token se define una sola vez con `light-dark()`; `color-scheme` decide el par. Por defecto sigue al sistema y el botón de la barra recorre automático, claro y oscuro (`data-tema` en `<html>`; la elección se guarda solo en `localStorage`).

## Tipografía
Una familia: **Figtree** variable (humanista, amable, legible a tamaños pequeños), autoalojada con `@fontsource-variable/figtree`. Escala rem fija con razón 1.2: 0.875 (secundario), 0.9375 (cuerpo en tablas), 1 (cuerpo), 1.125, 1.375, 1.75. Cifras tabulares en fechas, horas y tablas. Interlineado inverso al tamaño. Medida de prosa 65ch.

## Espacio y forma
Base de 4 px; separación generosa entre grupos y apretada dentro. Radio 8 px en controles, 12 px en paneles. Sombras con desplazamiento y desenfoque suaves; sin halos de color, sin sombras duras. Sin tarjetas anidadas: las listas son filas, no mosaicos de iconos.

## Componentes (vocabulario único)
Botón (primario, secundario, fantasma, peligro) con estados reposo, hover, foco, activo, deshabilitado y cargando. Campo con etiqueta visible, ayuda y error en línea. Tabla densa con cabecera fija y paginador. Insignia de estado con texto. Aviso en línea (info, éxito, aviso, error). Diálogo nativo `<dialog>` solo para confirmaciones destructivas. Esqueletos en carga; vacíos que enseñan el siguiente paso.

## Movimiento
150 a 200 ms con ease-out exponencial; solo para estado (hover, selección, aparición de resultados, confirmación). Retroalimentación al presionar (escala 0.98) en franjas y botones. Sin coreografía de carga. `prefers-reduced-motion`: sin desplazamientos ni giros; el estado se sigue viendo con fundidos y color, y el indicador de carga late en opacidad (guía `apple-design`).

## Navegación y respuesta
Barra superior con marca, rol y cierre de sesión; lateral por rol. En móvil el lateral pasa a barra inferior de 44 px mínimo por objetivo. Las tablas se vuelven listas apiladas bajo 720 px.

## Navegador
Selección de texto, caret, barras de desplazamiento, anillos de foco y numerales se tematizan con la paleta (`accent-color`, `::selection`, `scrollbar-color`).
