import DefaultTheme from 'vitepress/theme'
import Layout from './Layout.vue'
import './custom.css'
import ProgressDashboard from './components/ProgressDashboard.vue'
import Playground from './components/Playground.vue'
import ArchitectureBoard from './components/ArchitectureBoard.vue'
import ExerciseNav from './components/ExerciseNav.vue'
import AutoTopicGrid from './components/AutoTopicGrid.vue'

export default {
  ...DefaultTheme,
  Layout,
  enhanceApp({ app }) {
    app.component('ProgressDashboard', ProgressDashboard)
    app.component('Playground', Playground)
    app.component('ArchitectureBoard', ArchitectureBoard)
    app.component('ExerciseNav', ExerciseNav)
    app.component('AutoTopicGrid', AutoTopicGrid)
  }
}
