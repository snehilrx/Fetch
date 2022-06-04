package com.fetch.cloudflarebypass.uam

/**
 * Holds the necessary information used to parse and solve the challenge
 *
 * @param protocol Protocol used(http or https)
 * @param host Domain
 * @param page Body content of the IUAM page
 */
data class UAMPageAtributes(val protocol: String, val host: String, val page: String) {

    val formParams: UAMPageFormParams
        get() = UAMPageFormParams.fromUAMPage(this)

}