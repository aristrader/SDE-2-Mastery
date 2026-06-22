import DefaultTheme from 'vitepress/theme'
import Layout from './Layout.vue'
import './custom.css'
import ProgressDashboard from './components/ProgressDashboard.vue'

export default {
  ...DefaultTheme,
  Layout,
  enhanceApp({ app }) {
    app.component('ProgressDashboard', ProgressDashboard)
  }
}
