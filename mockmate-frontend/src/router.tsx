import { createBrowserRouter } from 'react-router-dom';
import App from './App';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';

import DashboardPage from './pages/DashboardPage';
import SessionConfigPage from './pages/SessionConfigPage';
import InterviewRoomPage from './pages/InterviewRoomPage';
import ReportPage from './pages/ReportPage';
import HistoryPage from './pages/HistoryPage';
import ProfilePage from './pages/ProfilePage';

import { ProtectedRoute, AuthRedirect } from './components/auth/AuthGuards';

export const router = createBrowserRouter([
    { path: '/', element: <LandingPage /> },
    { path: '/login', element: <AuthRedirect><LoginPage /></AuthRedirect> },
    { path: '/register', element: <AuthRedirect><RegisterPage /></AuthRedirect> },
    {
        path: '/',
        element: <ProtectedRoute />,
        children: [
            {
                element: <App />,
                children: [
                    { path: 'dashboard', element: <DashboardPage /> },
                    { path: 'interview/new', element: <SessionConfigPage /> },
                    { path: 'interview/:id', element: <InterviewRoomPage /> },
                    { path: 'interview/:id/report', element: <ReportPage /> },
                    { path: 'history', element: <HistoryPage /> },
                    { path: 'profile', element: <ProfilePage /> },
                ],
            },
        ],
    },
]);
