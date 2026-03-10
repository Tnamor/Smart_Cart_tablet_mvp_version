package com.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.smartcart.R
import com.smartcart.data.model.CategoryItem
import com.smartcart.data.model.MockData
import com.smartcart.data.model.Product
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.CartPanel
import com.smartcart.ui.components.SharedSidebar
import com.smartcart.ui.components.SharedTopBar
import com.smartcart.ui.theme.*

@Composable
fun CategoriesScreen(
    onNavigateHome: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateWishlist: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    val t = AppState.t()
    val lang = AppState.language
    val categories = MockData.categories
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryItem?>(null) }

    val categoryToProductCategories = mapOf(
        "produce" to listOf("Fruits", "Vegetables", "Produce"),
        "dairy" to listOf("Dairy"),
        "meat" to listOf("Meat"),
        "bakery" to listOf("Bakery"),
        "beverages" to listOf("Beverages"),
        "household" to listOf("Grocery", "Pantry"),
        "snacks" to listOf("Grocery", "Pantry"),
        "frozen" to listOf("Grocery"),
    )
    val filteredProducts = remember(selectedCategory, searchQuery, MockData.products) {
        val list = MockData.products
        val byCat = selectedCategory?.let { cat ->
            val allowed = categoryToProductCategories[cat.id] ?: listOf(cat.id, cat.nameEn)
            list.filter { p -> p.category in allowed || allowed.isEmpty() }
        } ?: list
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            byCat.filter {
                it.localizedName(lang).lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            }
        } else byCat
    }

    Row(Modifier.background(Background).fillMaxSize()) {
        SharedSidebar(
            activeRoute = "cats",
            onNavigate = { r ->
                when (r) {
                    "home" -> onNavigateHome()
                    "list" -> onNavigateToList()
                    "favs" -> onNavigateWishlist()
                }
            }
        )
        Column(Modifier.weight(1f).fillMaxSize()) {
            SharedTopBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })
            Column(
                Modifier.weight(1f).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    t.catsHeader,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (selectedCategory != null) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "${t.catsHeader} > ${selectedCategory!!.localizedName(lang)}",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            t.filterAll,
                            fontSize = 12.sp,
                            color = Primary,
                            modifier = Modifier.clickable { selectedCategory = null }
                        )
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredProducts) { product ->
                            CategoryProductCard(
                                product = product,
                                onAdd = { AppState.addToCart(product) }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(categories) { cat ->
                            CategoryCard(
                                category = cat,
                                lang = lang,
                                t = t,
                                onClick = { selectedCategory = cat }
                            )
                        }
                    }
                }
            }
        }
        CartPanel(onCheckout = onNavigateToCart)
    }
}

@Composable
private fun CategoryCard(
    category: CategoryItem,
    lang: com.smartcart.data.model.AppLanguage,
    t: com.smartcart.data.model.AppStrings,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(category.color).copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(category.color).copy(alpha = 0.5f))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(category.emoji, fontSize = 28.sp)
                Text(
                    category.localizedName(lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(category.color)
                )
            }
            Text(
                "${category.itemCount}${t.itemsSuffix}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun CategoryProductCard(product: Product, onAdd: () -> Unit) {
    val lang = AppState.language
    val name = product.localizedName(lang)
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp).background(Gray100)) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(product.imageUrl).crossfade(true).build(),
                    placeholder = painterResource(R.drawable.product_placeholder),
                    error = painterResource(R.drawable.product_placeholder),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
                if (product.isNew) {
                    Surface(
                        modifier = Modifier.padding(10.dp).align(Alignment.TopStart),
                        shape = RoundedCornerShape(6.dp),
                        color = ErrorRed
                    ) {
                        Text(
                            "New",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    product.category,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        product.formattedPrice(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F4F6)).padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Primary))
                            Spacer(Modifier.width(6.dp))
                            Text("Auto-detected by AI camera", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
