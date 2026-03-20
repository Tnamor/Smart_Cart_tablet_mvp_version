package com.smartcart.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcart.data.repository.AppState
import com.smartcart.ui.components.CartPanel
import com.smartcart.ui.components.SharedSidebar
import com.smartcart.ui.components.SharedTopBar
import com.smartcart.ui.theme.*

data class Message(val text: String, val isFromUser: Boolean)

@Composable
fun SupportScreen(
    onNavigateHome: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateCats: () -> Unit,
    onNavigateFavs: () -> Unit,
    onNavigateToCart: () -> Unit,
) {
    val t = AppState.t()
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf(
        Message("Здравствуйте! Как я могу вам помочь?", false),
        Message("У меня возникли проблемы с определением весового товара.", true),
        Message("Пожалуйста, поместите товар на весовую платформу тележки и подождите 2 секунды.", false)
    ) }

    Row(Modifier.background(Background).fillMaxSize()) {
        SharedSidebar(
            activeRoute = "support",
            onNavigate = { r ->
                when (r) {
                    "home" -> onNavigateHome()
                    "list" -> onNavigateToList()
                    "cats" -> onNavigateCats()
                    "favs" -> onNavigateFavs()
                }
            }
        )
        Column(Modifier.weight(1f).fillMaxSize()) {
            SharedTopBar(searchQuery = "", onSearchQueryChange = {})
            
            Column(Modifier.weight(1f).padding(32.dp)) {
                Text(t.navSupport, fontSize = 28.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                Spacer(Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = White,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gray100)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(messages) { msg ->
                                ChatBubble(msg)
                            }
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Введите сообщение...") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Border)
                            )
                            Button(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        messages.add(Message(messageText, true))
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Rounded.Send, null)
                            }
                        }
                    }
                }
            }
        }
        CartPanel(onCheckout = onNavigateToCart)
    }
}

@Composable
fun ChatBubble(message: Message) {
    val align = if (message.isFromUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isFromUser) Primary else Gray100
    val txtColor = if (message.isFromUser) White else TextPrimary
    
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.text,
                color = txtColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                fontSize = 14.sp
            )
        }
    }
}
