package com.passioagogo.market.ui.catalog.browse

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.passioagogo.market.domain.catalog.CatalogRepository
import com.passioagogo.market.domain.catalog.Category
import com.passioagogo.market.domain.catalog.ProductWithVariants
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

private val moneda: NumberFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

data class ProductDetailUiState(
    val producto: ProductWithVariants? = null,
    val categoria: Category? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            catalogRepository.observeProduct(productId).collect { producto ->
                val categorias = catalogRepository.observeCategories(includeInactive = true).first()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        producto = producto,
                        categoria = categorias.firstOrNull {
                            c -> c.id == producto?.product?.categoryId
                        },
                    )
                }
            }
        }
    }
}

/** Copia al portapapeles con aviso: el gesto es pulsación larga. */
private fun copiar(context: Context, etiqueta: String, texto: String) {
    if (texto.isBlank()) return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(etiqueta, texto))
    Toast.makeText(context, "$etiqueta copiado", Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    onAddToCart: ((variantId: String) -> Unit)? = null,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val pw = state.producto ?: run {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Producto no encontrado") }
        return
    }
    val producto = pw.product

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ---------- Imágenes ----------
        if (producto.imagenes.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(producto.imagenes) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(240.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        CampoCopiable("Producto", producto.nombre, MaterialTheme.typography.headlineSmall)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            state.categoria?.let { AssistChip(onClick = {}, label = { Text(it.nombre) }) }
            if (producto.sobrePedido) {
                AssistChip(
                    onClick = {},
                    label = { Text("Sobre pedido") },
                )
            }
        }

        producto.marca?.let { CampoCopiable("Marca", it) }
        producto.resumen?.let { CampoCopiable("Resumen", it) }
        producto.descripcion?.let { CampoCopiable("Descripción", it) }

        if (producto.attributes.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Características", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                producto.attributes.forEach { (clave, valor) ->
                    AssistChip(
                        onClick = {
                            copiar(context, clave, valor.jsonPrimitive.content)
                        },
                        label = { Text("$clave: ${valor.jsonPrimitive.content}") },
                    )
                }
            }
        }

        // ---------- Variantes ----------
        Spacer(Modifier.height(16.dp))
        Text("Presentaciones", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        pw.variants.forEach { variante ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                moneda.format(variante.precioVenta),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                variante.sku,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.combinedClickable(
                                    onClick = {},
                                    onLongClick = { copiar(context, "SKU", variante.sku) },
                                ),
                            )
                        }
                        onAddToCart?.let { agregar ->
                            androidx.compose.material3.Button(
                                onClick = { agregar(variante.id) },
                            ) { Text("Agregar") }
                        }
                    }
                    if (variante.attributes.isNotEmpty()) {
                        Text(
                            variante.attributes.entries.joinToString(" · ") { (k, v) ->
                                "$k: ${v.jsonPrimitive.content}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (pw.variants.isEmpty()) {
            Text(
                "Este producto aún no tiene presentaciones disponibles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Mantén pulsado cualquier dato para copiarlo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CampoCopiable(
    etiqueta: String,
    valor: String,
    estilo: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { copiar(context, etiqueta, valor) },
            )
            .padding(vertical = 6.dp)
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(valor, style = estilo)
    }
    HorizontalDivider()
}
