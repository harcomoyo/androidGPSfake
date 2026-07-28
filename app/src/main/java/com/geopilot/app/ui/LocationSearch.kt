package com.geopilot.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.geopilot.app.search.LocationSearchResult
import com.geopilot.app.search.LocationSearchService
import kotlinx.coroutines.launch

@Composable
fun LocationSearch(
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchService = remember { LocationSearchService() }
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<LocationSearchResult>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }

    fun search() {
        if (query.isBlank() || searching) return
        searching = true
        error = null
        results = emptyList()
        scope.launch {
            searchService.search(query)
                .onSuccess {
                    results = it
                    if (it.isEmpty()) error = "No locations found in Indonesia."
                }
                .onFailure { error = "Search failed. Check your connection." }
            searching = false
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
        ) {
            TextField(
                value = query,
                onValueChange = {
                    query = it
                    error = null
                    if (it.isBlank()) results = emptyList()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search places") },
                leadingIcon = { Icon(Icons.Outlined.Search, "Search") },
                trailingIcon = {
                    when {
                        searching -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        query.isNotEmpty() -> IconButton(
                            onClick = {
                                query = ""
                                results = emptyList()
                                error = null
                            },
                        ) { Icon(Icons.Outlined.Close, "Clear") }
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                keyboardActions = KeyboardActions(onSearch = { search() }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
        }
        if (error != null) {
            Text(
                error.orEmpty(),
                modifier = Modifier.padding(horizontal = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (results.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ) {
                Column {
                    results.forEachIndexed { index, result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = result.name.substringBefore(',')
                                    results = emptyList()
                                    onLocationSelected(result.latitude, result.longitude)
                                }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    result.name.substringBefore(','),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                )
                                Text(
                                    result.name.substringAfter(',', "Indonesia").trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                )
                            }
                        }
                        if (index < results.lastIndex) HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                    }
                }
            }
        }
    }
}
