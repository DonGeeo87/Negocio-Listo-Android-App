package com.negociolisto.app.data.repository

import com.negociolisto.app.data.local.dao.InspirationTipDao
import com.negociolisto.app.data.local.entity.toDomainModel
import com.negociolisto.app.data.local.entity.toEntity
import com.negociolisto.app.domain.model.InspirationTip
import com.negociolisto.app.domain.model.TimeOfDay
import com.negociolisto.app.domain.model.TipCategory
import com.negociolisto.app.domain.repository.InspirationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎯 IMPLEMENTACIÓN DEL REPOSITORIO DE TIPS
 * 
 * Implementación concreta que maneja la lógica de negocio
 * para los tips de inspiración usando Room.
 */
@Singleton
class InspirationRepositoryImpl @Inject constructor(
    private val inspirationTipDao: InspirationTipDao
) : InspirationRepository {

    override suspend fun getRandomTip(
        timeOfDay: TimeOfDay,
        excludeCategory: TipCategory?
    ): InspirationTip? = withContext(Dispatchers.IO) {
        
        // Si hay categoría a excluir, intentar obtener tip de otra categoría
        val tipEntity = if (excludeCategory != null) {
            inspirationTipDao.getRandomTipExcludingCategory(
                timeOfDay.name,
                excludeCategory.name
            )
        } else {
            inspirationTipDao.getRandomUnusedTipByTime(timeOfDay.name)
        }
        
        // Si no hay tips disponibles, resetear todos y obtener uno nuevo
        if (tipEntity == null) {
            resetAllTips()
            inspirationTipDao.getRandomUnusedTipByTime(timeOfDay.name)
        } else {
            tipEntity
        }?.toDomainModel()
    }

    override suspend fun markTipAsUsed(tipId: Long) {
        withContext(Dispatchers.IO) {
            val tip = inspirationTipDao.getTipById(tipId)
            tip?.let { tipEntity ->
                val updatedTip = tipEntity.copy(isUsed = true)
                inspirationTipDao.markTipAsUsed(updatedTip)
            }
        }
    }

    override suspend fun resetAllTips() = withContext(Dispatchers.IO) {
        inspirationTipDao.resetAllTips()
    }

    override suspend fun getTipStatistics(timeOfDay: TimeOfDay): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val available = inspirationTipDao.countAvailableTips(timeOfDay.name)
        val total = inspirationTipDao.countTotalTips(timeOfDay.name)
        Pair(available, total)
    }

    override suspend fun initializeIfEmpty() = withContext(Dispatchers.IO) {
        // Verificar si ya hay datos
        val totalTips = inspirationTipDao.countTotalTips(TimeOfDay.MORNING.name) +
                       inspirationTipDao.countTotalTips(TimeOfDay.AFTERNOON.name) +
                       inspirationTipDao.countTotalTips(TimeOfDay.NIGHT.name) +
                       inspirationTipDao.countTotalTips(TimeOfDay.DAWN.name)
        
        if (totalTips == 0) {
            populateInitialTips()
        }
    }

    /**
     * 🌱 POBLAR TIPS INICIALES
     * 
     * Inserta los tips iniciales en la base de datos.
     */
    private suspend fun populateInitialTips() {
        val initialTips = createInitialTips()
        inspirationTipDao.insertTips(initialTips.map { it.toEntity() })
    }

    /**
     * 📝 CREAR TIPS INICIALES
     * 
     * Genera la lista de tips iniciales organizados por categoría y horario.
     */
    private fun createInitialTips(): List<InspirationTip> {
        return listOf(
            // 🌅 MADRUGADA (0-5h) - Motivación y preparación
            InspirationTip(content = "La madrugada es de los emprendedores. ¡Aprovecha la tranquilidad para planificar tu día!", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.DAWN),
            InspirationTip(content = "Cada nuevo día trae nuevas oportunidades. ¡Despierta con energía y determinación!", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.DAWN),
            InspirationTip(content = "Un inventario bien organizado desde temprano es la base del éxito del día.", category = TipCategory.INVENTORY_MANAGEMENT, timeOfDay = TimeOfDay.DAWN),
            InspirationTip(content = "Revisa tus números de ayer. Los datos no mienten y te guían hacia mejores decisiones.", category = TipCategory.FINANCES, timeOfDay = TimeOfDay.DAWN),
            InspirationTip(content = "La preparación es la clave. Organiza tu espacio de trabajo antes de que lleguen los clientes.", category = TipCategory.BUSINESS_ADVICE, timeOfDay = TimeOfDay.DAWN),
            
            // 🌞 MAÑANA (6-11h) - Energía y enfoque
            InspirationTip(content = "El éxito comienza con el primer cliente del día. ¡Dale la bienvenida con una sonrisa!", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "Una sonrisa abre puertas que el dinero no puede abrir. ¡Sé amable con todos!", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "Las mejores ventas se hacen por la mañana cuando los clientes tienen energía y disposición.", category = TipCategory.SALES_TIPS, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "Mantén tu mostrador limpio y ordenado. La primera impresión es la que cuenta.", category = TipCategory.BUSINESS_ADVICE, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "Cada venta es una oportunidad de crear un cliente fiel. ¡Trata cada transacción como especial!", category = TipCategory.SALES_TIPS, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "La paciencia es una virtud en los negocios. No todos los días serán perfectos, pero cada día cuenta.", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.MORNING),
            
            // ☀️ TARDE (12-17h) - Productividad y gestión
            InspirationTip(content = "Revisa tus números del mediodía. ¿Cómo van las ventas? ¿Qué puedes mejorar?", category = TipCategory.FINANCES, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "Un inventario ordenado es dinero bien invertido. Tómate tiempo para organizar tu stock.", category = TipCategory.INVENTORY_MANAGEMENT, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "La tarde es perfecta para hacer seguimiento a tus clientes. ¡Mantén esas relaciones fuertes!", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "Ofrece productos complementarios. Un cliente que compra pan también podría querer mantequilla.", category = TipCategory.SALES_TIPS, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "La consistencia en el servicio es lo que diferencia a los buenos negocios de los excelentes.", category = TipCategory.BUSINESS_ADVICE, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "Conoce tus productos mejor que nadie. Tu expertise se nota y los clientes lo valoran.", category = TipCategory.BUSINESS_ADVICE, timeOfDay = TimeOfDay.AFTERNOON),
            
            // 🌙 NOCHE (18-23h) - Reflexión y preparación
            InspirationTip(content = "Celebra tus logros del día, por pequeños que sean. ¡Cada paso cuenta hacia el éxito!", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.NIGHT),
            InspirationTip(content = "Planifica el mañana, descansa hoy. Un emprendedor descansado es un emprendedor exitoso.", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.NIGHT),
            InspirationTip(content = "Revisa qué productos se vendieron más hoy. Esa información es oro para mañana.", category = TipCategory.INVENTORY_MANAGEMENT, timeOfDay = TimeOfDay.NIGHT),
            InspirationTip(content = "Anota los comentarios de tus clientes. Sus opiniones son tu mejor guía de mejora.", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.NIGHT),
            InspirationTip(content = "Calcula tu margen de ganancia diario. Los números no mienten y te muestran el camino.", category = TipCategory.FINANCES, timeOfDay = TimeOfDay.NIGHT),
            InspirationTip(content = "Mañana es una nueva oportunidad de superar a hoy. ¡Prepárate para ser aún mejor!", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.NIGHT),
            
            // 💰 CONSEJOS DE VENTAS ADICIONALES
            InspirationTip(content = "Conoce a tus clientes por su nombre. Ese detalle personal marca la diferencia.", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "Ofrece opciones, no impongas decisiones. Los clientes valoran la libertad de elegir.", category = TipCategory.SALES_TIPS, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "Un cliente satisfecho trae 5 clientes nuevos. ¡Invierte en la satisfacción!", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.NIGHT),
            InspirationTip(content = "La puntualidad en el servicio es respeto hacia tus clientes. ¡Sé puntual siempre!", category = TipCategory.BUSINESS_ADVICE, timeOfDay = TimeOfDay.MORNING),
            
            // 📦 GESTIÓN DE INVENTARIO
            InspirationTip(content = "Un producto bien exhibido se vende solo. ¡Invierte tiempo en tu presentación!", category = TipCategory.INVENTORY_MANAGEMENT, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "Controla tus existencias diariamente. Un stock vacío es una venta perdida.", category = TipCategory.INVENTORY_MANAGEMENT, timeOfDay = TimeOfDay.DAWN),
            InspirationTip(content = "Los productos frescos siempre al frente. La rotación es clave para mantener calidad.", category = TipCategory.INVENTORY_MANAGEMENT, timeOfDay = TimeOfDay.MORNING),
            
            // 💼 CONSEJOS DE NEGOCIO
            InspirationTip(content = "Tu negocio es tu reflejo. Si te sientes orgulloso, tus clientes también lo sentirán.", category = TipCategory.BUSINESS_ADVICE, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "La honestidad en los negocios es la mejor política. Construye confianza, construye futuro.", category = TipCategory.BUSINESS_ADVICE, timeOfDay = TimeOfDay.NIGHT),
            InspirationTip(content = "Aprende de tus errores, celebra tus aciertos. Ambos son parte del crecimiento.", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.NIGHT),
            
            // 📊 FINANZAS
            InspirationTip(content = "Lleva un registro de todo. Los números te cuentan la historia real de tu negocio.", category = TipCategory.FINANCES, timeOfDay = TimeOfDay.DAWN),
            InspirationTip(content = "No todos los gastos son malos. Invierte en lo que mejore tu servicio al cliente.", category = TipCategory.FINANCES, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "Un peso ahorrado es un peso ganado. Busca eficiencias sin sacrificar calidad.", category = TipCategory.FINANCES, timeOfDay = TimeOfDay.NIGHT),
            
            // 🤝 ATENCIÓN AL CLIENTE
            InspirationTip(content = "Escucha más de lo que hablas. Tus clientes te dirán exactamente qué necesitan.", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "Un 'gracias' genuino vale más que mil palabras. Agradece cada compra.", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "El cliente siempre tiene razón en lo que siente. Valida sus emociones, soluciona sus problemas.", category = TipCategory.CUSTOMER_SERVICE, timeOfDay = TimeOfDay.NIGHT),
            
            // 💪 MOTIVACIÓN ADICIONAL
            InspirationTip(content = "Los obstáculos son oportunidades disfrazadas. ¡Encuentra la manera de superarlos!", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "Tu actitud determina tu altitud. Mantén una mentalidad positiva, los resultados vendrán.", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "Cada día es una nueva oportunidad de ser mejor que ayer. ¡Aprovecha cada momento!", category = TipCategory.MOTIVATION, timeOfDay = TimeOfDay.DAWN),
            
            // 💰 VENTAS AVANZADAS
            InspirationTip(content = "Vende beneficios, no características. Los clientes compran soluciones, no productos.", category = TipCategory.SALES_TIPS, timeOfDay = TimeOfDay.MORNING),
            InspirationTip(content = "Crea urgencia con ofertas limitadas. La escasez aumenta el deseo de compra.", category = TipCategory.SALES_TIPS, timeOfDay = TimeOfDay.AFTERNOON),
            InspirationTip(content = "Sigue a tus clientes después de la venta. Un cliente feliz es tu mejor vendedor.", category = TipCategory.SALES_TIPS, timeOfDay = TimeOfDay.NIGHT)
        )
    }
}
