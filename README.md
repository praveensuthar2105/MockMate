# MockMate

MockMate is a full-stack mock interview platform that helps you practice technical interviews in a more realistic way.

It combines resume-aware AI questions, real-time interview chat, a coding round with code execution, voice support, and a post-interview performance report — all in one workflow.

---

## Why MockMate?

Most interview prep tools feel disconnected: one tool for questions, another for coding, and something else for feedback.

MockMate keeps everything in one place so you can:

- upload your resume and get personalized questions
- go through multiple interview phases (resume, DSA, system design, HR)
- solve coding problems inside the interview experience
- use voice input/output during practice
- review score reports and past sessions

---

## Core Features

- **Authentication & profile** (register/login + protected routes)
- **Resume upload and AI-based resume parsing**
- **AI interviewer with session memory**
- **Real-time interview messaging via WebSocket/STOMP**
- **DSA round with Monaco editor + sandboxed code execution**
- **Voice support for interview interaction**
- **Session history, analytics, and downloadable report flow**

---

## Tech Stack

### Frontend (`mockmate-frontend`)

- React + TypeScript + Vite
- Zustand, Axios, React Router
- Monaco Editor
- STOMP + SockJS for real-time communication

### Backend (`mockmate-backend`)

- Spring Boot 3 (Java 21)
- Spring Security + JWT
- Spring WebSocket (STOMP)
- PostgreSQL + Spring Data JPA
- LangChain4j + Gemini integration
- Apache PDFBox (resume parsing)

---

## Project Structure

```text
MockMate/
├── mockmate-frontend/   # React app
├── mockmate-backend/    # Spring Boot API + WebSocket server
├── ARCHITECTURE.md      # Voice/realtime architecture notes
└── PROJECT_STATUS.md    # Current project progress snapshot
```

---

## Prerequisites

Before running locally, make sure you have:

- **Node.js 20+** and npm
- **Java 21**
- **PostgreSQL 14+**
- A **Gemini API key** (for AI-powered features)

---

## Local Setup

### 1) Backend setup

```bash
cd mockmate-backend
cp .env.example .env
# update .env values
./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080` by default.

Backend `.env` keys:

- `DB_PASSWORD`
- `JWT_SECRET`
- `GEMINI_API_KEY`
- `UPLOAD_DIR`
- `CORS_ORIGINS`

### 2) Frontend setup

```bash
cd mockmate-frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173` by default.

Optional frontend env values (create `.env` in `mockmate-frontend`):

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws/websocket
```

---

## Useful Commands

### Frontend

```bash
cd mockmate-frontend
npm run dev
npm run lint
npm run build
```

### Backend

```bash
cd mockmate-backend
./mvnw spring-boot:run
./mvnw test
```

---

## Interview Flow (High Level)

1. User logs in and uploads resume
2. Resume is parsed and stored as structured profile data
3. User starts a session with chosen difficulty/company context
4. AI runs phase-based interview rounds
5. Candidate solves DSA tasks in-editor
6. System records responses and generates score/report data

---

## Current Status

The project is feature-rich and actively evolving. Some areas (especially automated test coverage and deployment automation) are still in progress.

For detailed tracking, check:

- `PROJECT_STATUS.md`
- `ARCHITECTURE.md`

---

## Notes

- This project uses third-party AI/model providers, so make sure your API keys and quotas are set correctly.
- Do not commit secrets (`.env`, API keys, JWT secrets) into source control.
