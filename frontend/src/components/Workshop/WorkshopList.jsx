import React from 'react';
import { MapPin, Phone, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const WorkshopList = ({ workshops }) => {
  const navigate = useNavigate();

  if (!workshops || workshops.length === 0) {
    return (
      <div className="flex justify-center items-center h-full text-gray-500">
        Aucun atelier trouvé.
      </div>
    );
  }

  return (
    <div className="space-y-4 overflow-y-auto pr-2" style={{ maxHeight: '600px' }}>
      {workshops.map(workshop => (
        <div key={workshop.id} className="card p-4 hover:shadow-lg transition-shadow border-l-4 border-l-primary">
          <div className="flex justify-between items-start">
            <h3 className="text-xl font-bold text-accent">{workshop.nom}</h3>
            <div className="flex flex-col items-end gap-1">
              <span className="bg-secondary/10 text-secondary px-2 py-1 rounded text-xs font-semibold">
                {workshop.typeService}
              </span>
              {workshop.distance !== null && workshop.distance !== undefined && (
                <span className="text-xs text-gray-500 font-medium">
                  à {workshop.distance.toFixed(1)} km
                </span>
              )}
            </div>
          </div>
          
          <div className="mt-3 space-y-2 text-sm text-gray-600">
            <div className="flex items-start">
              <MapPin className="w-4 h-4 mr-2 text-primary mt-0.5 flex-shrink-0" />
              <span>{workshop.adresse}</span>
            </div>
            <div className="flex items-center">
              <Phone className="w-4 h-4 mr-2 text-primary flex-shrink-0" />
              <span>{workshop.telephone}</span>
            </div>
            <div className="flex items-center">
              <Clock className="w-4 h-4 mr-2 text-primary flex-shrink-0" />
              <span>{workshop.horaires}</span>
            </div>
          </div>
          
          <div className="mt-4 flex justify-between items-center">
            <span className="text-xs text-gray-500">
              {workshop.deplacementPossible ? 'Déplacement possible' : 'Sur place uniquement'}
            </span>
            <button 
              onClick={() => navigate(`/workshop/${workshop.id}`)}
              className="btn-primary py-1 px-3 text-sm"
            >
              Réserver
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

export default WorkshopList;
