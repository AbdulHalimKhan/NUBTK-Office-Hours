package com.example.data

data class BangladeshHoliday(
    val date: String, // YYYY-MM-DD
    val name: String,
    val description: String = "Bangladesh Public Holiday"
)

object BangladeshHolidayCalendar {

    // Comprehensive list of Bangladesh public holidays matching Google Calendar's Bangladesh Holiday feed
    private val holidays = listOf(
        // 2025 Holidays
        BangladeshHoliday("2025-02-15", "Shab-e-Barat", "Religious Holiday"),
        BangladeshHoliday("2025-02-21", "International Mother Language Day", "National Holiday"),
        BangladeshHoliday("2025-03-17", "Sheikh Mujibur Rahman's Birthday & Children's Day", "National Holiday"),
        BangladeshHoliday("2025-03-26", "Independence Day", "National Holiday"),
        BangladeshHoliday("2025-03-27", "Shab-e-Qadr", "Religious Holiday"),
        BangladeshHoliday("2025-03-28", "Jumatul Bidah", "Religious Holiday"),
        BangladeshHoliday("2025-03-31", "Eid-ul-Fitr Holiday", "Religious Holiday"),
        BangladeshHoliday("2025-04-01", "Eid-ul-Fitr", "Religious Holiday"),
        BangladeshHoliday("2025-04-02", "Eid-ul-Fitr Holiday", "Religious Holiday"),
        BangladeshHoliday("2025-04-14", "Bengali New Year (Pohela Boishakh)", "Cultural Holiday"),
        BangladeshHoliday("2025-05-01", "May Day", "International Workers' Day"),
        BangladeshHoliday("2025-05-12", "Buddha Purnima", "Religious Holiday"),
        BangladeshHoliday("2025-06-06", "Eid-ul-Adha Holiday", "Religious Holiday"),
        BangladeshHoliday("2025-06-07", "Eid-ul-Adha", "Religious Holiday"),
        BangladeshHoliday("2025-06-08", "Eid-ul-Adha Holiday", "Religious Holiday"),
        BangladeshHoliday("2025-07-06", "Ashura (Muharram)", "Religious Holiday"),
        BangladeshHoliday("2025-08-15", "National Mourning Day", "National Observance"),
        BangladeshHoliday("2025-08-16", "Janmashtami", "Religious Holiday"),
        BangladeshHoliday("2025-09-05", "Eid-e-Milad-un-Nabi", "Religious Holiday"),
        BangladeshHoliday("2025-10-02", "Durga Puja (Bijoya Dashami)", "Religious Holiday"),
        BangladeshHoliday("2025-12-16", "Victory Day", "National Holiday"),
        BangladeshHoliday("2025-12-25", "Christmas Day", "Religious Holiday"),

        // 2026 Holidays
        BangladeshHoliday("2026-02-04", "Shab-e-Barat", "Religious Holiday"),
        BangladeshHoliday("2026-02-21", "International Mother Language Day", "National Holiday"),
        BangladeshHoliday("2026-03-17", "Sheikh Mujibur Rahman's Birthday & Children's Day", "National Holiday"),
        BangladeshHoliday("2026-03-18", "Shab-e-Qadr", "Religious Holiday"),
        BangladeshHoliday("2026-03-20", "Jumatul Bidah", "Religious Holiday"),
        BangladeshHoliday("2026-03-21", "Eid-ul-Fitr Holiday", "Religious Holiday"),
        BangladeshHoliday("2026-03-22", "Eid-ul-Fitr", "Religious Holiday"),
        BangladeshHoliday("2026-03-23", "Eid-ul-Fitr Holiday", "Religious Holiday"),
        BangladeshHoliday("2026-03-26", "Independence & National Day", "National Holiday"),
        BangladeshHoliday("2026-04-14", "Bengali New Year (Pohela Boishakh)", "Cultural Holiday"),
        BangladeshHoliday("2026-05-01", "May Day", "International Workers' Day"),
        BangladeshHoliday("2026-05-27", "Eid-ul-Adha Holiday", "Religious Holiday"),
        BangladeshHoliday("2026-05-28", "Eid-ul-Adha", "Religious Holiday"),
        BangladeshHoliday("2026-05-29", "Eid-ul-Adha Holiday", "Religious Holiday"),
        BangladeshHoliday("2026-05-31", "Buddha Purnima", "Religious Holiday"),
        BangladeshHoliday("2026-06-26", "Ashura (Muharram)", "Religious Holiday"),
        BangladeshHoliday("2026-08-15", "National Mourning Day", "National Observance"),
        BangladeshHoliday("2026-08-25", "Eid-e-Milad-un-Nabi", "Religious Holiday"),
        BangladeshHoliday("2026-09-04", "Janmashtami", "Religious Holiday"),
        BangladeshHoliday("2026-10-20", "Durga Puja (Maha Nabami)", "Religious Holiday"),
        BangladeshHoliday("2026-10-21", "Durga Puja (Bijoya Dashami)", "Religious Holiday"),
        BangladeshHoliday("2026-12-16", "Victory Day", "National Holiday"),
        BangladeshHoliday("2026-12-25", "Christmas Day", "Religious Holiday"),

        // 2027 Holidays
        BangladeshHoliday("2027-01-24", "Shab-e-Barat", "Religious Holiday"),
        BangladeshHoliday("2027-02-21", "International Mother Language Day", "National Holiday"),
        BangladeshHoliday("2027-03-08", "Shab-e-Qadr", "Religious Holiday"),
        BangladeshHoliday("2027-03-10", "Eid-ul-Fitr Holiday", "Religious Holiday"),
        BangladeshHoliday("2027-03-11", "Eid-ul-Fitr", "Religious Holiday"),
        BangladeshHoliday("2027-03-12", "Eid-ul-Fitr Holiday", "Religious Holiday"),
        BangladeshHoliday("2027-03-17", "Sheikh Mujibur Rahman's Birthday", "National Holiday"),
        BangladeshHoliday("2027-03-26", "Independence Day", "National Holiday"),
        BangladeshHoliday("2027-04-14", "Bengali New Year (Pohela Boishakh)", "Cultural Holiday"),
        BangladeshHoliday("2027-05-01", "May Day", "International Workers' Day"),
        BangladeshHoliday("2027-05-17", "Eid-ul-Adha Holiday", "Religious Holiday"),
        BangladeshHoliday("2027-05-18", "Eid-ul-Adha", "Religious Holiday"),
        BangladeshHoliday("2027-05-19", "Eid-ul-Adha Holiday", "Religious Holiday"),
        BangladeshHoliday("2027-05-20", "Buddha Purnima", "Religious Holiday"),
        BangladeshHoliday("2027-06-16", "Ashura (Muharram)", "Religious Holiday"),
        BangladeshHoliday("2027-08-15", "National Mourning Day", "National Observance"),
        BangladeshHoliday("2027-08-15", "Eid-e-Milad-un-Nabi", "Religious Holiday"),
        BangladeshHoliday("2027-08-25", "Janmashtami", "Religious Holiday"),
        BangladeshHoliday("2027-10-10", "Durga Puja (Bijoya Dashami)", "Religious Holiday"),
        BangladeshHoliday("2027-12-16", "Victory Day", "National Holiday"),
        BangladeshHoliday("2027-12-25", "Christmas Day", "Religious Holiday")
    )

    /**
     * Returns Bangladesh public holidays that fall between the given ISO start and end dates.
     */
    fun getHolidaysForPeriod(startDateIso: String, endDateIso: String): List<BangladeshHoliday> {
        val s = startDateIso.trim()
        val e = endDateIso.trim()
        if (s.isEmpty() || e.isEmpty()) return emptyList()
        return holidays.filter { it.date in s..e }.sortedBy { it.date }
    }
}
