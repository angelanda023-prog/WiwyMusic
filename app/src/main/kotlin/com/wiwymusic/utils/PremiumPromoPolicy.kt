package com.wiwymusic.utils

fun shouldShowPremiumPromo(
    isPremium: Boolean?,
    lastShownEpochDay: Long,
    todayEpochDay: Long,
): Boolean = isPremium == false && lastShownEpochDay != todayEpochDay
