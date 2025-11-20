'use client'

import { motion } from 'framer-motion'
import { useState } from 'react'
import { 
  Code, Database, Cloud, Lock, Smartphone, 
  Layers, Zap, Palette, CheckCircle2 
} from 'lucide-react'

const techCategories = [
  {
    id: 'frontend',
    title: 'Frontend & UI',
    icon: Smartphone,
    color: 'bg-blue-500',
    technologies: [
      { name: 'Jetpack Compose', version: '1.5.10', description: 'UI moderna declarativa - Construye la interfaz con código, sin XML' },
      { name: 'Material Design 3', version: 'Latest', description: 'Sistema de diseño moderno - Guías visuales de Google' },
      { name: 'Navigation Component', version: 'Latest', description: 'Navegación tipo-safe - Navegación segura entre pantallas' },
      { name: 'Coil', version: '2.5.0', description: 'Carga eficiente de imágenes - Optimiza memoria y rendimiento' },
    ],
  },
  {
    id: 'backend',
    title: 'Backend & Datos',
    icon: Database,
    color: 'bg-green-500',
    technologies: [
      { name: 'Room Database', version: '2.6.1', description: 'Base de datos local SQLite - Almacena datos en el dispositivo' },
      { name: 'Firebase Firestore', version: '32.7.0', description: 'Base de datos en la nube - Sincroniza datos entre dispositivos' },
      { name: 'Firebase Storage', version: '32.7.0', description: 'Almacenamiento de imágenes - Guarda fotos de productos en la nube' },
      { name: 'Firebase Auth', version: '32.7.0', description: 'Autenticación segura - Login y registro de usuarios' },
      { name: 'Firebase FCM', version: '32.7.0', description: 'Notificaciones push - Alertas en tiempo real' },
      { name: 'DataStore', version: '1.1.1', description: 'Preferencias del usuario - Configuraciones y ajustes' },
    ],
  },
  {
    id: 'architecture',
    title: 'Arquitectura & DI',
    icon: Layers,
    color: 'bg-purple-500',
    technologies: [
      { name: 'Hilt', version: '2.51', description: 'Inyección de dependencias - Gestiona automáticamente las dependencias del código' },
      { name: 'Clean Architecture', version: 'Custom', description: 'Separación de capas - Organiza el código en capas independientes' },
      { name: 'MVVM', version: 'Pattern', description: 'Model-View-ViewModel - Separa la UI de la lógica de negocio' },
      { name: 'Repository Pattern', version: 'Pattern', description: 'Abstracción de datos - Unifica acceso a datos locales y remotos' },
      { name: 'Use Cases', version: 'Pattern', description: 'Lógica de negocio - Cada funcionalidad tiene su caso de uso' },
    ],
  },
  {
    id: 'utilities',
    title: 'Utilidades',
    icon: Zap,
    color: 'bg-yellow-500',
    technologies: [
      { name: 'Kotlin Coroutines', version: '1.7.3', description: 'Programación asíncrona - Ejecuta tareas sin bloquear la UI' },
      { name: 'Kotlinx DateTime', version: '0.5.0', description: 'Manejo moderno de fechas - Trabaja con fechas de forma segura' },
      { name: 'Flow', version: 'Built-in', description: 'Streams reactivos - Datos que se actualizan automáticamente' },
      { name: 'WorkManager', version: '2.9.0', description: 'Tareas en background - Sincroniza datos cuando hay conexión' },
    ],
  },
]

export default function TechStack() {
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null)

  return (
    <section className="py-20 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="text-center mb-12"
        >
          <h2 className="text-4xl md:text-5xl font-bold gradient-text mb-4">
            Stack Tecnológico
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto mb-2">
            Tecnologías modernas y probadas que potencian NegocioListo
          </p>
          <p className="text-sm text-gray-500 max-w-2xl mx-auto">
            💡 <strong>Nota:</strong> Haz clic en cada categoría para ver todas las tecnologías utilizadas. 
            Cada una tiene un propósito específico en la arquitectura de la aplicación.
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {techCategories.map((category, index) => {
            const Icon = category.icon
            const isSelected = selectedCategory === category.id

            return (
              <motion.div
                key={category.id}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className={`bg-gradient-to-br from-gray-50 to-white rounded-2xl p-6 shadow-lg hover:shadow-xl transition-all duration-300 cursor-pointer border-2 ${
                  isSelected ? 'border-primary' : 'border-transparent'
                }`}
                onClick={() => setSelectedCategory(isSelected ? null : category.id)}
              >
                <div className={`${category.color} w-12 h-12 rounded-lg flex items-center justify-center mb-4`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
                
                <h3 className="text-xl font-bold text-gray-900 mb-2">
                  {category.title}
                </h3>
                
                <div className="space-y-2 mt-4">
                  {category.technologies.slice(0, isSelected ? undefined : 2).map((tech, techIndex) => (
                    <motion.div
                      key={techIndex}
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.3, delay: techIndex * 0.05 }}
                      className="flex items-start space-x-2"
                    >
                      <CheckCircle2 className="w-4 h-4 text-primary mt-1 flex-shrink-0" />
                      <div className="flex-1">
                        <p className="text-sm font-semibold text-gray-900">
                          {tech.name}
                        </p>
                        <p className="text-xs text-gray-500">
                          {tech.version} • {tech.description}
                        </p>
                      </div>
                    </motion.div>
                  ))}
                </div>

                {!isSelected && category.technologies.length > 2 && (
                  <p className="text-sm text-primary mt-4 font-medium">
                    +{category.technologies.length - 2} más...
                  </p>
                )}
              </motion.div>
            )
          })}
        </div>

        {/* Versiones Principales */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="mt-12 bg-gradient-to-r from-primary to-secondary rounded-2xl p-8 text-white"
        >
          <h3 className="text-2xl font-bold mb-6 text-center">Versiones Principales</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { label: 'Kotlin', value: '1.9.22' },
              { label: 'Compose BOM', value: '2024.10.00' },
              { label: 'Hilt', value: '2.51' },
              { label: 'Room', value: '2.6.1' },
              { label: 'Firebase BOM', value: '32.7.0' },
              { label: 'Min SDK', value: '24 (Android 7.0)' },
              { label: 'Target SDK', value: '34 (Android 14)' },
              { label: 'DataStore', value: '1.1.1' },
            ].map((item, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, scale: 0.9 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                transition={{ duration: 0.3, delay: index * 0.05 }}
                className="bg-white/10 backdrop-blur-md rounded-lg p-4 text-center"
              >
                <p className="text-sm text-white/80 mb-1">{item.label}</p>
                <p className="text-lg font-bold">{item.value}</p>
              </motion.div>
            ))}
          </div>
        </motion.div>
      </div>
    </section>
  )
}

