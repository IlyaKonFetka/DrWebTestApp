package com.example.drwebtestapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drwebtestapp.manager.AppManager
import com.example.drwebtestapp.model.AppInfo
import com.example.drwebtestapp.model.AppDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(private val appManager: AppManager) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    
    private val _appDetails = MutableStateFlow<AppDetails?>(null)
    val appDetails: StateFlow<AppDetails?> = _appDetails.asStateFlow()
    
    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()
    
    init {
        loadApps()
    }
    
    private fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val apps = appManager.getInstalledApps()
                val currentState = _uiState.value
                val filteredApps = if (currentState.showSystemApps) {
                    apps
                } else {
                    apps.filter { !it.isSystemApp }
                }
                _uiState.value = _uiState.value.copy(
                    apps = apps,
                    filteredApps = filteredApps,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load apps: ${e.message}"
                )
            }
        }
    }
    
    fun loadAppDetails(packageName: String) {
        viewModelScope.launch {
            _isLoadingDetails.value = true
            try {
                val details = appManager.getAppDetails(packageName)
                _appDetails.value = details
            } catch (e: Exception) {
                _appDetails.value = null
            } finally {
                _isLoadingDetails.value = false
            }
        }
    }
    
    fun launchApp(packageName: String): Boolean {
        return appManager.launchApp(packageName)
    }
    
    fun filterApps(query: String) {
        val currentState = _uiState.value
        val baseApps = if (currentState.showSystemApps) {
            currentState.apps
        } else {
            currentState.apps.filter { !it.isSystemApp }
        }
        
        val filteredApps = if (query.isEmpty()) {
            baseApps
        } else {
            baseApps.filter { app ->
                app.name.contains(query, ignoreCase = true) || 
                app.packageName.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = currentState.copy(
            filteredApps = filteredApps,
            searchQuery = query
        )
    }
    
    fun toggleShowSystemApps() {
        val currentState = _uiState.value
        val newShowSystemApps = !currentState.showSystemApps
        _uiState.value = currentState.copy(showSystemApps = newShowSystemApps)
        
        // Reapply filtering with new system apps setting
        filterApps(currentState.searchQuery)
    }
    
    fun refreshApps() {
        val currentState = _uiState.value
        loadApps()
        // Reapply current search filter after loading
        if (currentState.searchQuery.isNotEmpty()) {
            filterApps(currentState.searchQuery)
        }
    }
}

data class AppUiState(
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val showSystemApps: Boolean = false
)
