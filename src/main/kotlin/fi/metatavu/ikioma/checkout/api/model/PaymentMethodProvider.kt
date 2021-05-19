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

import fi.metatavu.ikioma.email.payment.spec.model.BasePaymentMethodProvider
import fi.metatavu.ikioma.email.payment.spec.model.PaymentMethodProviderAllOf
import fi.metatavu.ikioma.email.payment.spec.model.PaymentMethodProviderAllOfParameters

import com.squareup.moshi.Json

/**
 * 
 * @param id ID of the provider
 * @param name Display name of the payment method
 * @param svg URL to payment method SVG icon (recommended to be used instead if PNG)
 * @param icon URL to payment method PNG icon
 * @param group 
 * @param url Form action url
 * @param parameters 
 */

data class PaymentMethodProvider (
    /* ID of the provider */
    @Json(name = "id")
    val id: kotlin.String,
    /* Display name of the payment method */
    @Json(name = "name")
    val name: kotlin.String,
    /* URL to payment method SVG icon (recommended to be used instead if PNG) */
    @Json(name = "svg")
    val svg: kotlin.String,
    /* URL to payment method PNG icon */
    @Json(name = "icon")
    val icon: kotlin.String,
    @Json(name = "group")
    val group: PaymentMethodProvider.Group,
    /* Form action url */
    @Json(name = "url")
    val url: kotlin.String,
    @Json(name = "parameters")
    val parameters: kotlin.collections.List<PaymentMethodProviderAllOfParameters>
) {

    /**
     * 
     * Values: mobile,bank,creditcard,credit
     */
    enum class Group(val value: kotlin.String) {
        @Json(name = "mobile") mobile("mobile"),
        @Json(name = "bank") bank("bank"),
        @Json(name = "creditcard") creditcard("creditcard"),
        @Json(name = "credit") credit("credit");
    }
}
