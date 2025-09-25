package com.passioagogo.market.presentation.viewModel.imagenes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.usecase.imagenes.DeleteImageUseCase
import com.passioagogo.market.domain.usecase.imagenes.GetAllImagesUseCase
import com.passioagogo.market.domain.usecase.imagenes.SaveSharedImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageGalleryUiState(
    val images: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedImageForPreview: String? = null
)

@HiltViewModel
class ImageGalleryViewModel @Inject constructor(
    private val saveSharedImageUseCase: SaveSharedImageUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val getAllImagesUseCase: GetAllImagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageGalleryUiState())
    val uiState: StateFlow<ImageGalleryUiState> = _uiState.asStateFlow()

    private val _actualFilePaths = MutableStateFlow<List<String>>(emptyList())
    val actualFilePaths: StateFlow<List<String>> = _actualFilePaths.asStateFlow()

    init {
        loadImages()
    }

    fun saveSharedImages(uris: List<Uri>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val results = uris.map { uri ->
                saveSharedImageUseCase.execute(uri)
            }

            val failures = results.filter { it.isFailure }
            _actualFilePaths.value = results.filter { it.isSuccess }.map { result -> result.fold(onSuccess = { it }, onFailure = { "" }) }

            _uiState.update {
                it.copy(
                    images = actualFilePaths.value,
                    isLoading = false,
                    errorMessage = "Error al guardar ${failures.size} imagen(es)"
                )
            }
        }
    }

    fun deleteImage(imagePath: String) {
        viewModelScope.launch {
            deleteImageUseCase.execute(imagePath).fold(
                onSuccess = {
                    _actualFilePaths.value = _actualFilePaths.value.filter { it != imagePath }
                    _uiState.update {
                        it.copy(
                            images = actualFilePaths.value,
                            isLoading = false,
                            errorMessage = "Error al borrar imagen"
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(errorMessage = "Error al eliminar imagen: ${exception.message}")
                    }
                }
            )
        }
    }

    fun showImagePreview(imagePath: String) {
        _uiState.update { it.copy(selectedImageForPreview = imagePath) }
    }

    fun hideImagePreview() {
        _uiState.update { it.copy(selectedImageForPreview = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadImages() {
        viewModelScope.launch {
            getAllImagesUseCase.execute()?.collect { images ->
                _uiState.update { it.copy(images = images) }
            }
        }
    }
}
