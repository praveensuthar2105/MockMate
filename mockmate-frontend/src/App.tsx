import { Outlet } from 'react-router-dom';
import Navbar from './components/shared/Navbar';

function App() {
  return (
    <div className="min-h-screen bg-bg-page font-body text-text-primary text-[15px]">
      <Navbar />
      <main className="pt-16 min-h-screen relative">
        <div className="w-full max-w-7xl mx-auto p-6 md:p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
}

export default App;
