import React, { useState, useRef, useEffect } from 'react';
import { MessageCircle, X, Send, Bot, User, Image as ImageIcon } from 'lucide-react';
import api from '../../api/axios';
import { useAuth } from '../../context/AuthContext';

const Chatbot = () => {
  const { user } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  
  useEffect(() => {
    if (messages.length === 0) {
      const userName = user?.prenom || user?.nom;
      const greeting = userName 
        ? `Bonjour ${userName} ! Je suis l'assistant Hephaistos. En quoi puis-je vous aider aujourd'hui ? (Plomberie, Menuiserie, Automobile...)`
        : "Bonjour ! Je suis l'assistant Hephaistos. En quoi puis-je vous aider aujourd'hui ? (Plomberie, Menuiserie, Automobile...)";
      setMessages([{ text: greeting, sender: 'bot' }]);
    }
  }, [user]);
  const [input, setInput] = useState('');
  const [selectedImage, setSelectedImage] = useState(null);
  const fileInputRef = useRef(null);
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleImageSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setSelectedImage({
          preview: URL.createObjectURL(file),
          base64: reader.result.split(',')[1],
          mediaType: file.type
        });
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim() && !selectedImage) return;

    if (!user) {
      setMessages(prev => [...prev, { text: "Veuillez vous connecter ou vous inscrire pour discuter avec moi et obtenir des conseils personnalisés.", sender: 'bot' }]);
      setInput('');
      setSelectedImage(null);
      return;
    }

    const userText = input.trim();
    setMessages(prev => [...prev, { text: userText, image: selectedImage?.preview, sender: 'user' }]);
    
    const requestPayload = { 
      message: userText,
      imageBase64: selectedImage?.base64,
      imageMediaType: selectedImage?.mediaType
    };
    
    setInput('');
    setSelectedImage(null);
    setIsLoading(true);

    try {
      const response = await api.post('/chatbot/message', requestPayload);
      const botResponse = response.data.response || response.data.reponse || response.data; 
      setMessages(prev => [...prev, { text: botResponse, sender: 'bot' }]);
    } catch (error) {
      console.error("Chatbot backend error", error);
      const errorMsg = error.response?.data?.message || "Erreur serveur : Je n'ai pas pu joindre le service d'intelligence artificielle. Veuillez vérifier la clé d'API ou réessayer plus tard.";
      setMessages(prev => [...prev, { text: errorMsg, sender: 'bot' }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      {/* Floating Button */}
      {!isOpen && (
        <button 
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 bg-primary text-white p-4 rounded-full shadow-lg hover:bg-primary-light transition-transform hover:scale-105 z-50 flex items-center justify-center"
        >
          <MessageCircle className="w-6 h-6" />
        </button>
      )}

      {/* Chat Window */}
      {isOpen && (
        <div className="fixed bottom-6 right-6 w-80 sm:w-96 bg-white rounded-xl shadow-2xl border border-gray-200 z-50 flex flex-col overflow-hidden" style={{ height: '500px' }}>
          {/* Header */}
          <div className="bg-secondary text-white p-4 flex justify-between items-center">
            <div className="flex items-center">
              <Bot className="w-5 h-5 mr-2" />
              <h3 className="font-bold">Assistant Hephaistos</h3>
            </div>
            <button onClick={() => setIsOpen(false)} className="text-gray-300 hover:text-white">
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Messages Area */}
          <div className="flex-grow p-4 overflow-y-auto bg-gray-50 flex flex-col space-y-4">
            {messages.map((msg, index) => (
              <div key={index} className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[80%] p-3 rounded-lg text-sm ${msg.sender === 'user' ? 'bg-primary text-white rounded-br-none' : 'bg-white border border-gray-200 text-gray-800 rounded-bl-none shadow-sm'}`}>
                  {msg.image && <img src={msg.image} alt="Upload" className="max-w-full rounded mb-2" style={{ maxHeight: '150px' }} />}
                  {msg.text && <div>{msg.text}</div>}
                </div>
              </div>
            ))}
            {isLoading && (
              <div className="flex justify-start">
                <div className="bg-white border border-gray-200 text-gray-500 p-3 rounded-lg rounded-bl-none shadow-sm flex items-center space-x-2">
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                  <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Image Preview Area */}
          {selectedImage && (
            <div className="p-2 border-t border-gray-200 bg-gray-50 flex items-center justify-between">
              <div className="flex items-center">
                <img src={selectedImage.preview} alt="Preview" className="h-10 w-10 object-cover rounded mr-2" />
                <span className="text-xs text-gray-500">Image jointe prête à l'envoi</span>
              </div>
              <button onClick={() => setSelectedImage(null)} className="text-red-500 hover:text-red-700">
                <X className="w-4 h-4" />
              </button>
            </div>
          )}

          {/* Input Area */}
          <form onSubmit={handleSend} className="p-3 border-t border-gray-200 bg-white flex items-center space-x-2">
            <button 
              type="button" 
              onClick={() => fileInputRef.current?.click()}
              className="text-gray-400 hover:text-primary transition-colors flex-shrink-0"
              title="Joindre une photo"
            >
              <ImageIcon className="w-5 h-5" />
            </button>
            <input 
              type="file" 
              accept="image/*" 
              ref={fileInputRef} 
              onChange={handleImageSelect} 
              className="hidden" 
            />
            <input 
              type="text" 
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Posez votre question..." 
              className="flex-grow px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-primary text-sm"
            />
            <button 
              type="submit" 
              disabled={isLoading || (!input.trim() && !selectedImage)}
              className="bg-primary text-white p-2 rounded-md hover:bg-primary-light disabled:opacity-50 flex-shrink-0"
            >
              <Send className="w-5 h-5" />
            </button>
          </form>
        </div>
      )}
    </>
  );
};

export default Chatbot;
