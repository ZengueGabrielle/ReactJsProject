import React, { useState, useEffect } from 'react';
import MapView from '../components/Map/MapView';
import WorkshopList from '../components/Workshop/WorkshopList';
import api from '../api/axios';
import { Search } from 'lucide-react';

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // Radius de la terre en km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
    Math.sin(dLon/2) * Math.sin(dLon/2); 
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
  return R * c; 
};

const Home = () => {
  const [workshops, setWorkshops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [serviceType, setServiceType] = useState('');
  const [userLocation, setUserLocation] = useState(null);

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
        },
        (error) => {
          console.error("Erreur de géolocalisation", error);
        }
      );
    }
  }, []);

  // Fetch workshops
  useEffect(() => {
    const fetchWorkshops = async () => {
      try {
        setLoading(true);
        const res = await api.get('/workshops');
        // Suppose the API returns a paginated structure: { content: [...] } or an array
        const data = Array.isArray(res.data) ? res.data : (res.data.content || []);
        
        // Always combine mock data with DB data to ensure map is populated with diverse workshops
        const mockWorkshops = [
            { id: 1, nom: 'Plomberie Express Douala', adresse: 'Akwa, Douala', latitude: 4.0435, longitude: 9.7025, telephone: '+237 600 00 00 01', typeService: 'Plomberie', deplacementPossible: true, horaires: '08:00 - 18:00' },
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
        
        // Combine by ensuring no duplicate IDs
        const dbWorkshops = Array.isArray(data) ? data : [];
        const combined = [...dbWorkshops];
        mockWorkshops.forEach(mock => {
          if (!combined.find(w => w.id === mock.id)) {
            combined.push(mock);
          }
        });
        setWorkshops(combined);
      } catch (err) {
        console.error('Failed to fetch workshops', err);
        // Fallback mock
        setWorkshops([
            { id: 1, nom: 'Plomberie Express Douala', adresse: 'Akwa, Douala', latitude: 4.0435, longitude: 9.7025, telephone: '+237 600 00 00 01', typeService: 'Plomberie', deplacementPossible: true, horaires: '08:00 - 18:00' },
            { id: 2, nom: 'Menuiserie Moderne', adresse: 'Bonanjo, Douala', latitude: 4.0321, longitude: 9.6912, telephone: '+237 600 00 00 02', typeService: 'Menuiserie', deplacementPossible: false, horaires: '09:00 - 17:00' },
            { id: 3, nom: 'Garage Auto Pro Yaoundé', adresse: 'Bastos, Yaoundé', latitude: 3.8820, longitude: 11.5034, telephone: '+237 600 00 00 03', typeService: 'Automobile', deplacementPossible: true, horaires: '07:30 - 18:30' },
            { id: 4, nom: 'Plomberie Capitale', adresse: 'Mvan, Yaoundé', latitude: 3.8166, longitude: 11.5166, telephone: '+237 600 00 00 04', typeService: 'Plomberie', deplacementPossible: true, horaires: '08:00 - 17:00' },
            { id: 5, nom: 'Menuiserie Fine Ydé', adresse: 'Biyem-Assi, Yaoundé', latitude: 3.8447, longitude: 11.4939, telephone: '+237 600 00 00 05', typeService: 'Menuiserie', deplacementPossible: true, horaires: '08:00 - 18:00' },
            { id: 6, nom: 'Auto Moto Bafoussam', adresse: 'Djeleng, Bafoussam', latitude: 5.4778, longitude: 10.4217, telephone: '+237 600 00 00 06', typeService: 'Automobile', deplacementPossible: true, horaires: '08:00 - 18:00' },
            { id: 7, nom: 'Menuiserie de l\'Ouest', adresse: 'Kamkop, Bafoussam', latitude: 5.4850, longitude: 10.4150, telephone: '+237 600 00 00 07', typeService: 'Menuiserie', deplacementPossible: false, horaires: '08:00 - 17:00' },
            { id: 8, nom: 'Sani-Garoua', adresse: 'Marouaré, Garoua', latitude: 9.3000, longitude: 13.3833, telephone: '+237 600 00 00 08', typeService: 'Plomberie', deplacementPossible: true, horaires: '07:00 - 15:00' },
            { id: 9, nom: 'Garage Central Bamenda', adresse: 'Commercial Avenue, Bamenda', latitude: 5.9580, longitude: 10.1519, telephone: '+237 600 00 00 09', typeService: 'Automobile', deplacementPossible: true, horaires: '08:00 - 17:30' },
            { id: 10, nom: 'Menuiserie Kribi', adresse: 'Dombe, Kribi', latitude: 2.9463, longitude: 9.9147, telephone: '+237 600 00 00 10', typeService: 'Menuiserie', deplacementPossible: true, horaires: '08:00 - 18:00' }
        ]);
      } finally {
        setLoading(false);
      }
    };
    fetchWorkshops();
  }, []);

  const filteredWorkshops = workshops.map(w => {
    let distance = null;
    if (userLocation && w.latitude && w.longitude) {
      distance = calculateDistance(userLocation.lat, userLocation.lng, w.latitude, w.longitude);
    }
    return { ...w, distance };
  }).filter(w => {
    return (
      (w.nom.toLowerCase().includes(searchQuery.toLowerCase()) || w.typeService.toLowerCase().includes(searchQuery.toLowerCase())) &&
      (serviceType === '' || w.typeService === serviceType)
    );
  }).sort((a, b) => {
    if (a.distance !== null && b.distance !== null) {
      return a.distance - b.distance;
    }
    return 0;
  });

  return (
    <div className="bg-gray-50 min-h-screen pb-12">
      {/* Hero Section */}
      <div className="bg-primary text-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h1 className="text-4xl font-bold mb-4">Trouvez un professionnel qualifié en un clic</h1>
          <p className="text-xl text-primary-light mb-8 max-w-2xl mx-auto">
            Plomberie, Menuiserie, Automobile, Bricolage, Électronique, IT... Nous avons l'expert qu'il vous faut.
          </p>
          
          {/* Search Bar */}
          <div className="max-w-3xl mx-auto bg-white p-2 rounded-full flex shadow-lg">
            <div className="flex-grow flex items-center pl-4 border-r border-gray-200">
              <Search className="w-5 h-5 text-gray-400 mr-2" />
              <input 
                type="text" 
                placeholder="Que cherchez-vous ?" 
                className="w-full py-2 outline-none text-gray-800"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="w-1/3 px-2">
              <select 
                className="w-full h-full bg-transparent outline-none text-gray-700"
                value={serviceType}
                onChange={(e) => setServiceType(e.target.value)}
              >
                <option value="">Tous les services</option>
                <option value="Plomberie">Plomberie</option>
                <option value="Menuiserie">Menuiserie</option>
                <option value="Automobile">Automobile</option>
                <option value="Bricolage">Bricolage</option>
                <option value="Électronique">Électronique</option>
                <option value="Informatique">Informatique / IT</option>
              </select>
            </div>
            <button className="btn-secondary rounded-full px-8 py-2">
              Rechercher
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-[-2rem]">
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
          <div className="lg:col-span-3 bg-white p-4 rounded-xl shadow-md border border-gray-100 z-10 h-[600px]">
            {loading ? (
              <div className="flex justify-center items-center h-full">Chargement...</div>
            ) : (
              <MapView workshops={filteredWorkshops} onWorkshopClick={(id) => window.location.href = `/workshop/${id}`} userLocation={userLocation} />
            )}
          </div>
          <div className="lg:col-span-2 bg-white p-4 rounded-xl shadow-md border border-gray-100 z-10 h-[600px]">
            <h2 className="text-lg font-bold text-secondary mb-4 border-b pb-2">Ateliers recommandés</h2>
            {loading ? (
              <div className="text-center py-8">Chargement...</div>
            ) : (
              <WorkshopList workshops={filteredWorkshops} />
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
