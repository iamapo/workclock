package com.iamapo.timetracker.domain

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

enum class GermanFederalState {
    BadenWuerttemberg,
    Bavaria,
    Berlin,
    Brandenburg,
    Bremen,
    Hamburg,
    Hesse,
    MecklenburgWesternPomerania,
    LowerSaxony,
    NorthRhineWestphalia,
    RhinelandPalatinate,
    Saarland,
    Saxony,
    SaxonyAnhalt,
    SchleswigHolstein,
    Thuringia
}

enum class GermanHolidayKind {
    NewYear,
    Epiphany,
    InternationalWomensDay,
    GoodFriday,
    EasterSunday,
    EasterMonday,
    LabourDay,
    AscensionDay,
    WhitSunday,
    WhitMonday,
    CorpusChristi,
    AssumptionDay,
    WorldChildrensDay,
    GermanUnityDay,
    ReformationDay,
    AllSaintsDay,
    DayOfRepentanceAndPrayer,
    ChristmasDay,
    SecondChristmasDay
}

data class GermanHoliday(
    val date: LocalDate,
    val kind: GermanHolidayKind
)

object GermanHolidayCalendar {
    fun holiday(
        date: LocalDate,
        federalState: GermanFederalState
    ): GermanHoliday? = holidays(date.year, federalState).firstOrNull { holiday -> holiday.date == date }

    fun holidays(
        year: Int,
        federalState: GermanFederalState
    ): List<GermanHoliday> {
        val easterSunday = easterSunday(year)
        return buildList {
            addHoliday(LocalDate(year, 1, 1), GermanHolidayKind.NewYear)
            if (federalState in EpiphanyStates) {
                addHoliday(LocalDate(year, 1, 6), GermanHolidayKind.Epiphany)
            }
            if (federalState == GermanFederalState.Berlin && year >= 2019 ||
                federalState == GermanFederalState.MecklenburgWesternPomerania && year >= 2023
            ) {
                addHoliday(LocalDate(year, 3, 8), GermanHolidayKind.InternationalWomensDay)
            }
            addHoliday(easterSunday - DatePeriod(days = 2), GermanHolidayKind.GoodFriday)
            if (federalState == GermanFederalState.Brandenburg) {
                addHoliday(easterSunday, GermanHolidayKind.EasterSunday)
            }
            addHoliday(easterSunday + DatePeriod(days = 1), GermanHolidayKind.EasterMonday)
            addHoliday(LocalDate(year, 5, 1), GermanHolidayKind.LabourDay)
            addHoliday(easterSunday + DatePeriod(days = 39), GermanHolidayKind.AscensionDay)
            if (federalState == GermanFederalState.Brandenburg) {
                addHoliday(easterSunday + DatePeriod(days = 49), GermanHolidayKind.WhitSunday)
            }
            addHoliday(easterSunday + DatePeriod(days = 50), GermanHolidayKind.WhitMonday)
            if (federalState in CorpusChristiStates) {
                addHoliday(easterSunday + DatePeriod(days = 60), GermanHolidayKind.CorpusChristi)
            }
            if (federalState == GermanFederalState.Saarland) {
                addHoliday(LocalDate(year, 8, 15), GermanHolidayKind.AssumptionDay)
            }
            if (federalState == GermanFederalState.Thuringia && year >= 2019) {
                addHoliday(LocalDate(year, 9, 20), GermanHolidayKind.WorldChildrensDay)
            }
            addHoliday(LocalDate(year, 10, 3), GermanHolidayKind.GermanUnityDay)
            if (isReformationDayHoliday(year, federalState)) {
                addHoliday(LocalDate(year, 10, 31), GermanHolidayKind.ReformationDay)
            }
            if (federalState in AllSaintsStates) {
                addHoliday(LocalDate(year, 11, 1), GermanHolidayKind.AllSaintsDay)
            }
            if (federalState == GermanFederalState.Saxony) {
                addHoliday(dayOfRepentanceAndPrayer(year), GermanHolidayKind.DayOfRepentanceAndPrayer)
            }
            addHoliday(LocalDate(year, 12, 25), GermanHolidayKind.ChristmasDay)
            addHoliday(LocalDate(year, 12, 26), GermanHolidayKind.SecondChristmasDay)
        }.sortedBy { holiday -> holiday.date }
    }

    private fun MutableList<GermanHoliday>.addHoliday(
        date: LocalDate,
        kind: GermanHolidayKind
    ) {
        add(GermanHoliday(date, kind))
    }

    private fun isReformationDayHoliday(
        year: Int,
        federalState: GermanFederalState
    ): Boolean = federalState in OriginalReformationStates ||
        year == 2017 ||
        year >= 2018 && federalState in NewReformationStates

    private fun dayOfRepentanceAndPrayer(year: Int): LocalDate {
        var date = LocalDate(year, 11, 22)
        while (date.dayOfWeek != kotlinx.datetime.DayOfWeek.WEDNESDAY) {
            date -= DatePeriod(days = 1)
        }
        return date
    }

    private fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate(year, month, day)
    }

    private val EpiphanyStates = setOf(
        GermanFederalState.BadenWuerttemberg,
        GermanFederalState.Bavaria,
        GermanFederalState.SaxonyAnhalt
    )
    private val CorpusChristiStates = setOf(
        GermanFederalState.BadenWuerttemberg,
        GermanFederalState.Bavaria,
        GermanFederalState.Hesse,
        GermanFederalState.NorthRhineWestphalia,
        GermanFederalState.RhinelandPalatinate,
        GermanFederalState.Saarland
    )
    private val AllSaintsStates = setOf(
        GermanFederalState.BadenWuerttemberg,
        GermanFederalState.Bavaria,
        GermanFederalState.NorthRhineWestphalia,
        GermanFederalState.RhinelandPalatinate,
        GermanFederalState.Saarland
    )
    private val OriginalReformationStates = setOf(
        GermanFederalState.Brandenburg,
        GermanFederalState.MecklenburgWesternPomerania,
        GermanFederalState.Saxony,
        GermanFederalState.SaxonyAnhalt,
        GermanFederalState.Thuringia
    )
    private val NewReformationStates = setOf(
        GermanFederalState.Bremen,
        GermanFederalState.Hamburg,
        GermanFederalState.LowerSaxony,
        GermanFederalState.SchleswigHolstein
    )
}
