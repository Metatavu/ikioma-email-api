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

import fi.metatavu.ikioma.email.payment.spec.model.PaymentMethodGroupData
import fi.metatavu.ikioma.email.payment.spec.model.PaymentMethodProvider

import com.squareup.moshi.Json

/**
 * Response for a successful payment request. Mechant ecom site can then either redirect the user to the URL given in href, or render the payment provider forms onsite. For each payment method an HTML form needs to be rendered using the parameters returned for each payment method provider. 
 * @param transactionId Checkout assigned transaction ID for the payment.
 * @param href Unique URL to hosted payment gateway
 * @param terms Text containing a link to the terms of payment
 * @param groups Contains data about the payment method groups. Contains only the groups found in the response's providers.
 * @param providers 
 */

data class PaymentRequestResponse (
    /* Checkout assigned transaction ID for the payment. */
    @Json(name = "transactionId")
    val transactionId: java.util.UUID,
    /* Unique URL to hosted payment gateway */
    @Json(name = "href")
    val href: kotlin.String,
    /* Text containing a link to the terms of payment */
    @Json(name = "terms")
    val terms: kotlin.String? = null,
    /* Contains data about the payment method groups. Contains only the groups found in the response's providers. */
    @Json(name = "groups")
    val groups: kotlin.collections.List<PaymentMethodGroupData>? = null,
    @Json(name = "providers")
    val providers: kotlin.collections.List<PaymentMethodProvider>? = null
)

