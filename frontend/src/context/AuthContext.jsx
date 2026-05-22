import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check local storage for existing session
    const storedUser = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    // SIMULATION ADMIN POUR TEST
    if (email === 'admin@hephaistos.com' && password === 'admin') {
      const adminData = { token: 'mock-admin-token', refreshToken: 'mock-refresh', role: 'ADMIN', email: 'admin@hephaistos.com', nom: 'Admin', prenom: 'Principal' };
      localStorage.setItem('token', adminData.token);
      localStorage.setItem('user', JSON.stringify(adminData));
      setUser(adminData);
      return adminData;
    }

    const response = await api.post('/auth/login', { email, password });
    // Le backend renvoie { token, refreshToken, role, email }
    const { token, refreshToken, role, email: userEmail } = response.data;
    const userData = { token, refreshToken, role, email: userEmail };
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const value = {
    user,
    loading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{!loading && children}</AuthContext.Provider>;
};

export const useAuth = () => {
  return useContext(AuthContext);
};
