package com.github.dlc.SpotifyIdeaPlugin.services

import com.github.dlc.SpotifyIdeaPlugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
