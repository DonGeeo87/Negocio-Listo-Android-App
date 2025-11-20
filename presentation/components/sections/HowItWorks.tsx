'use client'

import { motion } from 'framer-motion'
import { useState } from 'react'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism'
import {
  Smartphone, Cloud, Database, RefreshCw,
  ArrowRight, CheckCircle2, Code2, Zap
} from 'lucide-react'

const codeExamples = [
  {
    title: 'ViewModel con State Management',
    language: 'kotlin',
    explanation: 'El ViewModel gestiona el estado de la pantalla de forma reactiva. Usa StateFlow para que la UI se actualice automáticamente cuando cambian los datos. Hilt inyecta los Use Cases necesarios. Cuando el usuario abre la pantalla de productos, el ViewModel automáticamente carga los datos usando el Use Case. Si hay un error, se muestra en la UI sin bloquear la aplicación. El estado se actualiza en tiempo real, por lo que si otro usuario agrega un producto, la lista se refresca automáticamente.',
    code: `@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val addProductUseCase: AddProductUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()
    
    init {
        loadProducts()
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase()
                .catch { _uiState.update { it.copy(error = it.message) } }
                .collect { products ->
                    _uiState.update { it.copy(products = products) }
                }
        }
    }
}`,
  },
  {
    title: 'Repository Pattern con Offline-First',
    language: 'kotlin',
    explanation: 'El Repository implementa la estrategia Offline-First: primero muestra datos locales (Room) para respuesta instantánea, luego sincroniza con Firebase en segundo plano. Si falla la conexión, la app sigue funcionando con datos locales. Cuando el usuario busca productos, primero ve los datos guardados en el dispositivo (respuesta instantánea), luego en segundo plano se sincronizan con la nube. Si el usuario está sin internet, puede seguir trabajando normalmente. Cuando vuelve la conexión, todos los cambios se sincronizan automáticamente sin que el usuario tenga que hacer nada.',
    code: `@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val localDataSource: ProductLocalDataSource,
    private val remoteDataSource: ProductRemoteDataSource
) : ProductRepository {
    
    override fun getProducts(): Flow<List<Product>> = flow {
        // Primero emitir datos locales (offline-first)
        emit(localDataSource.getAllProducts())
        
        // Luego sincronizar con Firebase
        try {
            val remoteProducts = remoteDataSource.getAllProducts()
            localDataSource.saveProducts(remoteProducts)
            emit(remoteProducts)
        } catch (e: Exception) {
            // Si falla, mantener datos locales
            Log.e("ProductRepository", "Sync failed", e)
        }
    }
}`,
  },
  {
    title: 'Use Case - Lógica de Negocio',
    language: 'kotlin',
    explanation: 'Los Use Cases contienen la lógica de negocio pura, independiente de la UI y las fuentes de datos. Validan reglas de negocio (como precios positivos) antes de guardar. Esto hace el código más testeable y mantenible. Cuando el usuario intenta agregar un producto, el Use Case valida que el nombre no esté vacío y que el precio sea mayor a cero. Si alguna validación falla, retorna un error que se muestra al usuario de forma amigable. Si todo está correcto, guarda el producto y la UI se actualiza automáticamente mostrando el nuevo producto en la lista.',
    code: `@Singleton
class AddProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Unit> {
        return try {
            // Validaciones de negocio
            require(product.name.isNotBlank()) { "Nombre requerido" }
            require(product.price > 0) { "Precio debe ser mayor a 0" }
            
            repository.addProduct(product)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}`,
  },
]

const flowSteps = [
  {
    id: '1',
    title: 'Usuario Interactúa',
    description: 'El usuario realiza una acción en la UI (Compose)',
    icon: Smartphone,
    color: 'bg-blue-500',
  },
  {
    id: '2',
    title: 'ViewModel Procesa',
    description: 'El ViewModel ejecuta Use Cases y actualiza el estado',
    icon: Zap,
    color: 'bg-yellow-500',
  },
  {
    id: '3',
    title: 'Use Case Valida',
    description: 'La lógica de negocio valida y procesa la operación',
    icon: CheckCircle2,
    color: 'bg-green-500',
  },
  {
    id: '4',
    title: 'Repository Gestiona',
    description: 'El Repository guarda localmente (Room) y sincroniza (Firebase)',
    icon: Database,
    color: 'bg-purple-500',
  },
  {
    id: '5',
    title: 'Sincronización',
    description: 'WorkManager sincroniza en background cuando hay conexión',
    icon: RefreshCw,
    color: 'bg-indigo-500',
  },
]

export default function HowItWorks() {
  const [selectedCode, setSelectedCode] = useState(0)

  return (
    <section className="py-20 bg-gradient-to-b from-white to-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="text-center mb-12"
        >
          <h2 className="text-4xl md:text-5xl font-bold gradient-text mb-4">
            ¿Cómo Funciona?
          </h2>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto mb-2">
            Flujo de datos y arquitectura interna de NegocioListo
          </p>
          <p className="text-sm text-gray-500 max-w-2xl mx-auto">
            💡 <strong>Explicación:</strong> Cuando el usuario realiza una acción, los datos fluyen a través de las capas 
            de la aplicación. Cada paso tiene una responsabilidad específica, garantizando que la app funcione de forma 
            eficiente y confiable.
          </p>
        </motion.div>

        {/* Flow Diagram */}
        <div className="mb-16">
          <div className="flex flex-col md:flex-row items-center justify-center space-y-4 md:space-y-0 md:space-x-4">
            {flowSteps.map((step, index) => {
              const Icon = step.icon
              return (
                <div key={step.id} className="flex items-center">
                  <motion.div
                    initial={{ opacity: 0, scale: 0.8 }}
                    whileInView={{ opacity: 1, scale: 1 }}
                    viewport={{ once: true }}
                    transition={{ duration: 0.5, delay: index * 0.1 }}
                    className="bg-white rounded-2xl p-6 shadow-lg hover:shadow-xl transition-shadow w-48 text-center"
                  >
                    <div className={`${step.color} w-12 h-12 rounded-lg flex items-center justify-center mx-auto mb-4`}>
                      <Icon className="w-6 h-6 text-white" />
                    </div>
                    <h3 className="font-bold text-gray-900 mb-2">{step.title}</h3>
                    <p className="text-sm text-gray-600">{step.description}</p>
                  </motion.div>
                  {index < flowSteps.length - 1 && (
                    <ArrowRight className="hidden md:block w-8 h-8 text-gray-400 mx-4" />
                  )}
                </div>
              )
            })}
          </div>
        </div>

        {/* Code Examples */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.4 }}
        >
          <h3 className="text-2xl font-bold text-gray-900 mb-6 text-center">
            Ejemplos de Código Destacados
          </h3>

          {/* Code Tabs */}
          <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
            <div className="flex border-b border-gray-200">
              {codeExamples.map((example, index) => (
                <button
                  key={index}
                  onClick={() => setSelectedCode(index)}
                  className={`flex-1 px-6 py-4 text-sm font-medium transition-colors ${
                    selectedCode === index
                      ? 'bg-primary text-white'
                      : 'bg-gray-50 text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <div className="flex items-center justify-center space-x-2">
                    <Code2 className="w-4 h-4" />
                    <span>{example.title}</span>
                  </div>
                </button>
              ))}
            </div>

            <div className="p-6 bg-gradient-to-r from-primary/5 to-secondary/5 border-b border-gray-200">
              <div className="flex items-start space-x-3">
                <span className="text-2xl">💡</span>
                <div className="flex-1">
                  <p className="font-semibold text-primary mb-2">¿Qué hace este código?</p>
                  <p className="text-sm text-gray-700 leading-relaxed">
                    {codeExamples[selectedCode].explanation}
                  </p>
                </div>
              </div>
            </div>
            <div className="p-0">
              <SyntaxHighlighter
                language={codeExamples[selectedCode].language}
                style={vscDarkPlus}
                customStyle={{
                  margin: 0,
                  borderRadius: 0,
                  fontSize: '14px',
                }}
              >
                {codeExamples[selectedCode].code}
              </SyntaxHighlighter>
            </div>
          </div>
        </motion.div>

        {/* Key Highlights */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.6 }}
          className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6"
        >
          {[
            {
              title: 'Offline-First',
              description: 'Funciona completamente sin conexión. Los datos se sincronizan automáticamente cuando hay internet.',
              icon: Cloud,
            },
            {
              title: 'Clean Architecture',
              description: 'Separación clara de capas permite mantener el código limpio, testeable y escalable.',
              icon: Database,
            },
            {
              title: 'State Management',
              description: 'StateFlow y Compose State para una gestión reactiva y eficiente del estado de la aplicación.',
              icon: Zap,
            },
          ].map((highlight, index) => {
            const Icon = highlight.icon
            return (
              <motion.div
                key={index}
                initial={{ opacity: 0, scale: 0.9 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                transition={{ duration: 0.3, delay: index * 0.1 }}
                className="bg-white rounded-xl p-6 shadow-md hover:shadow-lg transition-shadow"
              >
                <Icon className="w-8 h-8 text-primary mb-4" />
                <h4 className="text-lg font-bold text-gray-900 mb-2">{highlight.title}</h4>
                <p className="text-sm text-gray-600">{highlight.description}</p>
              </motion.div>
            )
          })}
        </motion.div>
      </div>
    </section>
  )
}

