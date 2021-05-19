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
 * Settlement ID
 * @param id ID of a settlement
 * @param createdAt When the settlement was formed
 * @param settledAt When the settlement was paid
 */

data class SettlementIdItem (
    /* ID of a settlement */
    @Json(name = "id")
    val id: kotlin.Long,
    /* When the settlement was formed */
    @Json(name = "createdAt")
    val createdAt: kotlin.String,
    /* When the settlement was paid */
    @Json(name = "settledAt")
    val settledAt: kotlin.String
)
