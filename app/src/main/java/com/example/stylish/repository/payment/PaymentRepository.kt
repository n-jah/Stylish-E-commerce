package com.example.stylish.repository.payment
// PaymentRepository.kt
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class PaymentRepository(private val context: Context) {
    // ToDo dontforget to remove it
    private  val SECRET_KEY = "sk_test_51PvqwnGfrZnPfialyA9oJASFGdYjYXGtMmPAW5dTWDacyedx2jp0QIGjLU3nOpNYVviu7PTnMyhXvlniU13uqWQh00tYsdRf3s" // Replace with actual secret key


    private val queue = Volley.newRequestQueue(context)

    fun createCustomer(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val url = "https://api.stripe.com/v1/customers"

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                val customerId = JSONObject(response).getString("id")
                onSuccess(customerId)
                Log.d("payment","Customer ID"+ customerId)
            },
            { error -> onError(error.message ?: "Failed to create customer") }
        ) {
            override fun getHeaders() = mapOf("Authorization" to "Bearer $SECRET_KEY")
        }

        queue.add(request)
    }

    fun createEphemeralKey(customerId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val url = "https://api.stripe.com/v1/ephemeral_keys"

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                val ephemeralKey = JSONObject(response).getString("id")
                onSuccess(ephemeralKey)
                Log.d("payment","Ephemeral Key"+ ephemeralKey)
            },
            { error -> onError(error.message ?: "Failed to create ephemeral key") }
        ) {
            override fun getHeaders() = mapOf(
                "Authorization" to "Bearer $SECRET_KEY",
                "Stripe-Version" to "2024-06-20"
            )

            override fun getParams() = mapOf("customer" to customerId)
        }

        queue.add(request)
    }

    fun createPaymentIntent(customerId: String, ephemeralKey: String, amount: Int, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val url = "https://api.stripe.com/v1/payment_intents"

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                val clientSecret = JSONObject(response).getString("client_secret")
                onSuccess(clientSecret)
                Log.d("payment","Client Secret"+ clientSecret)
            },
            { error ->
                val responseBody = error.networkResponse?.data?.let { String(it) } ?: "No response body"
                Log.e("payment", "Error creating payment intent: $responseBody")
                onError("Failed to create payment intent: $responseBody")
            }

        ) {
            override fun getHeaders() = mapOf("Authorization" to "Bearer $SECRET_KEY")

            override fun getParams() = mapOf(
                "customer" to customerId,
                "amount" to amount.toString(),
                "currency" to "usd",
                "automatic_payment_methods[enabled]" to "true",
                "setup_future_usage" to "off_session"
            )
        }

        queue.add(request)
    }
}
