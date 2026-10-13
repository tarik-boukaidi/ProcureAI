import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // En dev, /api part vers le gateway (dans Docker, c'est Nginx qui fait ce travail)
    proxy: { "/api": "http://localhost:8080" },
  },
});
