import React from 'react';
import { Outlet, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Wrench, User, LogOut, LayoutDashboard } from 'lucide-react';
import Chatbot from '../Chatbot/Chatbot';

const MainLayout = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white shadow-sm border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <Link to="/" className="flex items-center text-primary font-bold text-xl">
                <Wrench className="w-6 h-6 mr-2" />
                Hephaistos
              </Link>
            </div>
            <div className="flex items-center space-x-4">
              <Link to="/" className="text-gray-600 hover:text-primary transition-colors">
                Accueil
              </Link>
              {user ? (
                <>
                  <Link to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} className="text-gray-600 hover:text-primary transition-colors flex items-center">
                    <LayoutDashboard className="w-4 h-4 mr-1" />
                    Tableau de bord
                  </Link>
                  <button onClick={logout} className="text-gray-600 hover:text-primary transition-colors flex items-center">
                    <LogOut className="w-4 h-4 mr-1" />
                    Déconnexion
                  </button>
                </>
              ) : (
                <Link to="/login" className="btn-primary flex items-center">
                  <User className="w-4 h-4 mr-2" />
                  Connexion
                </Link>
              )}
            </div>
          </div>
        </div>
      </header>
      
      <main className="flex-grow">
        <Outlet />
      </main>

      <Chatbot />

      <footer className="bg-secondary text-white py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <p>&copy; 2026 Hephaistos Maintenance. Tous droits réservés.</p>
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;
