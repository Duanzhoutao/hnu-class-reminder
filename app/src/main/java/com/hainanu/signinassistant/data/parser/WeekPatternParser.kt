package com.hainanu.signinassistant.data.parser

object WeekPatternParser {

    fun parse(input: String): Set<Int> {
        val normalized = input
            .replace("（", "(")
            .replace("）", ")")
            .replace("(周)", "周")
            .replace("周", "")
            .trim()

        val oddOnly = normalized.contains("单")
        val evenOnly = normalized.contains("双")
        val rangeText = normalized
            .replace("(单)", "")
            .replace("(双)", "")
            .replace("(单周)", "")
            .replace("(双周)", "")
            .replace(" ", "")

        return rangeText.split(",")
            .flatMap { segment ->
                when {
                    segment.contains("-") -> {
                        val (start, end) = segment.split("-", limit = 2)
                        val startValue = start.toIntOrNull()
                        val endValue = end.toIntOrNull()
                        if (startValue == null || endValue == null) {
                            emptyList()
                        } else {
                            (startValue..endValue).toList()
                        }
                    }

                    segment.toIntOrNull() != null -> listOf(segment.toInt())
                    else -> emptyList()
                }
            }
            .filter { value ->
                when {
                    oddOnly -> value % 2 == 1
                    evenOnly -> value % 2 == 0
                    else -> true
                }
            }
            .toSet()
    }
}
