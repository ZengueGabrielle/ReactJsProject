import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import MainLayout from './components/Layout/MainLayout';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import WorkshopDetails from './pages/WorkshopDetails';
import OrderWizard from './pages/OrderWizard';
import UserDashboard from './pages/UserDashboard';
import AdminDashboard from './pages/AdminDashboard';

const ProtectedRoute = ({ children, roles }) => {
  const { user, loading } = useAuth();
  if (loading) return <div>Chargement...</div>;
  if (!user) return <Navigate to="/login" />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/" />;
  return children;
};

const App = () => {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<MainLayout />}>
            <Route index element={<Home />} />
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
            <Route path="workshop/:id" element={<WorkshopDetails />} />
            
            {/* Protected Routes */}
            <Route path="order/:workshopId" element={
              <ProtectedRoute roles={['USER', 'COMPANY_MANAGER']}>
                <OrderWizard />
              </ProtectedRoute>
            } />
            <Route path="dashboard" element={
              <ProtectedRoute roles={['USER', 'COMPANY_MANAGER']}>
                <UserDashboard />
              </ProtectedRoute>
            } />
            
            {/* Admin Routes */}
            <Route path="admin" element={
              <ProtectedRoute roles={['ADMIN']}>
                <AdminDashboard />
              </ProtectedRoute>
            } />
          </Route>
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;
