'use client'

import { motion } from 'framer-motion'
import { Github, Download, Mail } from 'lucide-react'

export default function Footer() {
  return (
    <footer className="bg-gradient-to-r from-primary to-secondary text-white py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Logo y Descripción */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
            className="flex flex-col items-center md:items-start"
          >
            <div className="flex items-center space-x-3 mb-4">
              <img 
                src="/icon-negociolisto.png" 
                alt="NegocioListo" 
                className="w-12 h-12 object-contain"
              />
              <span className="text-2xl font-bold">NegocioListo</span>
            </div>
            <p className="text-white/80 text-sm text-center md:text-left">
              Gestión empresarial completa para emprendedores
            </p>
          </motion.div>

          {/* Links */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="flex flex-col items-center md:items-start space-y-4"
          >
            <h3 className="text-lg font-semibold mb-2">Enlaces</h3>
            <a
              href="https://github.com/DonGeeo87/Negocio-Listo-Android-App"
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center space-x-2 text-white/80 hover:text-white transition-colors"
            >
              <Github className="w-5 h-5" />
              <span>Repositorio GitHub</span>
            </a>
            <a
              href="#"
              className="flex items-center space-x-2 text-white/80 hover:text-white transition-colors"
            >
              <Download className="w-5 h-5" />
              <span>Descargar App</span>
            </a>
          </motion.div>

          {/* Información del Desarrollador */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="flex flex-col items-center md:items-start"
          >
            <h3 className="text-lg font-semibold mb-2">Desarrollador</h3>
            <p className="text-white/80 text-sm mb-2">
              Giorgio Interdonato Palacios
            </p>
            <a
              href="https://github.com/DonGeeo87"
              target="_blank"
              rel="noopener noreferrer"
              className="text-white/80 hover:text-white transition-colors text-sm"
            >
              @DonGeeo87
            </a>
          </motion.div>
        </div>

        {/* Copyright */}
        <motion.div
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6, delay: 0.6 }}
          className="mt-8 pt-8 border-t border-white/20 text-center"
        >
          <p className="text-white/60 text-sm">
            © {new Date().getFullYear()} NegocioListo. Desarrollado con ❤️ por un Emprendedor para Emprendedores
          </p>
        </motion.div>
      </div>
    </footer>
  )
}

