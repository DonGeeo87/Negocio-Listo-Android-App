/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'export',
  distDir: 'out',
  images: {
    unoptimized: true,
  },
  trailingSlash: false,
  basePath: '/presentacion-app',
  assetPrefix: '/presentacion-app',
}

module.exports = nextConfig

