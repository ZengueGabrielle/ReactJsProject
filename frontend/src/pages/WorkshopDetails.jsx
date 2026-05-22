import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MapPin, Phone, Clock, ShieldCheck, ChevronRight } from 'lucide-react';
import api from '../api/axios';

const WorkshopDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [workshop, setWorkshop] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchWorkshopDetails = async () => {
      try {
        setLoading(true);
        const res = await api.get(`/workshops/${id}`);
        if (res.data && res.data.nom) {
          setWorkshop(res.data);
          return;
        }
        throw new Error("Invalid workshop data");
      } catch (err) {
        console.error('Failed to fetch workshop details', err);
        // Fallback mock array from Home
        const mockWorkshops = [
            { id: 1, nom: 'Plomberie Express Douala', adresse: 'Akwa, Douala', latitude: 4.0435, longitude: 9.7025, telephone: '+237 600 00 00 01', typeService: 'Plomberie', deplacementPossible: true, horaires: '08:00 - 18:00', entreprise: { nom: 'Express Services CM' } },
            { id: 2, nom: 'Menuiserie Moderne', adresse: 'Bonanjo, Douala', latitude: 4.0321, longitude: 9.6912, telephone: '+237 600 00 00 02', typeService: 'Menuiserie', deplacementPossible: false, horaires: '09:00 - 17:00' },
            { id: 3, nom: 'Garage Auto Pro Yaoundé', adresse: 'Bastos, Yaoundé', latitude: 3.8820, longitude: 11.5034, telephone: '+237 600 00 00 03', typeService: 'Automobile', deplacementPossible: true, horaires: '07:30 - 18:30' },
            { id: 4, nom: 'Plomberie Capitale', adresse: 'Mvan, Yaoundé', latitude: 3.8166, longitude: 11.5166, telephone: '+237 600 00 00 04', typeService: 'Plomberie', deplacementPossible: true, horaires: '08:00 - 17:00' },
            { id: 5, nom: 'Menuiserie Fine Ydé', adresse: 'Biyem-Assi, Yaoundé', latitude: 3.8447, longitude: 11.4939, telephone: '+237 600 00 00 05', typeService: 'Menuiserie', deplacementPossible: true, horaires: '08:00 - 18:00' },
            { id: 6, nom: 'Auto Moto Bafoussam', adresse: 'Djeleng, Bafoussam', latitude: 5.4778, longitude: 10.4217, telephone: '+237 600 00 00 06', typeService: 'Automobile', deplacementPossible: true, horaires: '08:00 - 18:00' },
            { id: 7, nom: 'Menuiserie de l\'Ouest', adresse: 'Kamkop, Bafoussam', latitude: 5.4850, longitude: 10.4150, telephone: '+237 600 00 00 07', typeService: 'Menuiserie', deplacementPossible: false, horaires: '08:00 - 17:00' },
            { id: 8, nom: 'Sani-Garoua', adresse: 'Marouaré, Garoua', latitude: 9.3000, longitude: 13.3833, telephone: '+237 600 00 00 08', typeService: 'Plomberie', deplacementPossible: true, horaires: '07:00 - 15:00' },
            { id: 9, nom: 'Garage Central Bamenda', adresse: 'Commercial Avenue, Bamenda', latitude: 5.9580, longitude: 10.1519, telephone: '+237 600 00 00 09', typeService: 'Automobile', deplacementPossible: true, horaires: '08:00 - 17:30' },
            { id: 10, nom: 'Menuiserie Kribi', adresse: 'Dombe, Kribi', latitude: 2.9463, longitude: 9.9147, telephone: '+237 600 00 00 10', typeService: 'Menuiserie', deplacementPossible: true, horaires: '08:00 - 18:00' },
            { id: 11, nom: 'Électro Dépannage', adresse: 'Cité des Palmiers, Douala', latitude: 4.0583, longitude: 9.7547, telephone: '+237 600 00 00 11', typeService: 'Électronique', deplacementPossible: false, horaires: '08:30 - 17:30' },
            { id: 12, nom: 'PC & Mac Solutions', adresse: 'Ngoa-Ekélé, Yaoundé', latitude: 3.8612, longitude: 11.5002, telephone: '+237 600 00 00 12', typeService: 'Électronique', deplacementPossible: true, horaires: '09:00 - 18:00' },
            { id: 13, nom: 'Brico-Maison Pro', adresse: 'Essos, Yaoundé', latitude: 3.8765, longitude: 11.5342, telephone: '+237 600 00 00 13', typeService: 'Bricolage', deplacementPossible: true, horaires: '07:00 - 19:00' },
            { id: 14, nom: 'Le Pro du Bricolage', adresse: 'Deido, Douala', latitude: 4.0621, longitude: 9.7154, telephone: '+237 600 00 00 14', typeService: 'Bricolage', deplacementPossible: true, horaires: '08:00 - 17:00' }
        ];
        const found = mockWorkshops.find(w => w.id === parseInt(id));
        setWorkshop(found || {
          id: parseInt(id),
          nom: 'Atelier Indépendant',
          adresse: 'Cameroun',
          telephone: '+237 600 00 00 00',
          typeService: 'Général',
          deplacementPossible: true,
          horaires: '08:00 - 18:00'
        });
      } finally {
        setLoading(false);
      }
    };
    fetchWorkshopDetails();
  }, [id]);

  if (loading) return <div className="flex justify-center items-center h-64">Chargement des détails...</div>;
  if (!workshop || !workshop.nom) return <div className="text-center py-12 text-gray-500">Atelier introuvable.</div>;

  return (
    <div className="bg-gray-50 min-h-screen py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="bg-white rounded-xl shadow-md overflow-hidden border border-gray-100 mb-6">
          <div className="bg-primary h-32"></div>
          <div className="px-6 py-6 relative">
            <div className="absolute -top-12 left-6 bg-white p-2 rounded-xl shadow-lg border border-gray-100">
              <div className="w-20 h-20 bg-secondary/10 flex items-center justify-center rounded-lg">
                <span className="text-secondary font-bold text-2xl">{workshop.nom.charAt(0)}</span>
              </div>
            </div>
            
            <div className="mt-10 flex justify-between items-start">
              <div>
                <h1 className="text-3xl font-bold text-accent">{workshop.nom}</h1>
                <p className="text-gray-500 mt-1 flex items-center">
                  <ShieldCheck className="w-4 h-4 text-green-500 mr-1" />
                  Géré par {workshop.entreprise?.nom || 'Entreprise Indépendante'}
                </p>
              </div>
              <span className="bg-secondary/10 text-secondary px-4 py-2 rounded-full font-semibold">
                {workshop.typeService}
              </span>
            </div>
          </div>
        </div>

        {/* Info Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100 space-y-4">
            <h3 className="text-lg font-bold text-secondary border-b pb-2">Coordonnées</h3>
            <div className="flex items-start">
              <MapPin className="w-5 h-5 text-primary mr-3 mt-0.5" />
              <div>
                <p className="font-medium text-gray-800">Adresse</p>
                <p className="text-gray-600">{workshop.adresse}</p>
              </div>
            </div>
            <div className="flex items-start">
              <Phone className="w-5 h-5 text-primary mr-3 mt-0.5" />
              <div>
                <p className="font-medium text-gray-800">Téléphone</p>
                <p className="text-gray-600">{workshop.telephone}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100 space-y-4">
            <h3 className="text-lg font-bold text-secondary border-b pb-2">Informations pratiques</h3>
            <div className="flex items-start">
              <Clock className="w-5 h-5 text-primary mr-3 mt-0.5" />
              <div>
                <p className="font-medium text-gray-800">Horaires d'ouverture</p>
                <p className="text-gray-600">{workshop.horaires}</p>
              </div>
            </div>
            <div className="mt-4 p-3 bg-blue-50 rounded-lg border border-blue-100">
              <p className="text-sm text-blue-800 flex items-center">
                <ShieldCheck className="w-4 h-4 mr-2" />
                {workshop.deplacementPossible 
                  ? "Cet atelier propose des interventions à domicile." 
                  : "Cet atelier nécessite que vous vous déplaciez sur place."}
              </p>
            </div>
          </div>
        </div>

        {/* Action Call */}
        <div className="bg-white rounded-xl shadow-lg p-8 border border-gray-100 text-center">
          <h2 className="text-2xl font-bold text-accent mb-4">Prêt à confier vos travaux ?</h2>
          <p className="text-gray-600 mb-6 max-w-2xl mx-auto">
            Sélectionnez les détails de votre besoin et nous nous chargeons du reste.
          </p>
          <button 
            onClick={() => navigate(`/order/${workshop.id}`)}
            className="btn-primary text-lg px-8 py-3 rounded-full flex items-center mx-auto"
          >
            Commander un service <ChevronRight className="ml-2 w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default WorkshopDetails;
