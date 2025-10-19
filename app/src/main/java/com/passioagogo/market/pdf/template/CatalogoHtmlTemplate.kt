package com.passioagogo.market.pdf.template

object CatalogoHtmlTemplate {

    fun generatePortada(catalogo: com.passioagogo.market.domain.model.CatalogoCompleto): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                @page {
                    size: A4;
                    margin: 0;
                }
                
                body {
                    margin: 0;
                    padding: 0;
                    font-family: 'Arial', sans-serif;
                }
                
                .portada {
                    width: 100%;
                    height: 297mm;
                    background: linear-gradient(135deg, #7B2CBF 0%, #9D4EDD 50%, #7B2CBF 100%);
                    position: relative;
                    overflow: hidden;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    page-break-after: always;
                }
                
                .portada::before {
                    content: '';
                    position: absolute;
                    width: 600px;
                    height: 600px;
                    background: rgba(255, 255, 255, 0.05);
                    border-radius: 50%;
                    top: -200px;
                    left: -200px;
                }
                
                .portada::after {
                    content: '';
                    position: absolute;
                    width: 800px;
                    height: 800px;
                    background: rgba(255, 255, 255, 0.05);
                    border-radius: 50%;
                    bottom: -300px;
                    right: -300px;
                }
                
                .logo-container {
                    text-align: center;
                    z-index: 10;
                    color: white;
                    margin-bottom: 40px;
                }
                
                .logo-abanico {
                    font-size: 80px;
                    margin-bottom: 20px;
                }
                
                .logo-texto {
                    font-family: 'Brush Script MT', cursive;
                    font-size: 72px;
                    font-weight: normal;
                    margin: 0;
                    text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                }
                
                .subtitulo {
                    font-size: 16px;
                    letter-spacing: 4px;
                    text-transform: uppercase;
                    margin-top: 10px;
                }
                
                .catalogo-titulo {
                    font-size: 64px;
                    letter-spacing: 12px;
                    font-weight: 300;
                    margin: 40px 0 20px 0;
                    text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                }
                
                .catalogo-ano {
                    font-size: 96px;
                    font-weight: bold;
                    color: white;
                    text-shadow: 4px 4px 8px rgba(0,0,0,0.5);
                    letter-spacing: 8px;
                }
            </style>
        </head>
        <body>
            <div class="portada">
                <div class="logo-container">
                    <div class="logo-abanico">🎭</div>
                    <h1 class="logo-texto">Passion à gogo</h1>
                    <p class="subtitulo">${catalogo.subtitulo}</p>
                </div>
                <h2 class="catalogo-titulo">C A T Á L O G O</h2>
                <div class="catalogo-ano">2025</div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateProductosPage(
        categoria: com.passioagogo.market.domain.model.CategoriaCatalogo,
        productos: List<com.passioagogo.market.domain.model.ProductoCatalogo>
    ): String {
        val productosHtml = productos.joinToString("") { producto ->
            generateProductoCard(producto)
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                @page {
                    size: A4;
                    margin: 20mm;
                }
                
                body {
                    margin: 0;
                    padding: 0;
                    font-family: 'Arial', sans-serif;
                    background: linear-gradient(to bottom, #f8f4ff 0%, #ffffff 100%);
                }
                
                .categoria-header {
                    text-align: center;
                    padding: 30px 0;
                    background: white;
                    border-radius: 50px;
                    margin-bottom: 30px;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                }
                
                .categoria-nombre {
                    font-size: 28px;
                    font-weight: bold;
                    color: #7B2CBF;
                    display: inline-block;
                    margin: 0;
                }
                
                .categoria-icono {
                    font-size: 32px;
                    margin-right: 10px;
                }
                
                .productos-grid {
                    display: grid;
                    grid-template-columns: repeat(2, 1fr);
                    gap: 20px;
                    padding: 0;
                }
                
                .producto-card {
                    background: white;
                    border-radius: 20px;
                    padding: 20px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                    page-break-inside: avoid;
                    min-height: 400px;
                }
                
                .producto-imagen {
                    width: 100%;
                    height: 200px;
                    object-fit: contain;
                    margin-bottom: 15px;
                }
                
                .producto-nombre {
                    font-size: 18px;
                    font-weight: bold;
                    color: #333;
                    margin: 10px 0;
                    min-height: 45px;
                }
                
                .producto-descripcion {
                    font-size: 12px;
                    color: #666;
                    line-height: 1.4;
                    margin: 10px 0;
                    min-height: 60px;
                }
                
                .producto-precio {
                    font-size: 28px;
                    font-weight: bold;
                    color: white;
                    background: linear-gradient(135deg, #4A148C, #7B2CBF);
                    padding: 10px 20px;
                    border-radius: 15px;
                    display: inline-block;
                    margin: 10px 0;
                }
                
                .atributos-container {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 8px;
                    margin-top: 15px;
                }
                
                .atributo {
                    display: flex;
                    align-items: center;
                    font-size: 11px;
                    color: #7B2CBF;
                    background: #f3e5ff;
                    padding: 6px 12px;
                    border-radius: 12px;
                    gap: 5px;
                }
                
                .atributo-icono {
                    font-size: 14px;
                }
                
                .atributo-nombre {
                    font-weight: 600;
                }
            </style>
        </head>
        <body>
            <div class="categoria-header">
                <h2 class="categoria-nombre">
                    ${if (categoria.icono != null) "<span class=\"categoria-icono\">${categoria.icono}</span>" else ""}
                    ${categoria.nombre}
                </h2>
            </div>
            <div class="productos-grid">
                $productosHtml
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun generateProductoCard(producto: com.passioagogo.market.domain.model.ProductoCatalogo): String {
        val atributosHtml = producto.atributos.take(6).joinToString("") { atributo ->
            """
            <div class="atributo">
                ${if (atributo.icono != null) "<span class=\"atributo-icono\">${atributo.icono}</span>" else ""}
                <span class="atributo-nombre">${atributo.nombre}</span>
            </div>
            """
        }

        val imagenTag = if (producto.imagenPrincipal != null) {
            """<img src="${producto.imagenPrincipal}" class="producto-imagen" alt="${producto.nombre}" />"""
        } else {
            """<div class="producto-imagen" style="background: #f0f0f0; display: flex; align-items: center; justify-content: center;">
                <span style="font-size: 48px; color: #ccc;">📦</span>
            </div>"""
        }

        return """
        <div class="producto-card">
            $imagenTag
            <h3 class="producto-nombre">${producto.nombre}</h3>
            <p class="producto-descripcion">${producto.descripcion ?: ""}</p>
            <div class="producto-precio">$${"%.2f".format(producto.precio)}</div>
            <div class="atributos-container">
                $atributosHtml
            </div>
        </div>
        """
    }

    fun generateFichaProducto(producto: com.passioagogo.market.domain.model.ProductoCatalogo): String {
        val atributosHtml = producto.atributos.joinToString("") { atributo ->
            """
            <tr>
                <td class="atributo-label">
                    ${if (atributo.icono != null) "${atributo.icono} " else ""}
                    ${atributo.nombre}
                </td>
                <td class="atributo-valor">${atributo.valor}</td>
            </tr>
            """
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                @page {
                    size: A4;
                    margin: 20mm;
                }
                
                body {
                    margin: 0;
                    padding: 0;
                    font-family: 'Arial', sans-serif;
                }
                
                .ficha-header {
                    background: linear-gradient(135deg, #7B2CBF, #9D4EDD);
                    color: white;
                    padding: 30px;
                    text-align: center;
                    border-radius: 20px;
                    margin-bottom: 30px;
                }
                
                .ficha-titulo {
                    font-size: 32px;
                    margin: 0;
                }
                
                .ficha-content {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 30px;
                }
                
                .imagen-container {
                    text-align: center;
                }
                
                .producto-imagen-grande {
                    width: 100%;
                    max-height: 400px;
                    object-fit: contain;
                    border-radius: 15px;
                    box-shadow: 0 8px 16px rgba(0,0,0,0.1);
                }
                
                .info-container {
                    padding: 20px;
                }
                
                .producto-nombre-ficha {
                    font-size: 28px;
                    font-weight: bold;
                    color: #333;
                    margin-bottom: 15px;
                }
                
                .producto-descripcion-ficha {
                    font-size: 14px;
                    color: #666;
                    line-height: 1.6;
                    margin-bottom: 20px;
                }
                
                .precio-ficha {
                    font-size: 42px;
                    font-weight: bold;
                    color: #7B2CBF;
                    margin: 20px 0;
                }
                
                .atributos-tabla {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 20px;
                }
                
                .atributos-tabla tr {
                    border-bottom: 1px solid #e0e0e0;
                }
                
                .atributo-label {
                    padding: 12px;
                    font-weight: 600;
                    color: #7B2CBF;
                    width: 50%;
                }
                
                .atributo-valor {
                    padding: 12px;
                    color: #333;
                }
            </style>
        </head>
        <body>
            <div class="ficha-header">
                <h1 class="ficha-titulo">Ficha Técnica</h1>
            </div>
            
            <div class="ficha-content">
                <div class="imagen-container">
                    ${if (producto.imagenPrincipal != null) {
            """<img src="${producto.imagenPrincipal}" class="producto-imagen-grande" alt="${producto.nombre}" />"""
        } else {
            """<div class="producto-imagen-grande" style="background: #f0f0f0; display: flex; align-items: center; justify-content: center;">
                            <span style="font-size: 120px; color: #ccc;">📦</span>
                        </div>"""
        }}
                </div>
                
                <div class="info-container">
                    <h2 class="producto-nombre-ficha">${producto.nombre}</h2>
                    <p class="producto-descripcion-ficha">${producto.descripcion ?: "Sin descripción"}</p>
                    <div class="precio-ficha">$${"%.2f".format(producto.precio)}</div>
                    
                    <table class="atributos-tabla">
                        <tbody>
                            $atributosHtml
                        </tbody>
                    </table>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}