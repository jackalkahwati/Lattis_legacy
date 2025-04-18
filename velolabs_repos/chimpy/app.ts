import cors from 'cors';

app.use(cors({
  origin: 'https://yourdomain.com',
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));