package mohaamadreza.saemipour.no.vazheh

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform