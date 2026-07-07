---
order: 140
sidebar: false
---

# Live Progress Dashboard

Track active mastery over the SDE-2 Backend curriculum. This data is pulled from the syllabus matrices in `todo/study_plan/parts/*.md` whenever the site builds.

<script setup>
import { data } from '../progress.data.mjs'
</script>

<ProgressDashboard :data="data" />
