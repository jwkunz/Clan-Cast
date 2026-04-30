package com.privatefamily.broadcastsms.ui.navigation

object Routes {
    const val Splash = "splash"
    const val Home = "home"
    const val Lists = "lists"
    const val ListDetail = "listDetail"
    const val ContactPicker = "contactPicker"
    const val Templates = "templates"
    const val ComposeBroadcast = "composeBroadcast"
    const val Preflight = "preflight"
    const val History = "history"
    const val BroadcastDetail = "broadcastDetail"

    fun listDetail(listId: String): String = "$ListDetail/$listId"
    fun contactPicker(listId: String): String = "$ContactPicker/$listId"
    fun broadcastDetail(broadcastId: String): String = "$BroadcastDetail/$broadcastId"
}
