import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Users, Briefcase, Settings, BarChart, CheckCircle, XCircle } from 'lucide-react';
import api from '../api/axios';

const AdminDashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [pendingAgents, setPendingAgents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // In a real app, we would fetch from /admin/stats and /admin/agents?status=pending
    const fetchData = async () => {
      try {
        // mock for UI
        setStats({
          usersCount: 145,
          companiesCount: 24,
          revenue: 1250000,
          completedOrders: 312
        });
        setPendingAgents([
          { id: 1, nom: 'Kamga', prenom: 'Paul', carteProId: 'PRO-2026-001', team: { nom: 'Equipe Alpha' } },
          { id: 2, nom: 'Ngo', prenom: 'Jeanne', carteProId: 'PRO-2026-002', team: { nom: 'Equipe Beta' } }
        ]);
      } catch (err) {
        console.error("Erreur de chargement", err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleValidateAgent = async (id, validate) => {
    try {
      // await api.post(`/admin/agents/${id}/validate`, { valid: validate });
      setPendingAgents(pendingAgents.filter(a => a.id !== id));
      alert(`Agent ${validate ? 'validé' : 'rejeté'} avec succès.`);
    } catch (err) {
      alert("Erreur lors de la validation.");
    }
  };

  if (loading) return <div className="p-12 text-center">Chargement de l'administration...</div>;

  return (
    <div className="bg-gray-50 min-h-screen py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-3xl font-bold text-accent mb-8">Administration Centrale</h1>
        
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center">
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mr-4">
              <Users className="w-6 h-6 text-blue-600" />
            </div>
            <div>
              <p className="text-gray-500 text-sm">Utilisateurs</p>
              <p className="text-2xl font-bold">{stats?.usersCount}</p>
            </div>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center">
            <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mr-4">
              <Briefcase className="w-6 h-6 text-purple-600" />
            </div>
            <div>
              <p className="text-gray-500 text-sm">Entreprises</p>
              <p className="text-2xl font-bold">{stats?.companiesCount}</p>
            </div>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center">
            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mr-4">
              <CheckCircle className="w-6 h-6 text-green-600" />
            </div>
            <div>
              <p className="text-gray-500 text-sm">Interventions terminées</p>
              <p className="text-2xl font-bold">{stats?.completedOrders}</p>
            </div>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center">
            <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center mr-4">
              <BarChart className="w-6 h-6 text-orange-600" />
            </div>
            <div>
              <p className="text-gray-500 text-sm">Chiffre d'affaires</p>
              <p className="text-2xl font-bold">{stats?.revenue.toLocaleString()} XAF</p>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Panel */}
          <div className="lg:col-span-2 space-y-8">
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
              <h2 className="text-xl font-bold text-secondary mb-4 flex items-center">
                <Users className="w-5 h-5 mr-2" />
                Validation des Agents (En attente)
              </h2>
              {pendingAgents.length === 0 ? (
                <p className="text-gray-500">Aucun agent en attente de validation.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nom complet</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Carte Pro</th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Équipe assignée</th>
                        <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {pendingAgents.map(agent => (
                        <tr key={agent.id}>
                          <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">{agent.nom} {agent.prenom}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-gray-500">{agent.carteProId}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-gray-500">{agent.team?.nom}</td>
                          <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                            <button onClick={() => handleValidateAgent(agent.id, true)} className="text-green-600 hover:text-green-900 mr-3">Valider</button>
                            <button onClick={() => handleValidateAgent(agent.id, false)} className="text-red-600 hover:text-red-900">Rejeter</button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>

          {/* Side Panel */}
          <div className="space-y-8">
            <div className="bg-secondary text-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-bold mb-4 flex items-center">
                <Settings className="w-5 h-5 mr-2" />
                Actions rapides
              </h2>
              <div className="space-y-2">
                <button className="w-full text-left px-4 py-2 bg-secondary-light hover:bg-opacity-80 rounded-lg transition-colors">
                  Gérer les types de services
                </button>
                <button className="w-full text-left px-4 py-2 bg-secondary-light hover:bg-opacity-80 rounded-lg transition-colors">
                  Paramétrer les frais d'annulation
                </button>
                <button className="w-full text-left px-4 py-2 bg-secondary-light hover:bg-opacity-80 rounded-lg transition-colors">
                  Lister toutes les entreprises
                </button>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default AdminDashboard;
