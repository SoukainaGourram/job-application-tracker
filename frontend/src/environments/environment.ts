// Production environment (Docker / deployed)
// The browser uses relative URLs so Nginx can reverse-proxy to the backend container.
export const environment = {
  production: true,
  apiUrl: '/api',
};
