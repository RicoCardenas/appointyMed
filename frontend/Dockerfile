# Contexto de construccion: frontend/ (docker build frontend)
FROM node:22-alpine AS build
WORKDIR /app
# El lockfile es v6: corresponde a pnpm 8
RUN corepack enable && corepack prepare pnpm@8.15.9 --activate
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm run build

FROM nginx:1.27-alpine
# Plantilla: el contenedor sustituye las variables de entorno al arrancar (PORT y las URL de los servicios)
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
