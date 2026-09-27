# ==============================================================================
# JobTrack Frontend — Dockerfile
# Production-ready Nginx serving Angular Standalone SPA
# ==============================================================================
FROM nginx:alpine

# Remove default Nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy built Angular artifacts to Nginx html directory
COPY dist/frontend/browser /usr/share/nginx/html

# Copy custom Nginx configuration (SPA routing + reverse proxy)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
