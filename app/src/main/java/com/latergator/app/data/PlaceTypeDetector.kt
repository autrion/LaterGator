package com.latergator.app.data

object PlaceTypeDetector {

    private val rules: List<Pair<Regex, String>> = listOf(
        keywords("supermarkt", "einkaufen", "einkauf", "rewe", "aldi", "lidl", "edeka",
                 "kaufland", "netto", "lebensmittel", "penny", "tegut") to "🛒 Supermarkt",
        keywords("drogerie", "dm ", " dm,", " dm.", "rossmann", "müller") to "🧴 Drogerie",
        keywords("baumarkt", "obi", "hornbach", "hagebau", "werkzeug", "schrauben") to "🔨 Baumarkt",
        keywords("apotheke") to "💊 Apotheke",
        keywords("arzt", "zahnarzt", "praxis", "klinik", "krankenhaus", "doktor", "termin beim") to "🏥 Arzt",
        keywords("post", "dhl", "hermes", "paket", "briefkasten", "sendung", "einschreiben") to "📦 Post",
        keywords("bank", "geldautomat", "atm", "sparkasse", "commerzbank", "volksbank") to "🏦 Bank",
        keywords("tankstelle", "tanken", "sprit", "benzin") to "⛽ Tankstelle",
        keywords("büro", "arbeit", "firma", "kollege", "kollegin", "chef", "meeting") to "💼 Büro",
        keywords("zuhause", "küche", "keller", "balkon", "garage", "wohnung", "wäsche") to "🏠 Zuhause",
    )

    private fun keywords(vararg words: String): Regex =
        words.joinToString("|") { Regex.escape(it) }.toRegex(RegexOption.IGNORE_CASE)

    fun detect(description: String): String? =
        rules.firstOrNull { (pattern, _) -> pattern.containsMatchIn(description) }?.second
}
