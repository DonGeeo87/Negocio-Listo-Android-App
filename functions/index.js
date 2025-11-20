/**
 * 🔥 CLOUD FUNCTIONS PARA NEGOCIOLISTO
 * 
 * Funciones que se ejecutan en el servidor de Firebase
 * para tareas automáticas como envío de correos.
 * 
 * Desarrollador: Giorgio Interdonato Palacios
 * GitHub: @DonGeeo87
 */

const {onDocumentCreated} = require('firebase-functions/v2/firestore');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');

// Inicializar Firebase Admin
admin.initializeApp();

/**
 * 📧 CONFIGURACIÓN DE CORREO
 * 
 * Opción 1: Usar Gmail SMTP (recomendado para desarrollo)
 * Opción 2: Usar SendGrid, Mailgun u otro servicio (recomendado para producción)
 * 
 * Para usar Gmail:
 * 1. Habilitar "Acceso de aplicaciones menos seguras" en tu cuenta de Gmail
 * 2. O mejor: Usar "App Password" desde https://myaccount.google.com/apppasswords
 * 3. Configurar las variables de entorno en Firebase:
 *    firebase functions:config:set gmail.email="tu-email@gmail.com" gmail.password="tu-app-password"
 * 
 * Para usar SendGrid (recomendado para producción):
 * 1. Crear cuenta en https://sendgrid.com
 * 2. Obtener API Key
 * 3. Configurar: firebase functions:config:set sendgrid.api_key="tu-api-key"
 */

// Crear transporter de correo
// Usa variables de entorno de Firebase Functions (configuradas después del despliegue)
function createEmailTransporter() {
  // Leer variables de entorno
  const sendgridKey = process.env.SENDGRID_API_KEY || '';
  const gmailUser = process.env.GMAIL_EMAIL || '';
  const gmailPass = process.env.GMAIL_PASSWORD || '';
  
  // Opción 1: SendGrid (recomendado para producción)
  if (sendgridKey && sendgridKey !== '') {
    return nodemailer.createTransport({
      service: 'SendGrid',
      auth: {
        user: 'apikey',
        pass: sendgridKey
      }
    });
  }
  
  // Opción 2: Gmail SMTP (para desarrollo)
  if (gmailUser && gmailUser !== '' && gmailPass && gmailPass !== '') {
    return nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: gmailUser,
        pass: gmailPass
      }
    });
  }
  
  // Si no hay configuración, retornar null (la función fallará con un mensaje claro)
  return null;
}

/**
 * 📋 FUNCIÓN: ENVIAR NOTIFICACIÓN FCM CUANDO SE CREA UN PEDIDO
 * 
 * Se ejecuta automáticamente cuando se crea un nuevo pedido en Firestore.
 * Ruta: collections/{collectionId}/responses/{responseId}
 * 
 * Envía una notificación push al negocio para que sepa que hay un nuevo pedido.
 */
exports.onOrderCreated = onDocumentCreated(
  {
    document: 'collections/{collectionId}/responses/{responseId}',
    region: 'us-central1',
  },
  async (event) => {
    const orderData = event.data.data();
    const collectionId = event.params.collectionId;
    const responseId = event.params.responseId;
    
    console.log(`📋 Nuevo pedido creado: ${responseId} en colección ${collectionId}`);
    
    try {
      // Obtener datos de la colección para obtener el userId del negocio
      const collectionDoc = await admin.firestore()
        .collection('collections')
        .doc(collectionId)
        .get();
      
      if (!collectionDoc.exists) {
        console.log(`⚠️ Colección ${collectionId} no encontrada`);
        return null;
      }
      
      const collectionData = collectionDoc.data();
      let userId = collectionData.userId;
      
      // Si la colección no tiene userId, buscarlo de otras formas
      if (!userId) {
        console.log(`🔍 Colección no tiene userId, buscando alternativas...`);
        
        // Opción 1: Buscar en los clientes asociados
        if (collectionData.associatedCustomerIds && collectionData.associatedCustomerIds.length > 0) {
          const customerId = collectionData.associatedCustomerIds[0];
          console.log(`   Buscando userId en cliente asociado: ${customerId}`);
          
          const customerDoc = await admin.firestore()
            .collection('customers')
            .doc(customerId)
            .get();
          
          if (customerDoc.exists) {
            const customerData = customerDoc.data();
            userId = customerData.userId;
            if (userId) {
              console.log(`✅ userId encontrado en cliente: ${userId}`);
              
              // Actualizar la colección con el userId encontrado
              try {
                await admin.firestore()
                  .collection('collections')
                  .doc(collectionId)
                  .update({ userId: userId });
                console.log(`✅ Colección actualizada con userId: ${userId}`);
              } catch (error) {
                console.log(`⚠️ Error actualizando colección con userId: ${error.message}`);
              }
            }
          }
        }
        
        // Opción 2: Si aún no hay userId, buscar en pedidos anteriores de esta colección
        if (!userId) {
          console.log(`   Buscando userId en pedidos anteriores de la colección...`);
          const responsesSnapshot = await admin.firestore()
            .collection(`collections/${collectionId}/responses`)
            .limit(1)
            .get();
          
          if (!responsesSnapshot.empty) {
            const responseData = responsesSnapshot.docs[0].data();
            // Los pedidos pueden tener customerId, buscar el cliente
            if (responseData.customerId) {
              const customerDoc = await admin.firestore()
                .collection('customers')
                .doc(responseData.customerId)
                .get();
              
              if (customerDoc.exists) {
                const customerData = customerDoc.data();
                userId = customerData.userId;
                if (userId) {
                  console.log(`✅ userId encontrado en pedido anterior: ${userId}`);
                  
                  // Actualizar la colección con el userId encontrado
                  try {
                    await admin.firestore()
                      .collection('collections')
                      .doc(collectionId)
                      .update({ userId: userId });
                    console.log(`✅ Colección actualizada con userId: ${userId}`);
                  } catch (error) {
                    console.log(`⚠️ Error actualizando colección con userId: ${error.message}`);
                  }
                }
              }
            }
          }
        }
      }
      
      if (!userId) {
        console.log(`⚠️ No se pudo encontrar userId para colección ${collectionId}`);
        return null;
      }
      
      // Obtener token FCM del usuario del negocio
      const userDoc = await admin.firestore()
        .collection('users')
        .doc(userId)
        .get();
      
      if (!userDoc.exists) {
        console.log(`⚠️ Usuario ${userId} no encontrado`);
        return null;
      }
      
      const userData = userDoc.data();
      const fcmToken = userData.fcmToken;
      
      if (!fcmToken) {
        console.log(`⚠️ Usuario ${userId} no tiene token FCM registrado`);
        return null;
      }
      
      // Preparar datos de la notificación
      const clientName = orderData.clientName || 'Cliente';
      const orderTotal = orderData.subtotal || 0;
      const orderTotalFormatted = formatClp(orderTotal);
      
      // Construir payload de la notificación
      const message = {
        token: fcmToken,
        notification: {
          title: `📦 Nuevo pedido de ${clientName}`,
          body: `Pedido por ${orderTotalFormatted} - Ver detalles`,
        },
        data: {
          type: 'order',
          title: `📦 Nuevo pedido de ${clientName}`,
          body: `Pedido por ${orderTotalFormatted} - Ver detalles`,
          collectionId: collectionId,
          responseId: responseId,
          clientName: clientName,
          orderTotal: orderTotal.toString(),
        },
        android: {
          priority: 'high',
          notification: {
            channelId: 'order_notifications',
            sound: 'default',
            icon: 'ic_notification',
          },
        },
        apns: {
          payload: {
            aps: {
              sound: 'default',
              badge: 1,
            },
          },
        },
      };
      
      // Enviar notificación FCM
      const response = await admin.messaging().send(message);
      console.log(`✅ Notificación FCM de pedido enviada exitosamente: ${response}`);
      
      // ⚠️ ENVÍO DE CORREO DESHABILITADO
      // El envío automático de correos está deshabilitado por decisión del desarrollador
      // Los pedidos se procesan normalmente pero no se envían correos automáticos
      
      return null;
    } catch (error) {
      console.error(`❌ Error enviando notificación FCM de pedido: ${error.message}`);
      console.error(`   Stack: ${error.stack}`);
      // No lanzar error para evitar que la función falle completamente
      return null;
    }
  });

/**
 * 📝 GENERAR CUERPO DEL CORREO (TEXTO PLANO)
 */
function generateOrderEmailBody(orderData, collectionName, items, productsMap) {
  let body = `Hola ${orderData.clientName || 'Cliente'},\n\n`;
  body += `¡Gracias por tu pedido!\n\n`;
  body += `Detalles del pedido:\n`;
  body += `━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n`;
  body += `Colección: ${collectionName}\n`;
  body += `Pedido ID: ${orderData.id || 'N/A'}\n`;
  body += `Fecha: ${formatDate(orderData.createdAt)}\n`;
  body += `━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n`;
  body += `Productos solicitados:\n`;
  
  // Agregar items
  Object.entries(items).forEach(([productId, item]) => {
    const product = productsMap[productId] || {};
    const productName = product.name || 'Producto desconocido';
    const unitPrice = product.salePrice || 0;
    const quantity = item.quantity || 0;
    const lineTotal = unitPrice * quantity;
    
    body += `  • ${productName}\n`;
    body += `    Cantidad: ${quantity}\n`;
    body += `    Precio unitario: ${formatClp(unitPrice)}\n`;
    body += `    Subtotal: ${formatClp(lineTotal)}\n`;
    
    if (item.notes) {
      body += `    Notas: ${item.notes}\n`;
    }
    body += `\n`;
  });
  
  body += `Resumen:\n`;
  body += `━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n`;
  body += `Subtotal: ${formatClp(orderData.subtotal || 0)}\n`;
  body += `Total: ${formatClp(orderData.subtotal || 0)}\n`;
  body += `━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n`;
  
  // Estado del pedido
  const statusMessage = getStatusMessage(orderData.status);
  body += `${statusMessage}\n\n`;
  
  if (orderData.address) {
    body += `Dirección de entrega:\n${orderData.address}\n\n`;
  }
  
  if (orderData.observations) {
    body += `Tus notas:\n${orderData.observations}\n\n`;
  }
  
  body += `Estamos procesando tu pedido y te mantendremos informado sobre su estado.\n\n`;
  body += `Saludos,\n`;
  body += `Tu equipo de NegocioListo`;
  
  return body;
}

/**
 * 📝 GENERAR CUERPO DEL CORREO (HTML)
 */
function generateOrderEmailHTML(orderData, collectionName, items, productsMap) {
  let html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #009FE3 0%, #312783 100%); color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
        .content { background: #f9f9f9; padding: 20px; border: 1px solid #ddd; }
        .footer { background: #333; color: white; padding: 15px; text-align: center; border-radius: 0 0 8px 8px; }
        .order-item { background: white; padding: 15px; margin: 10px 0; border-radius: 4px; border-left: 4px solid #009FE3; }
        .total { font-size: 18px; font-weight: bold; color: #009FE3; }
        .status-badge { display: inline-block; padding: 8px 16px; background: #fff3cd; color: #856404; border-radius: 20px; font-weight: bold; }
      </style>
    </head>
    <body>
      <div class="container">
        <div class="header">
          <h1>✅ Confirmación de Pedido</h1>
          <p>${collectionName}</p>
        </div>
        <div class="content">
          <p>Hola <strong>${orderData.clientName || 'Cliente'}</strong>,</p>
          <p>¡Gracias por tu pedido!</p>
          
          <h3>Detalles del pedido:</h3>
          <p><strong>Pedido ID:</strong> ${orderData.id || 'N/A'}</p>
          <p><strong>Fecha:</strong> ${formatDate(orderData.createdAt)}</p>
          
          <h3>Productos solicitados:</h3>
  `;
  
  // Agregar items
  Object.entries(items).forEach(([productId, item]) => {
    const product = productsMap[productId] || {};
    const productName = product.name || 'Producto desconocido';
    const unitPrice = product.salePrice || 0;
    const quantity = item.quantity || 0;
    const lineTotal = unitPrice * quantity;
    
    html += `
      <div class="order-item">
        <h4>${productName}</h4>
        <p>Cantidad: ${quantity} × ${formatClp(unitPrice)} = <strong>${formatClp(lineTotal)}</strong></p>
        ${item.notes ? `<p><em>Notas: ${item.notes}</em></p>` : ''}
      </div>
    `;
  });
  
  html += `
          <div class="total">
            <p>Total: ${formatClp(orderData.subtotal || 0)}</p>
          </div>
          
          <div class="status-badge">${getStatusMessage(orderData.status)}</div>
          
          ${orderData.address ? `<p><strong>Dirección de entrega:</strong><br>${orderData.address}</p>` : ''}
          ${orderData.observations ? `<p><strong>Tus notas:</strong><br>${orderData.observations}</p>` : ''}
          
          <p>Estamos procesando tu pedido y te mantendremos informado sobre su estado.</p>
        </div>
        <div class="footer">
          <p>Saludos,<br>Tu equipo de NegocioListo</p>
        </div>
      </div>
    </body>
    </html>
  `;
  
  return html;
}

/**
 * 💰 FORMATEAR MONTO EN CLP
 */
function formatClp(amount) {
  return `$${Math.round(amount).toLocaleString('es-CL')}`;
}

/**
 * 📅 FORMATEAR FECHA
 */
function formatDate(dateString) {
  if (!dateString) return 'N/A';
  
  try {
    const date = dateString.toDate ? dateString.toDate() : new Date(dateString);
    return date.toLocaleString('es-CL', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return dateString.toString();
  }
}

/**
 * 📊 OBTENER MENSAJE DE ESTADO
 */
function getStatusMessage(status) {
  const messages = {
    'PENDING_CLIENT_APPROVAL': 'Tu pedido está pendiente de aprobación. Por favor aprueba para continuar.',
    'PENDING_BUSINESS_APPROVAL': 'Tu pedido está pendiente de aprobación del negocio. Te notificaremos cuando sea procesado.',
    'APPROVED': '¡Tu pedido ha sido aprobado y está listo para iniciar la producción!',
    'IN_PRODUCTION': 'Tu pedido está en producción. Pronto estará listo.',
    'READY_FOR_DELIVERY': 'Tu pedido está listo para entrega.',
    'DELIVERED': 'Tu pedido ha sido entregado.',
    'CANCELLED': 'Tu pedido ha sido cancelado.'
  };
  
  return messages[status] || 'Estado del pedido: ' + status;
}

/**
 * 💬 FUNCIÓN: ENVIAR NOTIFICACIÓN FCM CUANDO CLIENTE ENVÍA MENSAJE
 * 
 * Se ejecuta automáticamente cuando un cliente envía un mensaje desde el portal web.
 * Ruta: customers/{customerId}/messages/{messageId}
 * 
 * Envía una notificación push al negocio para que sepa que hay un nuevo mensaje.
 */
exports.onChatMessageCreated = onDocumentCreated(
  {
    document: 'customers/{customerId}/messages/{messageId}',
    region: 'us-central1',
  },
  async (event) => {
    const messageData = event.data.data();
    const customerId = event.params.customerId;
    const messageId = event.params.messageId;
    
    console.log(`💬 Nuevo mensaje de chat: ${messageId} del cliente ${customerId}`);
    
    try {
      // Solo procesar mensajes del cliente (no del negocio)
      if (messageData.senderType !== 'CLIENT') {
        console.log(`ℹ️ Mensaje del negocio, no se envía notificación`);
        return null;
      }
      
      // Obtener datos del cliente para obtener el userId del negocio
      const customerDoc = await admin.firestore()
        .collection('customers')
        .doc(customerId)
        .get();
      
      let userId = null;
      let customerData = null;
      
      if (customerDoc.exists) {
        customerData = customerDoc.data();
        userId = customerData.userId;
      }
      
      // Si el cliente no tiene userId, buscarlo en las colecciones asociadas
      if (!userId) {
        console.log(`🔍 Cliente no tiene userId, buscando en colecciones asociadas...`);
        
        // Opción 1: Buscar en la colección del mensaje si existe
        if (messageData.collectionId) {
          console.log(`   Buscando en colección del mensaje: ${messageData.collectionId}`);
          const collectionDoc = await admin.firestore()
            .collection('collections')
            .doc(messageData.collectionId)
            .get();
          
          if (collectionDoc.exists) {
            const collectionData = collectionDoc.data();
            userId = collectionData.userId;
            if (userId) {
              console.log(`✅ userId encontrado en colección del mensaje: ${userId}`);
            }
          }
        }
        
        // Opción 2: Si aún no hay userId, buscar en colecciones que tienen este cliente asociado
        if (!userId) {
          console.log(`   Buscando en colecciones con cliente asociado...`);
          const collectionsSnapshot = await admin.firestore()
            .collection('collections')
            .where('associatedCustomerIds', 'array-contains', customerId)
            .limit(1)
            .get();
          
          if (!collectionsSnapshot.empty) {
            const collectionDoc = collectionsSnapshot.docs[0];
            const collectionData = collectionDoc.data();
            userId = collectionData.userId;
            if (userId) {
              console.log(`✅ userId encontrado en colección asociada: ${userId} (${collectionDoc.id})`);
            }
          }
        }
        
        // Actualizar el cliente con el userId encontrado para futuras notificaciones
        if (userId && customerDoc.exists) {
          try {
            await admin.firestore()
              .collection('customers')
              .doc(customerId)
              .update({ userId: userId });
            console.log(`✅ Cliente actualizado con userId: ${userId}`);
          } catch (error) {
            console.log(`⚠️ Error actualizando cliente con userId: ${error.message}`);
          }
        }
      }
      
      if (!userId) {
        console.log(`⚠️ No se pudo encontrar userId para cliente ${customerId}`);
        return null;
      }
      
      // Si no tenemos customerData, obtenerlo de nuevo
      if (!customerData && customerDoc.exists) {
        customerData = customerDoc.data();
      }
      
      // Obtener token FCM del usuario del negocio
      const userDoc = await admin.firestore()
        .collection('users')
        .doc(userId)
        .get();
      
      if (!userDoc.exists) {
        console.log(`⚠️ Usuario ${userId} no encontrado`);
        return null;
      }
      
      const userData = userDoc.data();
      const fcmToken = userData.fcmToken;
      
      if (!fcmToken) {
        console.log(`⚠️ Usuario ${userId} no tiene token FCM registrado`);
        return null;
      }
      
      // Preparar datos de la notificación
      const customerName = customerData.name || 'Cliente';
      const messageText = messageData.message || 'Nuevo mensaje';
      const collectionId = messageData.collectionId || null;
      
      // Construir payload de la notificación
      const message = {
        token: fcmToken,
        notification: {
          title: `💬 Mensaje de ${customerName}`,
          body: messageText.length > 100 ? messageText.substring(0, 100) + '...' : messageText,
        },
        data: {
          type: 'chat',
          customerId: customerId,
          customerName: customerName,
          messageId: messageId,
          message: messageText,
          collectionId: collectionId || '',
        },
        android: {
          priority: 'high',
          notification: {
            channelId: 'chat_notifications',
            sound: 'default',
            icon: 'ic_notification',
          },
        },
        apns: {
          payload: {
            aps: {
              sound: 'default',
              badge: 1,
            },
          },
        },
      };
      
      // Enviar notificación FCM
      const response = await admin.messaging().send(message);
      console.log(`✅ Notificación FCM enviada exitosamente: ${response}`);
      
      return null;
    } catch (error) {
      console.error(`❌ Error enviando notificación FCM: ${error.message}`);
      console.error(`   Stack: ${error.stack}`);
      // No lanzar error para evitar que la función falle completamente
      return null;
    }
  });

