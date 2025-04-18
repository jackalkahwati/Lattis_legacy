package com.lattis.lattis.presentation.base

interface DataView<ActionListenerType> {
    fun showLoading(hideContent: Boolean)
    fun hideLoading()
    fun setToolbarHeader(title: String)
    fun setToolbarDescription(subtitle: String)
    fun hideToolbar()
}