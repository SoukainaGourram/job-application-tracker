# ==============================================================================
# JobTrack Frontend — Dockerfile
# Expects the Angular app to be pre-built locally with: npm run build
# The dist/ folder is then served by Nginx Alpine.
#
# Why not multi-stage? Docker Desktop on this machine has no internet access,
# so npm ci cannot download packages inside the build container.
# Build locally first: npm run build
# Then: docker compose build
# ==============================================================================
FROM nginx:alpine

# Remove default Nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy pre-built Angular artifacts to Nginx html directory
COPY dist/frontend/browser /usr/share/nginx/html

# Copy custom Nginx configuration (SPA routing + reverse proxy to backend)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
