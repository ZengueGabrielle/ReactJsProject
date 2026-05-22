import React from 'react';
import { GoogleMap, useJsApiLoader, Marker, InfoWindow } from '@react-google-maps/api';

const containerStyle = {
  width: '100%',
  height: '100%',
  minHeight: '500px',
  borderRadius: '0.75rem'
};

const defaultCenter = {
  lat: 4.0511, // Defaulting to Douala, Cameroon as example
  lng: 9.7679
};

const MapView = ({ workshops, onWorkshopClick, userLocation }) => {
  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY
  });

  const [selectedWorkshop, setSelectedWorkshop] = React.useState(null);

  if (!isLoaded) return <div className="flex justify-center items-center h-full bg-gray-50 rounded-xl">Chargement de la carte...</div>;

  const center = userLocation || defaultCenter;

  return (
    <GoogleMap
      mapContainerStyle={containerStyle}
      center={center}
      zoom={12}
      options={{
        styles: [
          {
            featureType: "poi",
            elementType: "labels",
            stylers: [{ visibility: "off" }]
          }
        ]
      }}
    >
      {workshops && workshops.map(workshop => (
        <Marker
          key={workshop.id}
          position={{ lat: workshop.latitude, lng: workshop.longitude }}
          onClick={() => setSelectedWorkshop(workshop)}
        />
      ))}

      {userLocation && (
        <Marker
          position={userLocation}
          icon={{
            url: "http://maps.google.com/mapfiles/ms/icons/blue-dot.png"
          }}
          title="Votre position"
        />
      )}

      {selectedWorkshop && (
        <InfoWindow
          position={{ lat: selectedWorkshop.latitude, lng: selectedWorkshop.longitude }}
          onCloseClick={() => setSelectedWorkshop(null)}
        >
          <div className="p-2 max-w-xs">
            <h3 className="font-bold text-lg text-primary">{selectedWorkshop.nom}</h3>
            <p className="text-sm text-gray-600 my-1">{selectedWorkshop.adresse}</p>
            <p className="text-sm font-medium">Services: {selectedWorkshop.typeService}</p>
            {selectedWorkshop.distance !== null && selectedWorkshop.distance !== undefined && (
              <p className="text-xs text-gray-500 font-semibold mt-1">À {selectedWorkshop.distance.toFixed(1)} km</p>
            )}
            <button 
              className="mt-3 btn-primary text-xs w-full"
              onClick={() => onWorkshopClick(selectedWorkshop.id)}
            >
              Voir détails
            </button>
          </div>
        </InfoWindow>
      )}
    </GoogleMap>
  );
};

export default React.memo(MapView);
