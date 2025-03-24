package com.example.silassaq.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.silassaq.R
import com.example.silassaq.network.MetNoWeatherService
import com.example.silassaq.ui.theme.AccentBlue
import com.example.silassaq.ui.theme.AccentYellow
import com.example.silassaq.ui.theme.TextSecondaryLight

/**
 * Enhanced location selector that groups locations by region
 */
@Composable
fun EnhancedLocationSelector(
    selectedLocation: String,
    onLocationSelected: (String) -> Unit
) {
    // Get locations grouped by region
    val locationsByRegion = MetNoWeatherService.getGreenlandLocationsByRegion()
    
    // Track expanded regions
    val expandedRegions = remember { mutableStateMapOf<String, Boolean>() }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Greenland Locations",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Display regions
        locationsByRegion.forEach { (region, locations) ->
            // Initialize region as expanded if it contains the selected location
            val isExpanded = expandedRegions.getOrPut(region) {
                locations.contains(selectedLocation)
            }
            
            // Region header
            RegionHeader(
                region = region,
                isExpanded = isExpanded,
                onToggleExpanded = { expandedRegions[region] = !isExpanded }
            )
            
            // Location list for expanded regions
            if (isExpanded) {
                locations.forEach { location ->
                    LocationItem(
                        location = location,
                        isSelected = location == selectedLocation,
                        onLocationSelected = onLocationSelected
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Header for a region with expand/collapse functionality
 */
@Composable
private fun RegionHeader(
    region: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = region,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AccentBlue
        )
        
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = AccentBlue
        )
    }
    
    HorizontalDivider(
        color = Color.LightGray.copy(alpha = 0.5f),
        thickness = 1.dp
    )
}

/**
 * Individual location item
 */
@Composable
private fun LocationItem(
    location: String,
    isSelected: Boolean,
    onLocationSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLocationSelected(location) }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = if (isSelected) AccentYellow else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = location,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentBlue else Color.Black,
            modifier = Modifier.weight(1f)
        )
        
        // Weather icon placeholder
        Icon(
            painter = painterResource(id = R.drawable.ic_weather_sunny),
            contentDescription = "Weather",
            tint = AccentYellow,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Temperature placeholder
        // In a real implementation, this would show the actual temperature
        Text(
            text = if (isSelected) "+5°C" else "",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
