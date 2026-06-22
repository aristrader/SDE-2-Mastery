---
sidebar: false
---

# Live Progress Dashboard

Track your active mastery over the SDE-2 Backend curriculum. This data is pulled live from your syllabus matrices (`todo/study_plan/parts/*.md`) every time the site builds.

<script setup>
import { data } from './progress.data.mjs'
</script>

<ProgressDashboard :data="data" />
