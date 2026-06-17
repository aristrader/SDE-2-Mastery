import DefaultTheme from 'vitepress/theme'
import CodePlayground from './components/CodePlayground.vue'
import './custom.css'

export default {
  ...DefaultTheme,
  enhanceApp({ app }) {
    app.component('CodePlayground', CodePlayground)
  }
}
