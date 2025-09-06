package com.emikhalets.miniweather.ui.weather

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.emikhalets.miniweather.R
import com.emikhalets.miniweather.core.LoadState
import com.emikhalets.miniweather.core.formatDoubleOneDigit
import com.emikhalets.miniweather.core.roundToIntOrDash
import com.emikhalets.miniweather.core.theme.MiniWeatherTheme
import com.emikhalets.miniweather.domain.model.WeatherModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenRoot(
        state = state,
        onQueryChange = viewModel::setQuery,
        onSearchCity = viewModel::getWeather,
        onLocationClick = {},
        onRetryClick = viewModel::getWeather,
        onPullRefresh = viewModel::getWeather,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenRoot(
    state: WeatherUiState,
    onPullRefresh: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchCity: () -> Unit,
    onLocationClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather)) },
                actions = {
                    // TODO: set location feature
                    IconButton(onClick = onLocationClick, enabled = false) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.my_location)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PullToRefreshBox(state.refreshing == LoadState.Loading, onRefresh = onPullRefresh) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.enter_city)) },
                        trailingIcon = {
                            IconButton(
                                onClick = onSearchCity,
                                enabled = state.query.isNotBlank()
                            ) { Icon(Icons.Default.Search, null) }
                        }
                    )

                    when {
                        state.weather == null -> {
                            when (state.loading) {
                                LoadState.Idle -> {
                                    EmptyStub()
                                }

                                LoadState.Loading -> {
                                    LoadingSkeleton()
                                }

                                is LoadState.Error -> {
                                    ErrorStub(state.loading.message, onRetryClick)
                                }
                            }
                        }

                        else -> {
                            WeatherHeroCard(state.weather)
                            DetailsGrid(state.weather)
                            if (state.refreshing is LoadState.Error) {
                                Toast.makeText(
                                    context,
                                    stringResource(R.string.error_refreshing_weather),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherHeroCard(model: WeatherModel) {
    val gradient = heroGradient(model)
    val updated = remember(model.updatedAt) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(model.updatedAt * 1000))
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradient))
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.hero_city_updated_at, model.city, updated),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${model.temperature?.roundToIntOrDash()}°",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.feels_like, model.feelsLike.roundToIntOrDash()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = model.description.replaceFirstChar { it.titlecase() }.ifEmpty { "—" },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
            AsyncImage(
                model = model.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

@Composable
private fun DetailsGrid(model: WeatherModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailChip(
            title = stringResource(R.string.humidity),
            value = model.humidity?.let { "${model.humidity}%" } ?: "—",
            modifier = Modifier.weight(1f)
        )
        DetailChip(
            title = stringResource(R.string.wind),
            value = model.windSpeed?.let { "${formatDoubleOneDigit(model.windSpeed)} м/с" } ?: "—",
            modifier = Modifier.weight(1f)
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        model.pressure?.let {
            DetailChip(
                title = stringResource(R.string.pressure),
                value = "$it",
                modifier = Modifier.weight(1f)
            )
        }
        model.dewPoint?.let {
            DetailChip(
                title = stringResource(R.string.dew_point),
                value = "${formatDoubleOneDigit(it)}°",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailChip(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun LoadingSkeleton() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .weight(1f)
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
private fun EmptyStub() {
    Text(
        text = stringResource(R.string.enter_city_to_show_weather),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ErrorStub(message: String, onRetryClick: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.error_value, message),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetryClick) {
            Text(stringResource(R.string.repeat))
        }
    }
}

@Preview
@Composable
private fun Preview1() {
    val model = WeatherModel(
        city = "Москва",
        temperature = 21.0,
        feelsLike = 18.0,
        humidity = 65,
        windSpeed = 2.5,
        description = "Какое-то описание хз",
        iconUrl = "",
        updatedAt = System.currentTimeMillis(),
        pressure = 762,
        dewPoint = 12.4,
    )
    val state = WeatherUiState(
        weather = model,
        query = "Лондон"
    )
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    val model = WeatherModel(
        city = "Москва",
        temperature = 15.0,
        feelsLike = 18.0,
        humidity = 65,
        windSpeed = 2.5,
        description = "Какое-то описание хз",
        iconUrl = "",
        updatedAt = System.currentTimeMillis(),
        pressure = 762,
        dewPoint = 12.4,
    )
    val state = WeatherUiState(weather = model)
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}

@Preview
@Composable
private fun Preview3() {
    val state = WeatherUiState(loading = LoadState.Error("Ошибка загрузки"))
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}

@Preview
@Composable
private fun Preview4() {
    val state = WeatherUiState(loading = LoadState.Idle)
    MiniWeatherTheme {
        ScreenRoot(
            state = state,
            onQueryChange = {},
            onSearchCity = {},
            onLocationClick = {},
            onRetryClick = {},
            onPullRefresh = {},
        )
    }
}