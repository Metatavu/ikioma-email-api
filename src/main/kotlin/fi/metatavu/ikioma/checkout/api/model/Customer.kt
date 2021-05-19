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
 * @param email Customer email address
 * @param firstName Customer first name
 * @param lastName Customer last name
 * @param phone Customer phone number
 * @param vatId Company VAT ID in international format
 */

data class Customer (
    /* Customer email address */
    @Json(name = "email")
    val email: kotlin.String,
    /* Customer first name */
    @Json(name = "firstName")
    val firstName: kotlin.String? = null,
    /* Customer last name */
    @Json(name = "lastName")
    val lastName: kotlin.String? = null,
    /* Customer phone number */
    @Json(name = "phone")
    val phone: kotlin.String? = null,
    /* Company VAT ID in international format */
    @Json(name = "vatId")
    val vatId: kotlin.String? = null
)

