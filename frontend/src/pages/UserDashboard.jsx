import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { Calendar, MapPin, DollarSign, Clock, Download, PenTool } from 'lucide-react';
import api from '../api/axios';
import SignaturePad from '../components/Signature/SignaturePad';

const UserDashboard = () => {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('orders');

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const res = await api.get('/orders/me');
        setOrders(res.data);
      } catch (err) {
        console.error('Error fetching orders:', err);
        // Mock data
        setOrders([
          {
            id: 1,
            workshop: { nom: 'Plomberie Express Douala' },
            date: '2026-05-25',
            heure: '10:00',
            mode: 'DEPLACEMENT',
            statut: 'TERMINEE',
            montantTotal: 15000,
            adresseDeplacement: 'Akwa, Rue des Manguier'
          },
          {
            id: 2,
            workshop: { nom: 'Menuiserie Moderne' },
            date: '2026-06-01',
            heure: '14:30',
            mode: 'SUR_PLACE',
            statut: 'CONFIRMEE',
            montantTotal: 45000,
            adresseDeplacement: null
          }
        ]);
      } finally {
        setLoading(false);
      }
    };
    fetchOrders();
  }, []);

  const getStatusColor = (statut) => {
    switch (statut) {
      case 'CONFIRMEE': return 'bg-blue-100 text-blue-800';
      case 'AGENT_EN_ROUTE': return 'bg-yellow-100 text-yellow-800';
      case 'TRAVAUX_EN_COURS': return 'bg-orange-100 text-orange-800';
      case 'TERMINEE': return 'bg-green-100 text-green-800';
      case 'FACTUREE': return 'bg-purple-100 text-purple-800';
      case 'ANNULEE': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const handleCancel = async (id) => {
    if (window.confirm("Êtes-vous sûr de vouloir annuler cette commande ? Des frais peuvent s'appliquer.")) {
      try {
        await api.delete(`/orders/${id}`);
        // Refresh orders...
        const updated = orders.map(o => o.id === id ? { ...o, statut: 'ANNULEE' } : o);
        setOrders(updated);
      } catch (err) {
        alert("Erreur lors de l'annulation.");
      }
    }
  };

  return (
    <div className="bg-gray-50 min-h-screen py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8 flex flex-col sm:flex-row items-center justify-between">
          <div className="flex items-center mb-4 sm:mb-0">
            <div className="w-16 h-16 bg-primary rounded-full flex items-center justify-center text-white text-2xl font-bold mr-4">
              {user?.nom?.charAt(0) || 'U'}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-accent">Bonjour, {user?.nom} {user?.prenom} !</h1>
              <p className="text-gray-500">Gérez vos commandes et interventions</p>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-gray-200 mb-6 overflow-x-auto">
          <button 
            className={`py-3 px-6 font-medium text-sm border-b-2 whitespace-nowrap transition-colors ${activeTab === 'orders' ? 'border-primary text-primary' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
            onClick={() => setActiveTab('orders')}
          >
            Mes Commandes
          </button>
          <button 
            className={`py-3 px-6 font-medium text-sm border-b-2 whitespace-nowrap transition-colors ${activeTab === 'profile' ? 'border-primary text-primary' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
            onClick={() => setActiveTab('profile')}
          >
            Mon Profil
          </button>
          <button 
            className={`py-3 px-6 font-medium text-sm border-b-2 whitespace-nowrap transition-colors ${activeTab === 'settings' ? 'border-primary text-primary' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
            onClick={() => setActiveTab('settings')}
          >
            Paramètres & Infos
          </button>
        </div>

        {/* Content */}
        {activeTab === 'profile' && (
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 max-w-2xl">
            <h2 className="text-xl font-bold text-accent mb-6">Informations personnelles</h2>
            <form className="space-y-6" onSubmit={(e) => { e.preventDefault(); alert('Profil mis à jour !'); }}>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Nom</label>
                  <input type="text" className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary" defaultValue={user?.nom} />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Prénom</label>
                  <input type="text" className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary" defaultValue={user?.prenom} />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input type="email" className="w-full px-4 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500" defaultValue={user?.email} disabled />
                <p className="text-xs text-gray-400 mt-1">L'email ne peut pas être modifié.</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Téléphone</label>
                <input type="tel" className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary" defaultValue={user?.telephone || ''} />
              </div>
              <div className="pt-4 border-t border-gray-100">
                <button type="submit" className="btn-primary py-2 px-6">
                  Enregistrer les modifications
                </button>
              </div>
            </form>
          </div>
        )}

        {activeTab === 'settings' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
              <h2 className="text-xl font-bold text-accent mb-6">Paramètres de l'application</h2>
              <form className="space-y-6" onSubmit={(e) => { e.preventDefault(); alert('Paramètres sauvegardés !'); }}>
                <div>
                  <label className="flex items-center space-x-3 mb-4">
                    <input type="checkbox" defaultChecked className="w-5 h-5 text-primary rounded focus:ring-primary" />
                    <span className="font-medium text-gray-700">Recevoir des notifications par email</span>
                  </label>
                  <label className="flex items-center space-x-3 mb-4">
                    <input type="checkbox" defaultChecked className="w-5 h-5 text-primary rounded focus:ring-primary" />
                    <span className="font-medium text-gray-700">Recevoir des SMS pour les urgences</span>
                  </label>
                  <label className="flex items-center space-x-3 mb-4">
                    <input type="checkbox" className="w-5 h-5 text-primary rounded focus:ring-primary" />
                    <span className="font-medium text-gray-700">Activer le mode sombre (Bientôt)</span>
                  </label>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Langue par défaut</label>
                  <select className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary">
                    <option value="fr">Français</option>
                    <option value="en">English</option>
                  </select>
                </div>
                <div className="pt-4 border-t border-gray-100">
                  <button type="submit" className="btn-primary py-2 px-6">
                    Sauvegarder les paramètres
                  </button>
                </div>
              </form>
            </div>
            
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 h-fit">
              <h2 className="text-xl font-bold text-accent mb-6">À propos de Hephaitos</h2>
              <div className="space-y-4 text-gray-600">
                <p><strong>Version de l'application :</strong> 1.0.0 (Bêta)</p>
                <p><strong>Conditions d'utilisation :</strong> <a href="#" className="text-primary hover:underline">Voir les CGU</a></p>
                <p><strong>Politique de confidentialité :</strong> <a href="#" className="text-primary hover:underline">Voir la politique</a></p>
                <div className="pt-4 mt-4 border-t border-gray-100">
                  <p className="text-sm">Hephaitos Maintenance est votre partenaire de confiance pour tous vos besoins en réparation et maintenance rapide à domicile ou en atelier.</p>
                  <p className="text-sm mt-2">© 2026 Hephaitos. Tous droits réservés.</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'orders' && (
          <div className="space-y-6">
            {loading ? (
              <div className="text-center py-12">Chargement de vos commandes...</div>
            ) : orders.length === 0 ? (
              <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center text-gray-500">
                Vous n'avez passé aucune commande.
              </div>
            ) : (
              orders.map(order => (
                <div key={order.id} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                  <div className="border-b border-gray-100 bg-gray-50 p-4 flex justify-between items-center">
                    <span className="font-bold text-gray-800">Commande #{order.id}</span>
                    <span className={`px-3 py-1 rounded-full text-xs font-bold ${getStatusColor(order.statut)}`}>
                      {order.statut.replace(/_/g, ' ')}
                    </span>
                  </div>
                  
                  <div className="p-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="space-y-3">
                        <div className="flex items-center text-gray-700">
                          <PenTool className="w-5 h-5 mr-3 text-primary" />
                          <span className="font-medium">{order.workshop?.nom}</span>
                        </div>
                        <div className="flex items-center text-gray-700">
                          <Calendar className="w-5 h-5 mr-3 text-primary" />
                          <span>{order.date} à {order.heure}</span>
                        </div>
                        <div className="flex items-center text-gray-700">
                          <MapPin className="w-5 h-5 mr-3 text-primary" />
                          <span>{order.mode === 'DEPLACEMENT' ? order.adresseDeplacement : 'Sur place (à l\'atelier)'}</span>
                        </div>
                        <div className="flex items-center text-gray-700">
                          <DollarSign className="w-5 h-5 mr-3 text-primary" />
                          <span className="font-bold">{order.montantTotal} FCFA</span>
                        </div>
                      </div>
                      
                      <div className="flex flex-col justify-center items-end space-y-3 border-t md:border-t-0 md:border-l border-gray-100 pt-4 md:pt-0 pl-0 md:pl-6">
                        {order.statut === 'CONFIRMEE' && (
                          <>
                            <button className="w-full btn-secondary py-2 flex justify-center text-sm">
                              Modifier (Max 1 fois)
                            </button>
                            <button onClick={() => handleCancel(order.id)} className="w-full bg-red-50 text-red-600 hover:bg-red-100 py-2 rounded-md font-medium text-sm transition-colors">
                              Annuler la commande
                            </button>
                          </>
                        )}
                        
                        {(order.statut === 'TERMINEE' || order.statut === 'FACTUREE') && (
                          <button className="w-full border border-primary text-primary hover:bg-primary-50 py-2 rounded-md font-medium text-sm flex items-center justify-center transition-colors">
                            <Download className="w-4 h-4 mr-2" />
                            Télécharger la facture
                          </button>
                        )}
                      </div>
                    </div>

                    {/* Signature required UI */}
                    {order.statut === 'TRAVAUX_EN_COURS' && (
                      <div className="mt-6 border-t border-gray-100 pt-6">
                        <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 p-4 rounded-lg mb-4">
                          L'intervention est en cours. Veuillez signer ci-dessous une fois les travaux terminés à votre satisfaction.
                        </div>
                        <SignaturePad orderId={order.id} onSigned={(url) => {
                          // Update UI upon signature success
                          alert("Signature enregistrée avec succès !");
                          setOrders(orders.map(o => o.id === order.id ? { ...o, statut: 'TERMINEE' } : o));
                        }} />
                      </div>
                    )}

                  </div>
                </div>
              ))
            )}
          </div>
        )}

      </div>
    </div>
  );
};

export default UserDashboard;
