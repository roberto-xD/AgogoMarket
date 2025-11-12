package com.passioagogo.market.presentation.viewModel.imagenes

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passioagogo.market.domain.bean.ImagenProducto
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
    val images: List<ImagenProducto> = emptyList(),
    val deleteImage: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val mensajeExito: String? = null,
)

@HiltViewModel
class ImageGalleryViewModel @Inject constructor(
    private val saveSharedImageUseCase: SaveSharedImageUseCase,
    private val deleteImageUseCase: DeleteImageUseCase,
    private val getAllImagesUseCase: GetAllImagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageGalleryUiState())
    val uiState: StateFlow<ImageGalleryUiState> = _uiState.asStateFlow()
    val showBottomSheet: MutableState<Boolean> = mutableStateOf(false)

    fun saveSharedImages(uris: List<Uri>, id: Long? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val results = uris.map { uri ->
                saveSharedImageUseCase.execute(uri,id)
            }

            val success = results.filter { it.isSuccess }.map { result ->
                result.fold(
                    onSuccess = {
                        ImagenProducto(
                            productoId = id ?: 0,
                            rutaImagen = it,
                            orden = results.indexOf(result)
                        )
                                },
                    onFailure = { null }
                )
            }

            _uiState.update {
                it.copy(
                    images = success.filterNotNull(),
                    isLoading = false,
                    mensajeExito = "Se guardaron ${success.size} imagen(es)",
                )
            }
        }
    }

    fun deleteImage(imageModel: ImagenProducto) {
        viewModelScope.launch {
            deleteImageUseCase.execute(
                imageModel = imageModel
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            deleteImage = imageModel.rutaImagen,
                            isLoading = false,
                            mensajeExito = "Imagen borrada",
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
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearPaths(){
        _uiState.update { it.copy(images = emptyList()) }
    }

    fun loadImages() {
        viewModelScope.launch {
            getAllImagesUseCase.execute().let { images ->
                _uiState.update {
                    it.copy(
                        images = images ?: emptyList()
                    )
                }
            }
        }
    }
}
