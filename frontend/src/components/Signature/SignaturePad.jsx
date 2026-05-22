import React, { useRef, useState, useEffect } from 'react';
import SignatureCanvas from 'signature_pad';
import { Download, Eraser, Check } from 'lucide-react';
import api from '../../api/axios';

const SignaturePad = ({ orderId, onSigned }) => {
  const canvasRef = useRef(null);
  const signaturePadRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (canvasRef.current && !signaturePadRef.current) {
      signaturePadRef.current = new SignatureCanvas(canvasRef.current, {
        penColor: 'rgb(26, 26, 26)', // accent black
        backgroundColor: 'rgb(255, 255, 255)'
      });
      
      // Resize canvas to fit container
      const ratio = Math.max(window.devicePixelRatio || 1, 1);
      const canvas = canvasRef.current;
      canvas.width = canvas.offsetWidth * ratio;
      canvas.height = canvas.offsetHeight * ratio;
      canvas.getContext("2d").scale(ratio, ratio);
      signaturePadRef.current.clear();
    }
  }, []);

  const handleClear = () => {
    signaturePadRef.current?.clear();
  };

  const handleSave = async () => {
    if (signaturePadRef.current?.isEmpty()) {
      setError("Veuillez signer avant de valider.");
      return;
    }

    setLoading(true);
    setError(null);
    try {
      // Get base64 PNG
      const dataUrl = signaturePadRef.current.toDataURL('image/png');
      const base64Data = dataUrl.split(',')[1];
      
      // Send to backend
      const response = await api.post(`/invoices/${orderId}/sign`, { signature: base64Data });
      
      if (onSigned) {
        onSigned(response.data.pdfUrl || response.data);
      }
    } catch (err) {
      setError("Erreur lors de la validation de la signature.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
      <h3 className="font-bold text-secondary mb-4">Signature Client</h3>
      <p className="text-sm text-gray-500 mb-4">
        En signant ci-dessous, vous validez la fin de l'intervention.
      </p>
      
      <div className="border-2 border-dashed border-gray-300 rounded-lg overflow-hidden bg-gray-50 mb-4">
        <canvas 
          ref={canvasRef} 
          className="w-full h-48 cursor-crosshair touch-none"
        ></canvas>
      </div>

      {error && <p className="text-red-500 text-sm mb-4">{error}</p>}

      <div className="flex justify-end space-x-3">
        <button 
          onClick={handleClear} 
          disabled={loading}
          className="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg flex items-center transition-colors"
        >
          <Eraser className="w-4 h-4 mr-2" />
          Effacer
        </button>
        <button 
          onClick={handleSave} 
          disabled={loading}
          className="px-4 py-2 bg-primary text-white hover:bg-primary-light rounded-lg flex items-center transition-colors"
        >
          {loading ? 'Traitement...' : (
            <>
              <Check className="w-4 h-4 mr-2" />
              Valider la signature
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default SignaturePad;
