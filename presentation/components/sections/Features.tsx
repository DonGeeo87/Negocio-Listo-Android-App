'use client'

import { motion } from 'framer-motion'
import { useState } from 'react'
import {
  Package, ShoppingCart, Users, DollarSign,
  FileText, Layers, BarChart3, Globe,
  MessageCircle, Bell, Link as LinkIcon, Palette
} from 'lucide-react'

const mainFeatures = [
  {
    id: 'inventory',
    title: 'Gestión de Inventario',
    icon: Package,
    color: 'bg-blue-500',
    features: [
      'Catálogo completo con imágenes optimizadas',
      'Control de stock y precios en CLP',
      'Categorización avanzada',
      'Escaneo de códigos de barras',
      'Alertas automáticas de stock bajo',
      'Búsqueda y filtros en tiempo real',
    ],
  },
  {
    id: 'sales',
    title: 'Sistema de Ventas',
    icon: ShoppingCart,
    color: 'bg-green-500',
    features: [
      'Registro rápido con carrito',
      'Múltiples métodos de pago (registro interno)',
      'Generación automática de facturas',
      'Historial completo de transacciones',
      'Estadísticas en tiempo real',
      'Integración con clientes y productos',
    ],
  },
  {
    id: 'customers',
    title: 'Gestión de Clientes',
    icon: Users,
    color: 'bg-purple-500',
    features: [
      'Base de datos completa',
      'Historial de compras detallado',
      'Información de contacto completa',
      'Importación desde contactos',
      'Segmentación de clientes',
      'Búsqueda y filtros avanzados',
    ],
  },
  {
    id: 'expenses',
    title: 'Control de Gastos',
    icon: DollarSign,
    color: 'bg-red-500',
    features: [
      'Categorización inteligente',
      'Seguimiento de proveedores',
      'Integración con ventas',
      'Análisis de ganancias',
      'Filtros por fecha y categoría',
      'Resúmenes automáticos con visualización clara',
    ],
  },
  {
    id: 'invoices',
    title: 'Sistema de Facturación',
    icon: FileText,
    color: 'bg-yellow-500',
    features: [
      'Múltiples plantillas (3 tipos)',
      'Personalización completa',
      'Exportación PDF de alta calidad',
      'Numeración automática',
      'Compartir por email y WhatsApp',
      'Vista previa antes de exportar',
    ],
  },
  {
    id: 'collections',
    title: 'Portal del Cliente',
    icon: Globe,
    color: 'bg-indigo-500',
    isHighlight: true,
    features: [
      'Portal web completo y funcional',
      '5 Templates visuales personalizables',
      'Chat en tiempo real bidireccional',
      'Sistema de pedidos completo',
      'Gestión automática de clientes',
      'Notificaciones Push (FCM)',
      'Links públicos compartibles',
      'Seguimiento de pedidos en tiempo real',
    ],
  },
]

const portalFeatures = [
  { icon: Palette, title: '5 Templates Visuales', description: 'MODERN, CLASSIC, MINIMAL, DARK, COLORFUL' },
  { icon: MessageCircle, title: 'Chat en Tiempo Real', description: 'Comunicación bidireccional cliente-negocio' },
  { icon: Bell, title: 'Notificaciones Push', description: 'Alertas FCM en tiempo real' },
  { icon: LinkIcon, title: 'Links Públicos', description: 'Compartir por WhatsApp, email o SMS' },
]

export default function Features() {
  const [selectedFeature, setSelectedFeature] = useState<string | null>(null)

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
            Características Principales
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto mb-2">
            Funcionalidades completas para gestionar tu negocio de manera integral
          </p>
          <p className="text-sm text-gray-500 max-w-2xl mx-auto">
            💡 <strong>Nota:</strong> Haz clic en cada módulo para ver todas sus funcionalidades. 
            El Portal del Cliente es nuestra funcionalidad principal (Core Feature).
          </p>
        </motion.div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-12">
          {mainFeatures.map((feature, index) => {
            const Icon = feature.icon
            const isSelected = selectedFeature === feature.id

            return (
              <motion.div
                key={feature.id}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: index * 0.1 }}
                className={`relative rounded-2xl p-6 shadow-lg hover:shadow-xl transition-all duration-300 cursor-pointer border-2 ${
                  feature.isHighlight
                    ? 'bg-gradient-to-br from-primary to-secondary text-white border-primary'
                    : isSelected
                    ? 'bg-gray-50 border-primary'
                    : 'bg-white border-transparent'
                }`}
                onClick={() => setSelectedFeature(isSelected ? null : feature.id)}
              >
                {feature.isHighlight && (
                  <div className="absolute top-4 right-4 bg-white/20 backdrop-blur-md rounded-full px-3 py-1 text-xs font-bold">
                    CORE FEATURE
                  </div>
                )}

                <div className={`${feature.color} w-12 h-12 rounded-lg flex items-center justify-center mb-4 ${
                  feature.isHighlight ? 'bg-white/20' : ''
                }`}>
                  <Icon className={`w-6 h-6 ${feature.isHighlight ? 'text-white' : 'text-white'}`} />
                </div>

                <h3 className={`text-xl font-bold mb-2 ${feature.isHighlight ? 'text-white' : 'text-gray-900'}`}>
                  {feature.title}
                </h3>

                {isSelected && (
                  <motion.ul
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    transition={{ duration: 0.3 }}
                    className="mt-4 space-y-2"
                  >
                    {feature.features.map((item, itemIndex) => (
                      <motion.li
                        key={itemIndex}
                        initial={{ opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.2, delay: itemIndex * 0.05 }}
                        className={`text-sm flex items-start space-x-2 ${
                          feature.isHighlight ? 'text-white/90' : 'text-gray-600'
                        }`}
                      >
                        <span className="text-primary mt-1">•</span>
                        <span>{item}</span>
                      </motion.li>
                    ))}
                  </motion.ul>
                )}

                {!isSelected && (
                  <p className={`text-sm ${feature.isHighlight ? 'text-white/80' : 'text-gray-600'}`}>
                    {feature.features.length} funcionalidades
                  </p>
                )}
              </motion.div>
            )
          })}
        </div>

        {/* Portal del Cliente - Destacado */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.4 }}
          className="bg-gradient-to-r from-primary to-secondary rounded-2xl p-8 text-white"
        >
          <div className="text-center mb-8">
            <Globe className="w-16 h-16 mx-auto mb-4" />
            <h3 className="text-3xl font-bold mb-2">Portal del Cliente - Core Feature</h3>
            <p className="text-lg text-white/90 max-w-3xl mx-auto">
              Canal de comunicación profesional directo sin intermediarios ni grandes equipos.
              Ahorro de tiempo y dinero con automatización completa.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {portalFeatures.map((feature, index) => {
              const Icon = feature.icon
              return (
                <motion.div
                  key={index}
                  initial={{ opacity: 0, scale: 0.9 }}
                  whileInView={{ opacity: 1, scale: 1 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.3, delay: index * 0.1 }}
                  className="bg-white/10 backdrop-blur-md rounded-xl p-6 text-center"
                >
                  <Icon className="w-8 h-8 mx-auto mb-3" />
                  <h4 className="font-bold mb-2">{feature.title}</h4>
                  <p className="text-sm text-white/80">{feature.description}</p>
                </motion.div>
              )
            })}
          </div>
        </motion.div>
      </div>
    </section>
  )
}

