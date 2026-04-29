package com.privatefamily.broadcastsms.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatefamily.broadcastsms.data.model.MessageTemplateEntity
import com.privatefamily.broadcastsms.data.repository.TemplateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplatesViewModel(
    private val repository: TemplateRepository,
) : ViewModel() {
    val templates: StateFlow<List<MessageTemplateEntity>> = repository.observeTemplates().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun createTemplate(name: String, body: String) {
        viewModelScope.launch {
            repository.createTemplate(name, body)
        }
    }

    fun updateTemplate(template: MessageTemplateEntity, name: String, body: String) {
        viewModelScope.launch {
            repository.updateTemplate(template, name, body)
        }
    }

    fun deleteTemplate(template: MessageTemplateEntity) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }
}

class TemplatesViewModelFactory(
    private val repository: TemplateRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TemplatesViewModel(repository) as T
    }
}
