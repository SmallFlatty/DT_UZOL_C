# ---- frontend (Vue 3 -> nginx) ----
FROM --platform=$BUILDPLATFORM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# prazdna URL = same-origin: nginx preposiela /api na backend v tom istom stacku
RUN rm -f .env
ENV VITE_API_URL=""
RUN npx vite build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -qO- http://127.0.0.1/nginx-health || exit 1
