module.exports = {
  env: {
    es6: true,
    node: true,
  },
  parserOptions: {
    "ecmaVersion": 2020,
    "sourceType": "module",
  },
  extends: [
    "eslint:recommended",
    "google",
  ],
  rules: {
    "no-restricted-globals": ["error", "name", "length"],
    "prefer-arrow-callback": "error",
    "quotes": ["error", "double", {"allowTemplateLiterals": true}],
    "linebreak-style": "off", // Desactivar para Windows
    "max-len": ["error", {"code": 120}], // Aumentar límite de línea
    "require-jsdoc": "off", // Desactivar requerimiento de JSDoc
    "valid-jsdoc": "off", // Desactivar validación de JSDoc
    "indent": ["error", 2], // Usar 2 espacios en lugar de 4
    "comma-dangle": ["error", "always-multiline"], // Requerir comas finales
  },
  overrides: [
    {
      files: ["**/*.spec.*"],
      env: {
        mocha: true,
      },
      rules: {},
    },
  ],
  globals: {},
};

