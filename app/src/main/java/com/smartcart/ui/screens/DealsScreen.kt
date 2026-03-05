package com.smartcart.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.smartcart.data.model.Deal
import com.smartcart.data.model.MockData
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.CartPanel
import com.smartcart.ui.components.SharedSidebar
import com.smartcart.ui.components.SharedTopBar
import com.smartcart.ui.theme.*

@Composable
fun DealsScreen(
    onNavigateHome: () -> Unit,
    onNavigateCats: () -> Unit,
    onNavigateWishlist: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    val t = AppState.t()
    val lang = AppState.language
    val deals = MockData.deals
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("all") }

    val filteredDeals = remember(deals, activeFilter, searchQuery) {
        var list = deals
        if (activeFilter != "all") {
            val pct = when (activeFilter) {
                "20" -> 20
                "30" -> 30
                "50" -> 50
                "new" -> -1
                else -> null
            }
            list = if (pct == -1) list.filter { it.product.isNew }
            else if (pct != null) list.filter { it.discount == pct }
            else list
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.product.localizedName(lang).lowercase().contains(q) ||
                    it.product.category.lowercase().contains(q)
            }
        }
        list
    }

    val expiringDeals = filteredDeals.filter { it.expiresIn != null }
    val regularDeals = filteredDeals.filter { it.expiresIn == null }

    Row(Modifier.background(Background).fillMaxSize()) {
        SharedSidebar(
            activeRoute = "deals",
            onNavigate = { r ->
                when (r) {
                    "home" -> onNavigateHome()
                    "cats" -> onNavigateCats()
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
                    "🔥 ${t.dealsHeader}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "all" to t.filterAll,
                        "20" to t.filter20,
                        "30" to t.filter30,
                        "50" to t.filter50,
                        "new" to t.filterNew,
                    ).forEach { (id, label) ->
                        FilterChip(
                            label = label,
                            active = activeFilter == id,
                            onClick = { activeFilter = id }
                        )
                    }
                }
                if (expiringDeals.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            t.expiringSoon,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(expiringDeals) { deal ->
                                DealCard(deal = deal, onAdd = { AppState.addToCart(deal.product) })
                            }
                        }
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(if (expiringDeals.isEmpty()) filteredDeals else regularDeals) { deal ->
                        DealCard(deal = deal, onAdd = { AppState.addToCart(deal.product) })
                    }
                }
            }
        }
        CartPanel(onCheckout = onNavigateToCart)
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (active) Primary else White,
        border = BorderStroke(1.dp, if (active) Primary else BorderStrong)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) White else TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun DealCard(deal: Deal, onAdd: () -> Unit) {
    val lang = AppState.language
    val name = deal.product.localizedName(lang)
    val inCart = AppState.isInCart(deal.product.id)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp).background(Gray100)) {
                AsyncImage(
                    model = deal.product.imageUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(10.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(6.dp),
                    color = ErrorRed
                ) {
                    Text(
                        "-${deal.discount}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                deal.expiresIn?.let { exp ->
                    Surface(
                        modifier = Modifier.padding(10.dp).align(Alignment.TopEnd),
                        shape = RoundedCornerShape(6.dp),
                        color = AccentOrange
                    ) {
                        Text(
                            exp,
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
                    deal.product.category,
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
                    Column {
                        Text(
                            deal.formattedOriginal(),
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1),
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                        Text(
                            deal.formattedDiscounted(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (inCart) SuccessGreen else AccentOrange, CircleShape)
                            .clickable { onAdd() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (inCart) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
