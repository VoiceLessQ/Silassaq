package com.example.silassaq.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.silassaq.ui.theme.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    visible: Boolean,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    isDarkMode: Boolean,
    greenlandCities: List<String> = listOf("Nuuk", "Ilulissat", "Sisimiut", "Qaqortoq", "Tasiilaq", "Aasiaat")
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Search screen implementation
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { 
                    onDismiss()
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 16.dp)
                    .clickable(enabled = false) { /* Prevent clicks from passing through */ },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) DarkNavyCard else Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header with title and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Search location",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else TextPrimaryLight
                        )
                        
                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = if (isDarkMode) Color.White else TextPrimaryLight
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search input field
                    TextField(
                        value = searchText,
                        onValueChange = { onSearchTextChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp)),
                        placeholder = { 
                            Text(
                                "Enter city name",
                                color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = if (isDarkMode) Color.White else TextPrimaryLight,
                            unfocusedTextColor = if (isDarkMode) Color.White else TextPrimaryLight,
                            focusedContainerColor = if (isDarkMode) DarkNavyCardLight else Color.LightGray.copy(alpha = 0.2f),
                            unfocusedContainerColor = if (isDarkMode) DarkNavyCardLight else Color.LightGray.copy(alpha = 0.2f),
                            cursorColor = if (isDarkMode) AccentBlue else MeteoBlue,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (searchText.isNotEmpty()) {
                                    onSearch(searchText)
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Search button
                    Button(
                        onClick = {
                            if (searchText.isNotEmpty()) {
                                onSearch(searchText)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode) AccentBlue else MeteoBlue
                        )
                    ) {
                        Text(
                            text = "Search",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Greenland cities
                    Text(
                        text = "Greenland Cities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else TextPrimaryLight
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // City chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        greenlandCities.forEach { city ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isDarkMode) DarkNavyCardLight else Color.LightGray.copy(alpha = 0.3f))
                                    .clickable {
                                        onSearch(city)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = city,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDarkMode) Color.White else TextPrimaryLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 