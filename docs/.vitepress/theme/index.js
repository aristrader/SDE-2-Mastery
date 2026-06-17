import DefaultTheme from 'vitepress/theme'
import CodePlayground from './components/CodePlayground.vue'

export default {
  ...DefaultTheme,
  enhanceApp({ app }) {
    app.component('CodePlayground', CodePlayground)
  }
}
