'use client'

import { useState } from 'react'
import Hero from '@/components/sections/Hero'
import TechStack from '@/components/sections/TechStack'
import Architecture from '@/components/sections/Architecture'
import Features from '@/components/sections/Features'
import HowItWorks from '@/components/sections/HowItWorks'
import Navigation from '@/components/Navigation'
import Footer from '@/components/Footer'
import ScrollToTop from '@/components/ScrollToTop'

export default function Home() {
  const [activeSection, setActiveSection] = useState<string>('hero')

  return (
    <main className="min-h-screen">
      <Navigation activeSection={activeSection} setActiveSection={setActiveSection} />
      
      <div className="space-y-0">
        <section id="hero">
          <Hero onScroll={() => setActiveSection('tech')} />
        </section>
        
        <section id="tech">
          <TechStack />
        </section>
        
        <section id="architecture">
          <Architecture />
        </section>
        
        <section id="features">
          <Features />
        </section>
        
        <section id="how-it-works">
          <HowItWorks />
        </section>
      </div>
      
      <Footer />
      <ScrollToTop />
    </main>
  )
}

