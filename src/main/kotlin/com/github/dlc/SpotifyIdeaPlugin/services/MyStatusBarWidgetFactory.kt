package com.github.dlc.SpotifyIdeaPlugin.services

import com.github.dlc.SpotifyIdeaPlugin.ui.SpotifyPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.Consumer
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.*


class MyStatusBarWidgetFactory : StatusBarWidgetFactory {
    private var statusUpdaterThread: Thread? = null
    private var spotifyStatusUpdater: SpotifyStatusUpdater? = null
    private lateinit var PluginWidget: StatusBarWidget
    private val name = "Spotify-IDEA-Player"

    override fun getId(): String {
        return name
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun isAvailable(project: Project): Boolean {
        return true
    }

    override fun createWidget(project: Project): StatusBarWidget {
        PluginWidget = object : StatusBarWidget {

            override fun dispose() {
                spotifyStatusUpdater!!.stop()
                statusUpdaterThread!!.interrupt()
            }

            override fun ID(): String {
                return name
            }

            override fun install(statusBar: StatusBar) {
                spotifyStatusUpdater = SpotifyStatusUpdater(statusBar)
                statusUpdaterThread = Thread(spotifyStatusUpdater)
                statusUpdaterThread!!.start()
            }

            override fun getPresentation(): StatusBarWidget.WidgetPresentation? {
                return object : StatusBarWidget.MultipleTextValuesPresentation {
                    override fun getTooltipText(): String? {
                        return "Spotify-IDEA-Player"
                    }

                    override fun getClickConsumer(): Consumer<MouseEvent>? {
                        return Consumer {
                            SpotifyService.getCodeFromBrowser()
                        }
                    }

                    override fun getPopupStep(): ListPopup? {
                        kotlin.runCatching {

                            val spotifyPanel = SpotifyPanel(spotifyStatusUpdater!!)
                            SpotifyService.currentPanel = spotifyPanel
                            val popup =
                                JBPopupFactory.getInstance().createComponentPopupBuilder(spotifyPanel, spotifyPanel)
                                    .setRequestFocus(true)
                                    .setCancelOnClickOutside(true)
                                    .createPopup()
                            val mouseX = MouseInfo.getPointerInfo().location.getX()
                            val mouseY = MouseInfo.getPointerInfo().location.getY()
                            popup.show(RelativePoint(Point(mouseX.toInt(), mouseY.toInt())))
                            popup.setLocation(
                                Point(
                                    (mouseX - spotifyPanel.width / 2).toInt(),
                                    (mouseY - spotifyPanel.height * 1.05).toInt()
                                )
                            )
                        }.onFailure { e ->
                            e.printStackTrace()
                        }
                        return null
                    }

                    override fun getSelectedValue(): String? {
                        return SpotifyService.title.isNotEmpty().let {
                            if (it) {
                                " " + SpotifyService.title
                            } else {
                                " No song playing"
                            }
                        }
                    }

                    override fun getIcon(): Icon? {
                        return spotifyStatusUpdater?.currentIcon ?: IconLoader.getIcon(
                            "/icons/spotify-inactive.svg",
                            this::class.java
                        )
                    }
                }
            }
        }
        return PluginWidget
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        spotifyStatusUpdater!!.stop()
        statusUpdaterThread!!.interrupt()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean {
        return true
    }
}