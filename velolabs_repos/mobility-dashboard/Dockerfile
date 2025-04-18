# Build stage
FROM node:18-alpine AS deps
WORKDIR /app

# Install dependencies only when needed
COPY package*.json ./
RUN npm ci

# Rebuild the source code only when needed
FROM node:18-alpine AS builder
WORKDIR /app

# Copy dependencies from deps stage
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# Set Next.js environment variables
ENV NEXT_TELEMETRY_DISABLED 1
ENV NODE_ENV production

# Run type checking and linting
RUN npm run type-check
RUN npm run lint

# Build the application
RUN npm run build

# Production stage
FROM node:18-alpine AS runner
WORKDIR /app

# Set production environment
ENV NODE_ENV production
ENV NEXT_TELEMETRY_DISABLED 1

# Create non-root user for security
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# Copy only necessary files from builder
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/next.config.js ./

# Set correct permissions
RUN chown -R nextjs:nodejs /app

# Switch to non-root user
USER nextjs

# Expose port
EXPOSE 3000

# Add health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD node -e "const http = require('http'); \
    const options = { host: 'localhost', port: 3000, path: '/api/health', timeout: 2000 }; \
    const request = http.request(options, (res) => { \
        console.log('STATUS:', res.statusCode); \
        process.exit(res.statusCode === 200 ? 0 : 1); \
    }); \
    request.on('error', function(err) { \
        console.log('ERROR:', err); \
        process.exit(1); \
    }); \
    request.end();"

# Start the application
CMD ["node", "server.js"]
