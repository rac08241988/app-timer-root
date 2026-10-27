package com.apptimerroot.lock

/**
 * Best-effort category -> package mapping for bulk selection chips.
 * Known package lists plus simple label/package keyword heuristics.
 */
enum class AppCategory(val label: String) {
    SOCIAL("Social"),
    STREAMING("Streaming"),
    GAMES("Games"),
    BROWSERS("Browsers"),
    COMMUNICATION("Communication/Chat"),
    SHOPPING("Shopping"),
    NEWS("News"),
    ENTERTAINMENT("Entertainment");
}

object AppCategories {

    private val known: Map<AppCategory, Set<String>> = mapOf(
        AppCategory.SOCIAL to setOf(
            "com.facebook.katana",
            "com.facebook.lite",
            "com.instagram.android",
            "com.instagram.barcelona", // Threads
            "com.twitter.android",
            "com.zhiliaoapp.musically", // TikTok
            "com.ss.android.ugc.trill",  // TikTok alt
            "com.reddit.frontpage",
            "com.snapchat.android",
            "com.linkedin.android",
            "com.pinterest",
            "com.tumblr",
            "com.vkontakte.android",
            "com.bereal.ft",
            "com.discord", // also Communication — overlap OK
            "club.beeper.android",
            "com.twitter.android.lite"
        ),
        AppCategory.STREAMING to setOf(
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music",
            "tv.twitch.android.app",
            "com.netflix.mediaclient",
            "com.hulu.plus",
            "com.disney.disneyplus",
            "com.amazon.avod.thirdpartyclient", // Prime Video
            "com.spotify.music",
            "com.aspiro.tidal",
            "com.max.android", // Max / HBO
            "com.hbo.hbonow",
            "com.apple.android.music",
            "com.pandora.android",
            "deezer.android.app",
            "com.soundcloud.android",
            "com.crunchyroll.crunchyroid",
            "com.plexapp.android",
            "com.vudu.android",
            "com.peacocktv.peacockandroid",
            "com.wbd.stream",
            "com.tubitv"
        ),
        AppCategory.GAMES to setOf(
            "com.supercell.clashofclans",
            "com.supercell.clashroyale",
            "com.supercell.brawlstars",
            "com.king.candycrushsaga",
            "com.king.farmheroessaga",
            "com.mojang.minecraftpe",
            "com.roblox.client",
            "com.epicgames.fortnite",
            "com.activision.callofduty.shooter",
            "com.tencent.ig", // PUBG Mobile
            "com.pubg.imobile",
            "com.nianticlabs.pokemongo",
            "com.nintendo.zara", // Switch Online
            "com.ea.gp.fifamobile",
            "com.gameloft.android.ANMP.GloftA9HM"
        ),
        AppCategory.BROWSERS to setOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.brave.browser",
            "org.mozilla.firefox",
            "org.mozilla.firefox_beta",
            "org.mozilla.focus",
            "com.microsoft.emmx", // Edge
            "com.opera.browser",
            "com.opera.mini.native",
            "com.sec.android.app.sbrowser", // Samsung Internet
            "com.duckduckgo.mobile.android",
            "com.kiwibrowser.browser",
            "com.vivaldi.browser",
            "com.uc.browser.en"
        ),
        AppCategory.COMMUNICATION to setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "org.telegram.messenger.web",
            "com.facebook.orca", // Messenger
            "com.facebook.mlite",
            "com.discord",
            "org.thoughtcrime.securesms", // Signal
            "com.skype.raider",
            "com.viber.voip",
            "com.google.android.apps.messaging",
            "com.google.android.apps.tachyon", // Duo / Meet
            "com.slack",
            "us.zoom.videomeetings",
            "com.microsoft.teams",
            "jp.naver.line.android",
            "com.snapchat.android", // overlap with Social
            "com.kakao.talk",
            "org.thunderdog.challegram" // Telegram X
        ),
        AppCategory.SHOPPING to setOf(
            "com.amazon.mShop.android.shopping",
            "com.amazon.aiv.aivapp",
            "com.ebay.mobile",
            "com.ebay.kleinanzeigen",
            "com.walmart.android",
            "com.target.ui",
            "com.contextlogic.wish",
            "com.zzkko", // Shein
            "com.alibaba.aliexpresshd",
            "com.etsy.android",
            "com.shopify.mobile",
            "com.bestbuy.android",
            "com.poshmark.app",
            "com.mercari",
            "com.offerup",
        ),
        AppCategory.NEWS to setOf(
            "com.google.android.apps.magazines", // Google News
            "flipboard.app",
            "com.nytimes.android",
            "com.washingtonpost.android",
            "com.cnn.mobile.android.phone",
            "com.bbc.news",
            "com.foxnews.android",
            "com.apple.news",
            "com.medium.reader",
            "com.reddit.frontpage" // overlap with Social
        ),
        AppCategory.ENTERTAINMENT to setOf(
            "com.reddit.frontpage",
            "com.ninegag.android.app",
            "com.zhiliaoapp.musically",
            "com.google.android.youtube",
            "tv.twitch.android.app",
            "com.tumblr",
            "com.imdb.mobile",
            "com.letterboxd.letterboxd"
        )
    )

    private val heuristics: Map<AppCategory, List<String>> = mapOf(
        AppCategory.SOCIAL to listOf(
            "instagram", "facebook", "twitter", "tiktok", "snapchat",
            "linkedin", "pinterest", "threads", "bereal", "reddit"
        ),
        AppCategory.STREAMING to listOf(
            "youtube", "netflix", "hulu", "disney", "spotify", "twitch",
            "primevideo", "hbo", "peacock", "crunchyroll", "tidal", "pandora",
            "soundcloud", "tubi"
        ),
        AppCategory.GAMES to listOf(
            "game", "games", "gaming", "play.", "unity", "roblox",
            "minecraft", "fortnite", "candycrush", "clash"
        ),
        AppCategory.BROWSERS to listOf(
            "browser", "chrome", "firefox", "brave", "opera", "edge",
            "duckduckgo", "webview"
        ),
        AppCategory.COMMUNICATION to listOf(
            "whatsapp", "telegram", "messenger", "signal", "discord",
            "skype", "viber", "slack", "zoom", "imessage", "sms", "chat"
        ),
        AppCategory.SHOPPING to listOf(
            "amazon", "ebay", "shop", "shopping", "walmart", "etsy",
            "aliexpress", "wish", "shein", "mercari", "offerup"
        ),
        AppCategory.NEWS to listOf(
            "news", "nyt", "cnn", "bbc", "flipboard", "medium"
        ),
        AppCategory.ENTERTAINMENT to listOf(
            "imdb", "letterboxd", "9gag", "meme", "entertainment"
        )
    )

    fun matches(category: AppCategory, packageName: String, label: String): Boolean {
        val pkg = packageName.lowercase()
        val name = label.lowercase()
        if (known[category]?.contains(packageName) == true) return true
        // Exact-ish known entries sometimes differ by suffix; also check contains for short known tokens
        val keys = heuristics[category] ?: emptyList()
        return keys.any { key ->
            pkg.contains(key) || name.contains(key)
        }
    }

    fun matchingPackages(
        category: AppCategory,
        apps: List<AppInfo>
    ): Set<String> =
        apps.filter { matches(category, it.packageName, it.label) }
            .map { it.packageName }
            .toSet()
}
