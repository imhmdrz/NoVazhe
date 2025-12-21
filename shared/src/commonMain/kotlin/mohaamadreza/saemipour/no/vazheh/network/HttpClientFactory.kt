package mohaamadreza.saemipour.no.vazheh.network

import io.ktor.client.HttpClient

/** Factory for creating platform-specific HttpClient */
expect fun createHttpClient(): HttpClient
