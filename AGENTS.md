# Appointy Med — instrucciones para agentes

Este repositorio pertenece a Appointy Med. Su infraestructura y arquitectura ya
están definidas. Antes de cambiar código, entiende la estructura existente y
extiende el patrón responsable de la funcionalidad; no rediseñes el sistema ni
añadas infraestructura sin una necesidad explícita.

## Lectura y alcance

- Lee este `AGENTS.md` completo antes de ejecutar una tarea y vuelve a
  consultarlo si el trabajo cambia de alcance.
- Inspecciona primero el código, las pruebas y la configuración relevantes.
- Conserva los cambios existentes del usuario y limita las modificaciones al
  objetivo solicitado.

## Skills del proyecto

- Usa `caveman` para mantener respuestas concisas cuando la configuración de la
  sesión lo indique. No sacrifiques precisión técnica, validaciones ni
  información necesaria para operar Appointy Med.
- Usa `graph` automáticamente en tareas complejas que se beneficien de
  descomposición, ejecución paralela, dependencias, caché, reintentos
  selectivos o quality gates; por ejemplo, cambios multiarchivo,
  arquitectura, migraciones, refactors amplios o depuración con varios frentes.
- No uses `graph` para preguntas, diagnósticos simples o cambios pequeños de un
  solo archivo cuando su orquestación no aporte valor.
- Antes de usar una skill, lee sus instrucciones `SKILL.md` completas y
  síguelas. Si una skill no aplica, continúa con el flujo normal del proyecto.

## Forma de trabajo

- Traza primero el comportamiento y los invariantes; después edita.
- Reutiliza las abstracciones, capas y dependencias ya existentes.
- Implementa la solución más sencilla que complete el objetivo sin eliminar
  seguridad, autorización, validación, manejo de errores, compatibilidad ni
  pruebas necesarias.
- Ejecuta la verificación mínima suficiente para demostrar el cambio y reporta
  brevemente lo modificado, las pruebas realizadas y cualquier riesgo material.
