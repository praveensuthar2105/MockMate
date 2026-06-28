# MockMate

MockMate is an AI-powered mock interview platform I built to help developers (and myself) prepare for the grind of technical and behavioral interviews. It uses Google's Gemini API to simulate a real interviewer that actually reads your resume and asks follow-up questions based on your specific experience.

The goal was to create something that feels more interactive than just a list of static questions.

## What's inside?

- **Real-time AI Interviews**: Uses WebSockets so the conversation feels fluid. The AI isn't just a chatbot; it's a "MockMate" that knows your background.
- **Resume Parsing**: You upload your resume (PDF), and the backend (Apache PDFBox) extracts your skills and projects so the AI can grill you on them.
- **Integrated Code Editor**: There's a spot to write code during the interview, and the AI gives you feedback on how you did—logic, efficiency, the whole bit.
- **Dashboard & Reports**: After each session, you get a breakdown of what went well and what didn't, so you know exactly what to study next.

## Tech Stack

I went with a Spring Boot backend and a React frontend because they're reliable and I wanted to get the project up and running quickly.

- **Backend**: Java 21, Spring Boot 3.3.5, Spring Security (JWT), PostgreSQL, WebSockets.
- **AI**: LangChain4j for the Gemini 2.0 Flash integration.
- **Frontend**: React 18, TypeScript, Tailwind CSS, and Vite.

## Project Structure

It's a simple monorepo setup:
- `/mockmate-backend`: The Spring Boot app.
- `/mockmate-frontend`: The React app.
- `/uploads`: Where the resumes live temporarily.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the target real-time voice interview
architecture and migration plan.

## How to run it locally

### Prerequisites
You'll need Java 21, Node.js, and a PostgreSQL instance running. Also, grab a Gemini API key from Google AI Studio.

### Setup
1. **Clone the repo**
   ```bash
   git clone https://github.com/praveensuthar2105/MockMate.git
   cd MockMate
   ```

2. **Env Variables**
   Create a `.env` file in the root. You can use `.env.example` as a template:
   ```env
   DB_PASSWORD=your_password
   JWT_SECRET=your_secret
   GEMINI_API_KEY=your_key
   UPLOAD_DIR=./uploads
   CORS_ORIGINS=http://localhost:5173
   ```

3. **Backend**
   ```bash
   cd mockmate-backend
   ./mvnw spring-boot:run
   ```

4. **Frontend**
   ```bash
   cd ../mockmate-frontend
   npm install
   npm run dev
   ```

## Key API Endpoints
- `POST /api/auth/login`: Get your JWT.
- `POST /api/resume/upload`: Send your resume over.
- `POST /api/sessions/create`: Start a new interview.
- `WS /ws-interview`: The WebSocket connection for the actual chat.

## License
MIT. Use it, fork it, whatever helps you get hired!

---
*Created by [Praveen Suthar](https://github.com/praveensuthar2105)*
