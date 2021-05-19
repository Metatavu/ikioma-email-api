/**
* Payment service API
* Payment processing API  Note: The API is currently under development. Some endpoints do not yet have all the features described here, and the responses of some do not match the description here. 
*
* The version of the OpenAPI document: 2.0.0
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package fi.metatavu.ikioma.email.payment.spec.model


import com.squareup.moshi.Json

/**
 * 
 * @param streetAddress Street address
 * @param postalCode Postal code
 * @param city City
 * @param country Alpha-2 country code
 * @param county County or top-level geographic subdivision
 */

data class Address (
    /* Street address */
    @Json(name = "streetAddress")
    val streetAddress: kotlin.String,
    /* Postal code */
    @Json(name = "postalCode")
    val postalCode: kotlin.String,
    /* City */
    @Json(name = "city")
    val city: kotlin.String,
    /* Alpha-2 country code */
    @Json(name = "country")
    val country: kotlin.String,
    /* County or top-level geographic subdivision */
    @Json(name = "county")
    val county: kotlin.String? = null
)
