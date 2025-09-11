package com.emikhalets.miniweather.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emikhalets.miniweather.R
import com.emikhalets.miniweather.core.formatDoubleOneDigit
import com.emikhalets.miniweather.core.theme.MiniWeatherTheme
import com.emikhalets.miniweather.data.local.AqiLevel
import com.emikhalets.miniweather.data.local.PollutantItem
import com.emikhalets.miniweather.data.local.PollutantItem.Companion.classify
import com.emikhalets.miniweather.data.local.PollutantItem.Companion.getCode
import com.emikhalets.miniweather.domain.model.PollutionModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun AirQualityDetails(data: PollutionModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val items = remember(data) {
        listOf(
            Pair(PollutantItem.PM2_5, data.pm2_5),
            Pair(PollutantItem.PM10, data.pm10),
            Pair(PollutantItem.O3, data.o3),
            Pair(PollutantItem.NO2, data.no2),
            Pair(PollutantItem.SO2, data.so2),
            Pair(PollutantItem.CO, data.co),
            Pair(PollutantItem.NO, data.no),
            Pair(PollutantItem.NH3, data.nh3),
        )
    }

    Column(modifier.fillMaxWidth()) {
        Row {
            val level = remember { AqiLevel.getLevelByValue(data.aqi) }
            val levelValue by remember { mutableStateOf(context.getString(level.nameRes)) }
            Column {
                Text(
                    stringResource(R.string.air_quality),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.air_quality_value, levelValue, data.aqi),
                        style = MaterialTheme.typography.titleSmall
                    )
                    ColorPin(16, level)
                }
            }
            Spacer(Modifier.width(8.dp))
            Spacer(Modifier.weight(1f))
            Text(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(data.updatedAt * 1000)),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEach { PollutantRow(it) }
        }
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        AirLegend()
    }
}

@Composable
private fun AirLegend() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AqiLevel.entries.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorPin(12, it)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(it.nameRes),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun PollutantRow(item: Pair<PollutantItem, Double?>) {
    val formattedValue = remember(item) { item.second?.let { formatDoubleOneDigit(it) } ?: "—" }
    val level = remember(item) { item.first.classify(item.second) }

    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(6.dp, 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ColorPin(16, level)
        Spacer(Modifier.width(6.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(item.first.nameRes),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "(${item.first.getCode()})",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            text = if (level != AqiLevel.NA) {
                stringResource(level.nameRes)
            } else {
                ""
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .widthIn(min = 72.dp)
        )
        Text(
            text = stringResource(R.string.air_quality_meature_value, formattedValue),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier
                .widthIn(min = 96.dp)
                .wrapContentWidth(Alignment.End)
        )
    }
}

@Composable
private fun ColorPin(size: Int, level: AqiLevel) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(level.color, CircleShape)
    )
}

// ==============
//  Preview Data
// ==============

@Preview(showBackground = true)
@Composable
private fun PreviewData() {
    MiniWeatherTheme {
        AirQualityDetails(
            data = PollutionModel(
                aqi = Random.nextInt(1, 5),
                updatedAt = System.currentTimeMillis() / 1000,
                co = Random.nextDouble(0.0, 15400.0),
                no = Random.nextDouble(0.0, 1.0),
                no2 = Random.nextDouble(0.0, 200.0),
                o3 = Random.nextDouble(0.0, 180.0),
                so2 = Random.nextDouble(0.0, 350.0),
                pm2_5 = Random.nextDouble(0.0, 75.0),
                pm10 = Random.nextDouble(0.0, 200.0),
                nh3 = Random.nextDouble(0.0, 1.0),
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDataFullScreen() {
    MiniWeatherTheme {
        Box(Modifier.fillMaxSize()) {
            AirQualityDetails(
                data = PollutionModel(
                    aqi = Random.nextInt(1, 6),
                    updatedAt = System.currentTimeMillis() / 1000,
                    co = Random.nextDouble(0.0, 15400.0),
                    no = Random.nextDouble(0.0, 1.0),
                    no2 = Random.nextDouble(0.0, 200.0),
                    o3 = Random.nextDouble(0.0, 180.0),
                    so2 = Random.nextDouble(0.0, 350.0),
                    pm2_5 = Random.nextDouble(0.0, 75.0),
                    pm10 = Random.nextDouble(0.0, 200.0),
                    nh3 = Random.nextDouble(0.0, 1.0),
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}