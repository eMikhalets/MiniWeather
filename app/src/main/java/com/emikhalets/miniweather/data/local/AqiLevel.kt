package com.emikhalets.miniweather.data.local

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.emikhalets.miniweather.R

enum class AqiLevel(
    @StringRes val nameRes: Int,
    val color: Color,
) {
    Good(R.string.air_quality_good, Color(0xFF00E400)),
    Fair(R.string.air_quality_fair, Color(0xFFFFEB00)),
    Moderate(R.string.air_quality_moderate, Color(0xFFFF7E00)),
    Poor(R.string.air_quality_poor, Color(0xFFFF0000)),
    VeryPoor(R.string.air_quality_very_poor, Color(0xFF8F3F97)),
    NA(R.string.air_quality_na, Color.Gray);

    companion object {

        fun getLevelByValue(value: Int): AqiLevel {
            return when (value) {
                1 -> Good
                2 -> Fair
                3 -> Moderate
                4 -> Poor
                5 -> VeryPoor
                else -> NA
            }
        }
    }
}

enum class PollutantItem(
    @StringRes val nameRes: Int,
) {
    SO2(R.string.air_quality_so2),
    NO2(R.string.air_quality_no2),
    PM10(R.string.air_quality_pm),
    PM2_5(R.string.air_quality_pm),
    O3(R.string.air_quality_o3),
    CO(R.string.air_quality_co),
    NO(R.string.air_quality_no),
    NH3(R.string.air_quality_nh3);

    companion object {

        fun PollutantItem.getCode(): String {
            return when (this) {
                SO2 -> "SO₂"
                NO2 -> "NO₂"
                PM10 -> "PM 10"
                PM2_5 -> "PM 2.5"
                O3 -> "O₃"
                CO -> "CO"
                NO -> "NO"
                NH3 -> "NH₃"
            }
        }

        fun PollutantItem.classify(value: Double?): AqiLevel {
            value ?: return AqiLevel.NA
            return when (this) {
                SO2 -> when {
                    value < 20 -> AqiLevel.Good
                    value < 80 -> AqiLevel.Fair
                    value < 250 -> AqiLevel.Moderate
                    value < 350 -> AqiLevel.Poor
                    else -> AqiLevel.VeryPoor
                }

                NO2 -> when {
                    value < 40 -> AqiLevel.Good
                    value < 70 -> AqiLevel.Fair
                    value < 150 -> AqiLevel.Moderate
                    value < 200 -> AqiLevel.Poor
                    else -> AqiLevel.VeryPoor
                }

                PM10 -> when {
                    value < 20 -> AqiLevel.Good
                    value < 50 -> AqiLevel.Fair
                    value < 100 -> AqiLevel.Moderate
                    value < 200 -> AqiLevel.Poor
                    else -> AqiLevel.VeryPoor
                }

                PM2_5 -> when {
                    value < 10 -> AqiLevel.Good
                    value < 25 -> AqiLevel.Fair
                    value < 50 -> AqiLevel.Moderate
                    value < 75 -> AqiLevel.Poor
                    else -> AqiLevel.VeryPoor
                }

                O3 -> when {
                    value < 60 -> AqiLevel.Good
                    value < 100 -> AqiLevel.Fair
                    value < 140 -> AqiLevel.Moderate
                    value < 180 -> AqiLevel.Poor
                    else -> AqiLevel.VeryPoor
                }

                CO -> when {
                    value < 4400 -> AqiLevel.Good
                    value < 9400 -> AqiLevel.Fair
                    value < 12400 -> AqiLevel.Moderate
                    value < 15400 -> AqiLevel.Poor
                    else -> AqiLevel.VeryPoor
                }

                NO -> AqiLevel.NA

                NH3 -> AqiLevel.NA
            }
        }
    }
}

fun aqiMeta(value: Int): AqiLevel {
    return when (value) {
        1 -> AqiLevel.Good
        2 -> AqiLevel.Fair
        3 -> AqiLevel.Moderate
        4 -> AqiLevel.Poor
        5 -> AqiLevel.VeryPoor
        else -> AqiLevel.NA
    }
}