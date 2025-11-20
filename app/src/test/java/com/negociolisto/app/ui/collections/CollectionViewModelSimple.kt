package com.negociolisto.app.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.negociolisto.app.data.CollectionRepositoryMock
import com.negociolisto.app.domain.model.Collection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModelSimple : ViewModel() {
    val collections: StateFlow<List<Collection>> = CollectionRepositoryMock
        .getCollections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCollection(collection: Collection) {
        viewModelScope.launch { CollectionRepositoryMock.addCollection(collection) }
    }

    fun updateCollection(collection: Collection) {
        viewModelScope.launch { CollectionRepositoryMock.updateCollection(collection) }
    }

    fun deleteCollection(id: String) {
        viewModelScope.launch { CollectionRepositoryMock.deleteCollection(id) }
    }
}


