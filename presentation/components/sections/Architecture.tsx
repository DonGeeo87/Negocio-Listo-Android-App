'use client'

import { motion } from 'framer-motion'
import { useState } from 'react'
import { Layers, Code, Database, ArrowDown, CheckCircle2 } from 'lucide-react'

const architectureLayers = [
  {
    id: 'presentation',
    name: 'Presentation Layer',
    description: 'Jetpack Compose + ViewModels - La capa que el usuario ve e interactúa',
    explanation: 'Esta capa contiene toda la interfaz de usuario (pantallas, botones, listas) y los ViewModels que gestionan el estado y la lógica de presentación.',
    color: 'bg-blue-500',
    components: [
      'UI Components',
      'ViewModels',
      'Navigation',
      'State Management',
    ],
  },
  {
    id: 'domain',
    name: 'Domain Layer',
    description: 'Use Cases + Business Models - La lógica de negocio pura',
    explanation: 'Esta capa contiene las reglas de negocio y casos de uso. No depende de la UI ni de las fuentes de datos, lo que la hace fácil de probar.',
    color: 'bg-purple-500',
    components: [
      'Use Cases',
      'Domain Models',
      'Interfaces',
      'Business Logic',
    ],
  },
  {
    id: 'data',
    name: 'Data Layer',
    description: 'Repositories + Data Sources - Acceso a datos locales y remotos',
    explanation: 'Esta capa gestiona todos los datos: almacenamiento local (Room), sincronización con Firebase, y conversión entre modelos de datos.',
    color: 'bg-green-500',
    components: [
      'Repositories',
      'Room Database',
      'Firebase Firestore',
      'Data Mappers',
    ],
  },
]

const designPatterns = [
  { name: 'MVVM', description: 'Model-View-ViewModel - Separa la interfaz (View) de la lógica (ViewModel) y los datos (Model). Facilita el testing y mantenimiento.' },
  { name: 'Repository Pattern', description: 'Abstracción de fuentes de datos - Unifica el acceso a datos locales (Room) y remotos (Firebase) en una sola interfaz.' },
  { name: 'Use Cases', description: 'Lógica de negocio pura - Cada funcionalidad tiene su caso de uso independiente, fácil de probar y reutilizar.' },
  { name: 'Dependency Injection', description: 'Hilt gestiona dependencias - En lugar de crear objetos manualmente, Hilt los inyecta automáticamente donde se necesitan.' },
  { name: 'Offline-First', description: 'Funciona sin conexión - La app prioriza datos locales para respuesta instantánea, luego sincroniza con la nube en segundo plano.' },
  { name: 'Single Source of Truth', description: 'Room como fuente única local - Todos los datos locales provienen de Room, evitando inconsistencias.' },
]

export default function Architecture() {
  const [selectedLayer, setSelectedLayer] = useState<string | null>(null)

  return (
    <section className="py-20 bg-gradient-to-b from-gray-50 to-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="text-center mb-12"
        >
          <h2 className="text-4xl md:text-5xl font-bold gradient-text mb-4">
            Arquitectura
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto mb-2">
            Clean Architecture + MVVM para un código mantenible y escalable
          </p>
          <p className="text-sm text-gray-500 max-w-2xl mx-auto">
            💡 <strong>Explicación:</strong> La aplicación está organizada en 3 capas principales. 
            Cada capa tiene una responsabilidad específica, lo que facilita el mantenimiento y las pruebas. 
            Haz clic en cada capa para ver sus componentes.
          </p>
        </motion.div>

        {/* Architecture Diagram */}
        <div className="mb-16">
          <div className="max-w-4xl mx-auto">
            {architectureLayers.map((layer, index) => {
              const isSelected = selectedLayer === layer.id
              return (
                <motion.div
                  key={layer.id}
                  initial={{ opacity: 0, x: -50 }}
                  whileInView={{ opacity: 1, x: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: index * 0.2 }}
                  className={`mb-4 rounded-2xl p-6 shadow-lg cursor-pointer transition-all duration-300 ${
                    isSelected
                      ? 'bg-gradient-to-r from-primary to-secondary text-white scale-105'
                      : 'bg-white hover:shadow-xl'
                  }`}
                  onClick={() => setSelectedLayer(isSelected ? null : layer.id)}
                >
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center space-x-4">
                      <div className={`${layer.color} w-12 h-12 rounded-lg flex items-center justify-center`}>
                        <Layers className="w-6 h-6 text-white" />
                      </div>
                      <div>
                        <h3 className={`text-2xl font-bold ${isSelected ? 'text-white' : 'text-gray-900'}`}>
                          {layer.name}
                        </h3>
                        <p className={`text-sm ${isSelected ? 'text-white/80' : 'text-gray-600'}`}>
                          {layer.description}
                        </p>
                      </div>
                    </div>
                    {index < architectureLayers.length - 1 && (
                      <ArrowDown className={`w-6 h-6 ${isSelected ? 'text-white' : 'text-gray-400'}`} />
                    )}
                  </div>

                  {isSelected && (
                    <motion.div
                      initial={{ opacity: 0, height: 0 }}
                      animate={{ opacity: 1, height: 'auto' }}
                      transition={{ duration: 0.3 }}
                      className="mt-4 pt-4 border-t border-white/20 space-y-4"
                    >
                      {layer.explanation && (
                        <p className="text-sm text-white/80 italic mb-3">
                          💡 {layer.explanation}
                        </p>
                      )}
                      <div>
                        <p className="text-sm font-semibold mb-3 text-white/90">Componentes:</p>
                        <div className="grid grid-cols-2 gap-2">
                          {layer.components.map((component, compIndex) => (
                            <motion.div
                              key={compIndex}
                              initial={{ opacity: 0, x: -10 }}
                              animate={{ opacity: 1, x: 0 }}
                              transition={{ duration: 0.2, delay: compIndex * 0.05 }}
                              className="flex items-center space-x-2"
                            >
                              <CheckCircle2 className="w-4 h-4 text-white" />
                              <span className="text-sm text-white/90">{component}</span>
                            </motion.div>
                          ))}
                        </div>
                      </div>
                    </motion.div>
                  )}
                </motion.div>
              )
            })}
          </div>
        </div>

        {/* Design Patterns */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.4 }}
        >
          <h3 className="text-2xl font-bold text-gray-900 mb-6 text-center">
            Patrones de Diseño Implementados
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {designPatterns.map((pattern, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, scale: 0.9 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                transition={{ duration: 0.3, delay: index * 0.1 }}
                className="bg-white rounded-xl p-6 shadow-md hover:shadow-lg transition-shadow border-l-4 border-primary"
              >
                <h4 className="text-lg font-bold text-gray-900 mb-2">{pattern.name}</h4>
                <p className="text-sm text-gray-600">{pattern.description}</p>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* System Design */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.6 }}
          className="mt-12 bg-gradient-to-br from-primary/10 to-secondary/10 rounded-2xl p-8"
        >
          <h3 className="text-2xl font-bold text-gray-900 mb-6 text-center">
            Sistema de Diseño Unificado
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {[
              { title: 'Componentes Unificados', items: ['UnifiedButtons', 'UnifiedCards', 'UnifiedTopAppBar'] },
              { title: 'Design Tokens', items: ['Colores', 'Tipografía', 'Espaciado'] },
              { title: 'Gradientes Corporativos', items: ['#009FE3', '#312783'] },
              { title: 'Modo Oscuro', items: ['Soporte completo', 'Transiciones suaves'] },
            ].map((item, index) => (
              <div key={index} className="bg-white rounded-lg p-4">
                <h4 className="font-bold text-gray-900 mb-2">{item.title}</h4>
                <ul className="space-y-1">
                  {item.items.map((subItem, subIndex) => (
                    <li key={subIndex} className="text-sm text-gray-600 flex items-center space-x-2">
                      <CheckCircle2 className="w-4 h-4 text-primary" />
                      <span>{subItem}</span>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </motion.div>
      </div>
    </section>
  )
}

